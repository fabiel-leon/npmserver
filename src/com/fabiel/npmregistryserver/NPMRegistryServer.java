/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.npmregistryserver;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Fabiel <fabiel.leon at gmail.com>
 */
public class NPMRegistryServer {

    final static String REPODIR = System.getProperty("user.dir");
    static String REPOURL = "http://registry.npmjs.org";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println(Paths.get("/-", "" + "-" + "asda" + ".tgz").toString());
        // TODO code application logic here
        System.getProperties().put("java.net.useSystemProxies", true);
//            List<String> readAllLines = Files.readAllLines(Paths.get("funcionalidades.txt"));
        System.out.println("SERVIDOR DE PAQUETES DE NODE *********\n"
                + "-servidor de paquetes de node\n"
                + "-servidor de documentacion de paquetes de node\n"
                + "-peticion a servidores de internet para paquetes no encontrados localmente , uso del proxy del sistema si esta establecido\n");

        REPOURL = "http://localhost:8888";

        final ExecutorService threads = Executors.newFixedThreadPool(5);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final String host = InetAddress.getLocalHost().getHostAddress();
                    final String hostname = InetAddress.getLocalHost().getHostName();
                    System.out.println("hostname = " + hostname);
//                    int port = 8080;
                    final int port = 7777;
                    System.out.println("carpeta = " + System.getProperty("user.dir"));
//                    final String repoDir ="D:\\server-repos\\repos\\npm";
                    HttpServer hs = HttpServer.create();
                    hs.bind(new InetSocketAddress(port), 0);
                    System.out.println("running = " + host + ":" + port);
                    System.out.println("running = localhost:" + port);
//                    hs.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
                    hs.createContext("/",
                            new HttpHandler() {

//            String repoDir = "D:\\repotest";
                        @Override
                        public void handle(HttpExchange he) throws IOException {
                            try {
                                URI requestURI = he.getRequestURI();
                                he.getResponseHeaders().add("Expires", "Fri, 30 Oct 3000 14:19:41 GMT");
//                                he.getResponseHeaders().add("ETag", "3e86-410-3596fbbc");
                                he.getResponseHeaders().add("Cache-Control", "max-age=315360000, public");
                                he.getResponseHeaders().add("Last-Modified", "Mon, 29 Jun 1998 02:28:12 GMT");
                                String path = requestURI.getPath();
                                String query = requestURI.getQuery();
                                int pathCount = Paths.get(path).getNameCount();
                                System.out.println("path = " + path + " count = " + pathCount);
                                File reqFile = new File(REPODIR, path);
                                /**
                                 * INDEX DE LA DOCUMENTACION si no hay query si
                                 * existe una query SERVIR DOCUMENTACION DEL
                                 * ARCHIVO index.json del MODULO
                                 */
                                if (pathCount == 0) {
                                    he.getResponseHeaders().add("Content-Type", "text/html");
                                    if (query == null) {
                                        //index 
                                        indexAllModules(reqFile, he);
                                    } else {
                                        //doc del modulo
                                        docModule(path, query, reqFile, he);
                                    }
                                } //
                                /**
                                 * servir Propiedades del modulo
                                 */
                                else if (pathCount == 1) {
                                    File index = new File(reqFile, "index.json");
                                    if (!index.exists()) {
                                        fallback(path, he);
                                    }
                                    servirIndexJson(index, path, he);
                                } //
                                /**
                                 * SERVIR GZIP TARBALL del modulo
                                 */
                                else if (pathCount == 3) {
                                    if (!reqFile.exists()) {
                                        fallback(path, he);
                                    }
                                    servirArchivo(reqFile, he);
                                } //
                                /**
                                 * SERVIR Propiedades de la version pedida del
                                 * modulo
                                 */
                                else if (pathCount == 2) {
                                    /**
                                     * SERVIR /module/0.0.0
                                     */
                                    String version = reqFile.getName();
                                    File moduleName = reqFile.getParentFile();
//                                    + port + path + "/-" + path + "-" + key + ".tgz"
                                    String filePath = Paths.get("/" + moduleName.getName(), "-", moduleName.getName() + "-" + version + ".tgz").toString();
                                    System.out.println("filePath = " + filePath);
                                    File file = new File(REPODIR, filePath);
                                    if (!file.exists()) {
                                        System.out.println("file no existe = " + file + " path=" + path);
                                        fallback(filePath, he);
                                    }
                                    TarArchiveInputStream stream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(file)));
                                    ArchiveEntry entry = stream.getNextEntry();
                                    while (!entry.getName().endsWith("/package.json") || "package.json".equals(entry.getName())) {
                                        entry = stream.getNextEntry();
                                    }
                                    JSONObject jsono = new JSONObject(new String(ByteStreams.toByteArray(stream), "ASCII"));
                                    if (!jsono.has("dist")) {
                                        JSONObject dist = new JSONObject();
                                        dist.put("tarball", "http://" + host + ":" + port + "/" + moduleName.getName() + "/-/" + file.getName());
                                        MessageDigest digest = MessageDigest.getInstance("SHA");
                                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                                        byte[] buff = new byte[10240];
                                        int cnt;
                                        while ((cnt = bis.read(buff)) != -1) {
                                            digest.update(buff, 0, cnt);
                                        }
                                        dist.put("shasum", javax.xml.bind.DatatypeConverter.printHexBinary(digest.digest()));
                                        jsono.put("dist", dist);
                                    }
                                    byte[] bytes = jsono.toString().getBytes();
                                    if (entry.getName().endsWith("/package.json") || "package.json".equals(entry.getName())) {
                                        servirStream(new ByteArrayInputStream(bytes), "application/json", bytes.length, he);
                                    }
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(NPMRegistryServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        private void servirArchivo(File index, HttpExchange he) throws IOException, Exception {
                            String probeContentType = Files.probeContentType(index.toPath());
                            if (probeContentType == null) {
                                if (index.getName().endsWith(".json")) {
                                    probeContentType = "application/json";
                                } else if (index.getName().endsWith(".tgz")) {
                                    probeContentType = "application/x-gzip";
                                }
                            }
                            if (probeContentType == null) {
                                throw new Exception("content type indefinido");
                            }
                            servirStream(new BufferedInputStream(new FileInputStream(index)), probeContentType, index.length(), he);
                        }

                        private void servirStream(InputStream index, String type, long length, HttpExchange he) throws IOException {
                            he.getResponseHeaders().add("Content-Type", type);
                            he.sendResponseHeaders(200, length);
                            try (OutputStream os = he.getResponseBody()) {
                                ByteStreams.copy(index, os);
                                os.flush();
                            }
                            he.close();
                        }

                        private void fallback(String path, HttpExchange he) throws IOException {
                            try {
                                Logger.getGlobal().log(Level.INFO, " trying download from internet {0}", path);
                                URLConnection openConnection = new URL(REPOURL + path).openConnection();
                                openConnection.setReadTimeout(30000);
                                openConnection.setConnectTimeout(5000);
                                //
                                BufferedOutputStream bos;
                                //escribir el archivo al disco
                                File file = new File(REPODIR, Paths.get(path, (openConnection.getContentType().contains("json") ? "/index.json" : "")).toString());
                                try (InputStream inputStream = openConnection.getInputStream()) {
                                    //escribir el archivo al disco
                                    file.getParentFile().mkdirs();
                                    bos = new BufferedOutputStream(new FileOutputStream(file));
                                    ByteStreams.copy(inputStream, bos);
                                    bos.flush();
                                    bos.close();
                                }
                                //servir archivo al cliente 
//                                servirStream(new ByteArrayInputStream(toByteArray), openConnection.getContentType(), openConnection.getContentLengthLong(), he);
                            } catch (Exception ex) {
                                Logger.getLogger(NPMRegistryServer.class.getName()).log(Level.SEVERE, null, ex);
                                notFound(path, he);
                            }
                        }

                        private void notFound(String path, HttpExchange he) throws IOException {
//                            try {
                            Logger.getGlobal().log(Level.INFO, "404 {0}", path);
                            byte[] bytes = "{\"succes\":false,\"message\":\"not found\", \"error\":true}".getBytes();
                            he.sendResponseHeaders(404, bytes.length);
                            try (OutputStream responseBody = he.getResponseBody()) {
                                responseBody.write(bytes);
                            }
                            he.close();
//                            } catch (IOException ex) {
//                                Logger.getLogger(NPMRegistryServer.class.getName()).log(Level.SEVERE, null, ex);
//                            }
                        }

                        private void servirIndexJson(File index, String path, HttpExchange he) throws JSONException, FileNotFoundException, IOException {
                            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(index))) {
                                JSONObject jsono = new JSONObject(new String(ByteStreams.toByteArray(bis), "ASCII"));
                                JSONObject versions = jsono.getJSONObject("versions");
                                for (Iterator it = versions.keys(); it.hasNext();) {
                                    String key = (String) it.next();
                                    JSONObject version = versions.getJSONObject(key);
                                    JSONObject dist = version.getJSONObject("dist");
                                    //dist.put("tarball", dist.getString("tarball").replaceFirst("localhost", host));
                                    dist.put("tarball", "http://" + host + ":" + port + path + "/-" + path + "-" + key + ".tgz");
                                }
                                byte[] bytes = jsono.toString().getBytes();
                                servirStream(new ByteArrayInputStream(bytes), "application/json", bytes.length, he);
                            }
                        }

                        private void indexAllModules(File f, HttpExchange he) throws IOException {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try (PrintWriter pw = new PrintWriter(baos)) {
                                File[] listFiles = f.listFiles();
                                for (File file : listFiles) {
                                    pw.print("<a href='?p=" + file.getName() + "'>" + file.getName() + "</a><br>");
                                }
                                pw.flush();
                            }
                            byte[] toByteArray = baos.toByteArray();
                            he.sendResponseHeaders(200, toByteArray.length);
                            ByteStreams.copy(new ByteArrayInputStream(toByteArray), he.getResponseBody());
                            he.close();
                        }

                        private void docModule(String path, String query, File f, HttpExchange he) throws IOException, JSONException {
                            String[] split = query.split("=");
                            File file = new File(new File(f, split[1]), "index.json");
                            if (file.exists()) {
                                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                                    JSONObject jsono = new JSONObject(new String(ByteStreams.toByteArray(bis), "ASCII"));
                                    try {
                                        String string = jsono.getString("readme");
                                        byte[] bytes = string.getBytes();
                                        servirStream(new ByteArrayInputStream(bytes), "text/plain", bytes.length, he);
                                    } catch (JSONException jsone) {
                                        notFound(path, he);
                                    }
                                }
                            } else {
                                notFound(path, he);
                            }
                        }
                    }
                    );
                    hs.setExecutor(threads);
                    hs.start();
                } catch (IOException ex) {
                    Logger.getLogger(NPMRegistryServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });
        t.start();
    }

}
