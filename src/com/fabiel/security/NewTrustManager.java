/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.security;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 *
 * @author FL
 */
public class NewTrustManager extends X509ExtendedTrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
