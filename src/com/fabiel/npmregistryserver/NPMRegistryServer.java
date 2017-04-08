/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.npmregistryserver;

import com.fabiel.npmregistryserver.db.Module;
import com.fabiel.npmregistryserver.db.ModuleJpaController;
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
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Fabiel <fabiel.leon at gmail.com>
 */
public class NPMRegistryServer {

    static String REPODIR = Paths.get(System.getProperty("user.dir"), "repo").toString();
    static String REPOURL = "http://registry.npmjs.org";

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
           REPODIR =  Paths.get("/media/datos/npm", "repo").toString();
        }
        System.out.println("repo "+ REPODIR);
//        Paths.get(System.getProperty("user.dir"), "repo").toString();
//        String url = URLDecoder.decode("http://localhost:3333/search.html?q=asd+sss", "UTF-8");
//        System.out.println(url);
//        System.out.println(URLEncoder.encode("http://localhost:3333/search.html?q=asd+sss", "UTF-8"));
//        URL u = null;
//        try {
//            u = new URL(url);
//            InputStream openStream = u.openStream();
//            openStream.read(new byte[11022] );
//        } catch (MalformedURLException e) {
//            System.out.println("malformed");
//        }
//        try {
//            u.toURI();
//        } catch (URISyntaxException e) {
//            System.out.println("URI");
//        }
//        System.setProperty("java.net.useSystemProxies", "true");

        Properties p = new Properties();
        try {
            p.load(new BufferedInputStream(new FileInputStream("proxy.properties")));
        } catch (Exception e) {

        }
        if (p.containsKey("http.proxyHost")) {
            System.getProperties().putAll(p);
//            System.setProperties();
        }
        System.getProperties().list(System.out);

        System.out.println("SERVIDOR DE PAQUETES DE NODE *********\n"
                + "para corregir errores pasar 1 parametro\n"
                + "-servidor de paquetes de node\n"
                + "-servidor de documentacion de paquetes de node\n"
                + "-peticion a servidores de internet para paquetes no encontrados localmente o con un mes de desactualizados, uso del proxy del sistema si esta establecido\n"
                + "-borrado y re-descarga de los index.json para los arhivos mal conformados maximo 3 veces\n"
                + "-descarga en archivos temporales para no se pierdan los archivos originales hasta que se descarguen las nuevas\n"
                + "-en nuevaa version funcionalidad de busqueda por keywords,description y readme del json cambiar a express con mongoose");
//        try {
//            Class.forName("org.sqlite.JDBC");
//        } catch (ClassNotFoundException ex) {
//            System.err.printf("error:  {0}", ex);
//        }
     
            File file = new File(System.getProperty("user.dir"), "file.json");
            File createTempFile = File.createTempFile("test", "test", new File(System.getProperty("user.dir")));
            if (file.exists() && !file.delete()) {
                throw new IOException("error file = " + file + " no se borro");
            }
            if (!createTempFile.renameTo(file)) {
                throw new IOException("error file = " + createTempFile + " no se renombro");
            }
       

        try {
            //configurar Logger
            FileHandler fh = new FileHandler("npmserver.log", true);
            Logger.getLogger("").addHandler(fh);
        } catch (IOException | SecurityException ex) {
            System.err.printf("error:  '%1$s'\n", ex);
        }

//        REPOURL = "http://localhost:8888";
//        System.out.println("");
//        EntityManagerFactory emf2 = Persistence.createEntityManagerFactory("npmserverPU");
        final ExecutorService threads = Executors.newFixedThreadPool(15);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final String host = InetAddress.getLocalHost().getHostAddress();
                    final String hostname = InetAddress.getLocalHost().getHostName();
                    System.out.printf("hostname = '%1$s'\n", hostname);
//                    System.out.println();
//                    int port = 8080;
                    final int port = 8888;
                    System.out.printf("carpeta = '%1$s'\n", REPODIR);
//                    final String repoDir ="D:\\server-repos\\repos\\npm";
                    HttpServer hs = HttpServer.create();
                    hs.bind(new InetSocketAddress(port), 0);
                    System.out.printf("running = '%1$s':'%2$s'\n", new Object[]{host, port});
                    System.out.printf("running = localhost:'%1$s'\n", port);
//                    hs.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
                    hs.createContext("/",
                            new HttpHandler() {
//            String repoDir = "D:\\repotest";
                        @Override
                        public void handle(HttpExchange he) throws IOException {
                            URI requestURI = he.getRequestURI();
                            String path = requestURI.getRawPath();
                            String query = requestURI.getQuery();
//                            System.out.println("query = " + query);
//                            System.out.println("path = " + path);
                            try {

//                                        he.getResponseHeaders().add("Expires", "Fri, 30 Oct 3000 14:19:41 GMT");
//                                he.getResponseHeaders().add("ETag", "3e86-410-3596fbbc");
//                                        he.getResponseHeaders().add("Cache-Control", "max-age=315360000, public");
//                                        he.getResponseHeaders().add("Last-Modified", "Mon, 29 Jun 1998 02:28:12 GMT");
                                int pathCount = Paths.get(path).getNameCount();
//                                System.out.println("path = " + path + " count = " + pathCount);
                                File reqFile = new File(REPODIR, path);
                                //
                                switch (pathCount) {
                                    //
                                    /**
                                     * path = 0 INDEX DE LA DOCUMENTACION si no
                                     * hay query si existe una query SERVIR
                                     * DOCUMENTACION DEL ARCHIVO index.json del
                                     * MODULO
                                     */
                                    case 0:
                                        he.getResponseHeaders().add("Content-Type", "text/html");
                                        if (query == null) {
                                            //index
                                            indexAllModules(reqFile, he);
                                        } else {
                                            //doc del modulo
                                            docModule(path, query, reqFile, he);
                                        }
                                        break;
                                    //
                                    /**
                                     * path = 1 servir Propiedades del modulo
                                     */
                                    case 1:
                                        File index = new File(reqFile, "index.json");
                                        //si el arhivo json tiene problemas eliminarlo y descargarlo hasta que se descargue bien (o hasta que se seque el malecon)
                                        int count = 0;
                                        while (true) {
                                            try {
//                                                        if (!index.exists()) {
//                                                            fallback(path, he);
//                                                        }
//                                                        FileTime lmt = Files.getLastModifiedTime(index.toPath());
//                                                        if ((new Date().getTime() - lmt.toMillis()) > TimeUnit.DAYS.toMillis(30)) {
                                                System.out.printf("fall back for update");
//                                                            System.out.printf("fall back for \"last modified = \"'%1$s'\n", new Date(lmt.toMillis()));
                                                try {
                                                    fallback(path, he);
                                                } catch (IOException e) {
                                                    System.err.printf("error:  '%1$s'\n", e);
                                                    //silently serve old index.json
                                                    System.out.printf("  could not download from internet '%1$s'\n", path);
                                                }
//                                                        }
                                                servirIndexJson(index, path, he);
                                                break;
                                            } catch (IOException | JSONException jsone) {
                                                count++;
                                                if (count == 3) {
                                                    throw jsone;
                                                }
                                                index.delete();
                                            }
                                        }
                                        break;
                                    //
                                    /**
                                     * path = 3 SERVIR GZIP TARBALL del modulo
                                     */
                                    case 3:
                                        if (!reqFile.exists()) {
                                            System.out.printf("no existe localmente = '%1$s'\n", path);
                                            fallback(path, he);
                                        }
                                        servirArchivo(reqFile, he);
                                        break;
                                    /**
                                     * path = 2 SERVIR Propiedades de la version
                                     * pedida del modulo eje: /module/0.0.0
                                     */
                                    case 2:
                                        String version = reqFile.getName();
                                        File moduleName = reqFile.getParentFile();
                                        //                                    + port + path + "/-" + path + "-" + key + ".tgz"
                                        String filePath = Paths.get("/" + moduleName.getName(), "-", moduleName.getName() + "-" + version + ".tgz").toString();
//                                                Logger.getLogger(NPMRegistryServer.class.getName()).log(Level.INFO, "filePath = {0}", filePath);
                                        File file = new File(REPODIR, filePath);
                                        if (!file.exists()) {
                                            System.out.printf("file no existe = '%1$s' path='%2$s'\n", new Object[]{file, path});
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
                                        break;
                                    default:
                                        break;
                                }
                            } catch (Exception ex) {
                                notFound(path, he);
                                System.err.printf("error:  '%1$s'\n", ex.getLocalizedMessage());
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

                        private void fallback(String path, HttpExchange he) throws FileNotFoundException, IOException {
                            System.out.printf(" trying download from internet '%1$s%2$s'\n", new Object[]{REPOURL, path});
                            HttpURLConnection openConnection = (HttpURLConnection) new URL(REPOURL + path.replace("\\", "/")).openConnection();
//                            openConnection.setRequestProperty("Accept-Encoding", "gzip");
                            openConnection.setInstanceFollowRedirects(false);
                            openConnection.setReadTimeout(60000);
                            openConnection.setConnectTimeout(60000);
//                            openConnection.
                            //escribir el archivo al disco
                            File file = new File(REPODIR, Paths.get(path, (!path.endsWith(".tgz") ? "/index.json" : "")).toString());
                            file.getParentFile().mkdirs();
                            File createTempFile = File.createTempFile("npmserver", "temp", file.getParentFile());
//                            System.out.println("file = " + file);
                            try (InputStream inputStream = openConnection.getInputStream()) {
                                //escribir el archivo al disco
                                if (!"application/json".equals(openConnection.getContentType()) && !"application/octet-stream".equals(openConnection.getContentType())) {
                                    inputStream.close();
                                    throw new IOException("content type no requerido " + openConnection.getContentType());
                                }

                                BufferedOutputStream bos;
                                bos = new BufferedOutputStream(new FileOutputStream(createTempFile));
                                if ("gzip".equals(openConnection.getContentEncoding())) {
                                    ByteStreams.copy(new GZIPInputStream(inputStream), bos);
                                } else {
                                    ByteStreams.copy(inputStream, bos);
                                }
                                ByteStreams.copy(inputStream, bos);
                                bos.flush();
                                bos.close();
                            }
                            if (file.exists() && !file.delete()) {
                                throw new IOException("error file = " + file + " no se borro");
                            }
                            if (!createTempFile.renameTo(file)) {
                                throw new IOException("error file = " + createTempFile + " no se renombro");
                            }
                            System.out.printf("  downloaded from internet '%1$s'\n", path);
                        }

                        private void notFound(String path, HttpExchange he) throws IOException {
                            System.out.printf("404 '%1$s'\n", path);// , path);
                            byte[] bytes = "{\"succes\":false,\"message\":\"not found\", \"error\":true}".getBytes();
                            he.sendResponseHeaders(404, bytes.length);
                            try (OutputStream responseBody = he.getResponseBody()) {
                                responseBody.write(bytes);
                            }
                            he.close();
                        }

                        private void servirIndexJson(File index, String path, HttpExchange he) throws FileNotFoundException, IOException, JSONException {
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
                                int indexOf = versions.toString().indexOf("https://registry.npmjs.org/");
                                if (indexOf != -1) {
                                    System.out.println("archivo malo " + index.getAbsolutePath());
                                }
                                byte[] bytes = jsono.toString().getBytes();
                                servirStream(new ByteArrayInputStream(bytes), "application/json", bytes.length, he);
                            }
                        }

                        private void indexAllModules(File f, HttpExchange he) throws IOException {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try (PrintWriter pw = new PrintWriter(baos)) {
                                pw.print("<form><input name='search'/><input type='submit'/></form>");
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
                            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                                JSONObject jsono = new JSONObject(new String(ByteStreams.toByteArray(bis), "ASCII"));
                                String string = jsono.getString("readme");
                                byte[] bytes = string.getBytes();
                                servirStream(new ByteArrayInputStream(bytes), "text/plain", bytes.length, he);
                            }
                        }
                    }
                    );
                    hs.setExecutor(threads);
                    hs.start();
                } catch (IOException ex) {
                    System.err.printf("error:  '%1$s'\n", ex);
                }
            }

        });
        t.start();

//        int moduleCount = mjc.getModuleCount();
//        System.out.println("module Database Count = " + moduleCount);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("npmserverPU");
        final ModuleJpaController mjc = new ModuleJpaController(emf);
        Path repoDirPath = Paths.get(REPODIR);
        try {
            Files.walkFileTree(repoDirPath, EnumSet.allOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                    System.out.println("dir = " + dir);
                    return FileVisitResult.CONTINUE; //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public FileVisitResult visitFile(Path dir, BasicFileAttributes attrs) throws IOException {
                    String moduleName = dir.getFileName().toString();
                    Module findModule = mjc.findModule(moduleName);
                    Path resolve = dir.resolve("index.json");
                    if (findModule == null && Files.exists(resolve)) {
                        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(resolve))) {
                            findModule = new Module();
                            JSONObject jsono = new JSONObject(new String(ByteStreams.toByteArray(bis), "ASCII"));
                            findModule.setId(moduleName);
                            if (jsono.has("description")) {
                                String description = jsono.getString("description");
                                findModule.setDescription(description);
                            }
                            if (jsono.has("readme")) {
                                String readme = jsono.getString("readme");
                                findModule.setReadme(readme);
                            }
                            if (jsono.has("keywords")) {
                                JSONArray keywords = jsono.getJSONArray("keywords");
                                int length = keywords.length();
                                for (int i = 0; i < length; i++) {
                                    String keyword = keywords.getString(i);
                                    findModule.add(keyword);
                                }
                            }
                            mjc.create(findModule);
//                            System.out.println("modulo añadido = " + findModule);
                        } catch (Exception ex) {
                            System.err.printf("error:  '%1$s'\n", ex);
                        }
                    }
                    return FileVisitResult.CONTINUE; //To change body of generated methods, choose Tools | Templates.
                }

//                    @Override
//                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                        System.out.println("file = " + file);
//                        return FileVisitResult.CONTINUE; //To change body of generated methods, choose Tools | Templates.
//                    }
            });

        } catch (IOException ex) {
            System.err.printf("error:  '%1$s'\n", ex);
//            System.err.printf("error:  {0}", ex);
        }
//        int moduleCount2 = mjc.getModuleCount();
//        System.out.println("module Database Count = " + moduleCount2);
//        List<Module> filterModuleEntities = mjc.filterModuleEntities("yauzl", true, 10, 10);
//        for (Module get : filterModuleEntities) {
//            System.out.println("get = " + get);
//        }
    }
}
