/*-----------------------------------------------------------------------

  This file is part of the jSSLutils library.
  
Copyright (c) 2008-2009, The University of Manchester, United Kingdom.
All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright 
      notice, this list of conditions and the following disclaimer in the 
      documentation and/or other materials provided with the distribution.
 * Neither the name of the The University of Manchester nor the names of 
      its contributors may be used to endorse or promote products derived 
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

  Author........: Bruno Harbulot

-----------------------------------------------------------------------*/

package org.jsslutils.sslcontext.test;

import static org.junit.Assert.*;

import java.security.KeyStore;

import javax.net.ssl.SSLServerSocket;

import org.jsslutils.sslcontext.SSLContextFactory;
import org.jsslutils.sslcontext.X509SSLContextFactory;
import org.jsslutils.sslcontext.test.MiniSslClientServer;
import org.junit.Test;

/**
 * Tests the SSLContext configured for X.509 without CRLs. It should accept both
 * the "good" and the "bad" certificate.
 * 
 * @author Bruno Harbulot.
 * 
 */
public class SimpleX509Test extends MiniSslClientServer {
    protected KeyStore clientStore = null;
    protected SSLContextFactory clientSSLContextFactory;
    protected SSLContextFactory serverSSLContextFactory;

    @Test
    public void testGoodClient() throws Exception {
        this.clientStore = getGoodClientCertKeyStore();
        assertTrue("Loaded keystore", true);
        assertTrue(runTest());
    }

    @Test
    public void testBadClient() throws Exception {
        this.clientStore = getBadClientCertKeyStore();
        assertTrue("Loaded keystore", true);
        assertTrue(runTest());
    }

    public boolean prepareSSLContextFactories() throws Exception {
        this.clientSSLContextFactory = new X509SSLContextFactory(
                this.clientStore, MiniSslClientServer.KEYSTORE_PASSWORD,
                getCaKeyStore());
        this.serverSSLContextFactory = new X509SSLContextFactory(
                getServerCertKeyStore(), MiniSslClientServer.KEYSTORE_PASSWORD,
                getCaKeyStore());
        return true;
    }

    public boolean runTest() throws Exception {
        assertTrue(prepareSSLContextFactories());
        return runTest(clientSSLContextFactory.buildSSLContext(),
                serverSSLContextFactory.buildSSLContext());
    }

    public static void main(String[] args) throws Exception {
        SimpleX509Test test = new SimpleX509Test();
        test.verboseExceptions = true;
        test.prepareSSLContextFactories();
        SSLServerSocket serverSocket = test
                .prepareServerSocket(test.serverSSLContextFactory
                        .buildSSLContext());
        test.serverTimeout = 20000;
        test.runServer(serverSocket);
    }
}
