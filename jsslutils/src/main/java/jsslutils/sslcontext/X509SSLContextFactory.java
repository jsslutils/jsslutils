/*-----------------------------------------------------------------------

  This file is part of the jSSLutils library.
  
Copyright (c) 2008, The University of Manchester, United Kingdom.
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

package jsslutils.sslcontext;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class is a factory that provides methods for creating an SSLContext
 * configured with the settings set in this factory: using the SunX509 algorithm
 * for both the key manager and the trust manager. These managers are created
 * from the KeyStores passed to the constructor. Unlike the PKIX implementation,
 * this implementation does not support CRLs.
 * 
 * @author Bruno Harbulot
 * 
 */
public class X509SSLContextFactory extends SSLContextFactory {
	private KeyStore keyStore;
	private char[] keyPassword;
	private KeyStore trustStore;

	private boolean keyManagerWrapperLocked = false;
	private X509KeyManagerWrapper x509KeyManagerWrapper;

	private boolean trustManagerWrapperLocked = false;
	private X509TrustManagerWrapper x509TrustManagerWrapper;

	private CallbackHandler keyPasswordCallbackHandler;

	/**
	 * Builds an SSLContextFactory using the SunX509 algorithm in the
	 * TrustManagerFactory.
	 * 
	 * @param keyStore
	 *            KeyStore that contains the key.
	 * @param keyPassword
	 *            password to the key.
	 * @param trustStore
	 *            KeyStore that contains the trusted X.509 certificates.
	 */
	public X509SSLContextFactory(KeyStore keyStore, String keyPassword,
			KeyStore trustStore) {
		this(keyStore,
				(keyPassword != null) ? keyPassword.toCharArray() : null,
				trustStore);
	}

	/**
	 * Builds an SSLContextFactory using the SunX509 algorithm in the
	 * TrustManagerFactory.
	 * 
	 * @param keyStore
	 *            KeyStore that contains the key.
	 * @param keyPassword
	 *            password to the key.
	 * @param trustStore
	 *            KeyStore that contains the trusted X.509 certificates.
	 */
	public X509SSLContextFactory(KeyStore keyStore, char[] keyPassword,
			KeyStore trustStore) {
		this.keyStore = keyStore;
		this.keyPassword = keyPassword;
		this.trustStore = trustStore;
	}

	/**
	 * Returns the key store.
	 * 
	 * @return the key store.
	 */
	protected KeyStore getKeyStore() {
		return this.keyStore;
	}

	/**
	 * Returns the trust store.
	 * 
	 * @return the trust store.
	 */
	protected KeyStore getTrustStore() {
		return this.trustStore;
	}

	/**
	 * Sets the X509KeyManagerWrapper that will be used to wrap the trust
	 * managers returned by getRawKeyManagers() before being returned by
	 * getKeyManagers().
	 * 
	 * @param keyManagerWrapper
	 *            wrapper (may be null).
	 */
	public final void setKeyManagerWrapper(
			X509KeyManagerWrapper keyManagerWrapper)
			throws LockedSettingsException {
		synchronized (this) {
			if (!this.keyManagerWrapperLocked) {
				this.x509KeyManagerWrapper = keyManagerWrapper;
			} else {
				throw new LockedSettingsException(
						"KeyManagerWrapper already set and locked.");
			}
		}
	}

	/**
	 * Gets the X509KeyManagerWrapper that will be used to wrap the trust
	 * managers returned by getRawKeyManagers() before being returned by
	 * getKeyManagers().
	 * 
	 * @return wrapper.
	 */
	public final X509KeyManagerWrapper getKeyManagerWrapper() {
		return this.x509KeyManagerWrapper;
	}

	/**
	 * Locks the key manager wrapper so that it can no longer be set afterwards.
	 */
	public final void lockKeyManagerWrapper() {
		synchronized (this) {
			this.keyManagerWrapperLocked = true;
		}
	}

	/**
	 * Checks whether-or-not the key manager wrapper can still be changed.
	 * 
	 * @return true if it's locked, false if it can be changed.
	 */
	public final boolean isKeyManagerWrapperLocked() {
		synchronized (this) {
			return this.keyManagerWrapperLocked;
		}
	}

	/**
	 * Sets the X509TrustManagerWrapper that will be used to wrap the trust
	 * managers returned by getRawTrustManagers() before being returned by
	 * getTrustManagers().
	 * 
	 * @param trustManagerWrapper
	 *            wrapper (may be null).
	 */
	public final void setTrustManagerWrapper(
			X509TrustManagerWrapper trustManagerWrapper)
			throws LockedSettingsException {
		synchronized (this) {
			if (!this.trustManagerWrapperLocked) {
				this.x509TrustManagerWrapper = trustManagerWrapper;
			} else {
				throw new LockedSettingsException(
						"TrustManagerWrapper already set and locked.");
			}
		}
	}

	/**
	 * Gets the X509TrustManagerWrapper that will be used to wrap the trust
	 * managers returned by getRawTrustManagers() before being returned by
	 * getTrustManagers().
	 * 
	 * @return wrapper.
	 */
	public final X509TrustManagerWrapper getTrustManagerWrapper() {
		return this.x509TrustManagerWrapper;
	}

	/**
	 * Locks the trust manager wrapper so that it can no longer be set
	 * afterwards.
	 */
	public final void lockTrustManagerWrapper() {
		synchronized (this) {
			this.trustManagerWrapperLocked = true;
		}
	}

	/**
	 * Checks whether-or-not the trust manager wrapper can still be changed.
	 * 
	 * @return true if it's locked, false if it can be changed.
	 */
	public final boolean isTrustManagerWrapperLocked() {
		synchronized (this) {
			return this.trustManagerWrapperLocked;
		}
	}

	/**
	 * Sets the CallbackHandler that will be used to obtain the key password if
	 * this password is still null. (Optional.)
	 * 
	 * @param keyPasswordCallbackHandler
	 *            CallbackHandler that will be used to get the password.
	 */
	public void setKeyPasswordCallbackHandler(
			CallbackHandler keyPasswordCallbackHandler) {
		this.keyPasswordCallbackHandler = keyPasswordCallbackHandler;
	}

	/**
	 * Builds KeyManagers from the key store provided in the constructor, using
	 * a SunX509 KeyManagerFactory.
	 * 
	 * @return Key managers corresponding to the key store.
	 */
	protected KeyManager[] getRawKeyManagers()
			throws SSLContextFactoryException {
		if (this.keyStore != null) {
			try {
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				if ((this.keyPassword != null)
						|| (this.keyPasswordCallbackHandler == null)) {
					kmf.init(this.keyStore, this.keyPassword);
				} else {
					PasswordCallback passwordCallback = new PasswordCallback(
							"KeyStore password? ", false);
					this.keyPasswordCallbackHandler
							.handle(new Callback[] { passwordCallback });
					char[] password = passwordCallback.getPassword();
					kmf.init(this.keyStore, password);
					if (password != null) {
						for (int i = 0; i < password.length; i++) {
							password[i] = 0;
						}
					}
				}
				return kmf.getKeyManagers();
			} catch (NoSuchAlgorithmException e) {
				throw new SSLContextFactoryException(e);
			} catch (KeyStoreException e) {
				throw new SSLContextFactoryException(e);
			} catch (UnrecoverableKeyException e) {
				throw new SSLContextFactoryException(e);
			} catch (IOException e) {
				throw new SSLContextFactoryException(e);
			} catch (UnsupportedCallbackException e) {
				throw new SSLContextFactoryException(e);
			}
		} else {
			return null;
		}
	}

	/**
	 * Builds KeyManagers by wrapping getRawKeyManagers(), if a wrapper has been
	 * set up (return the original key manager otherwise).
	 * 
	 * @return Wrapped key managers from getRawKeyManagers().
	 */
	@Override
	public KeyManager[] getKeyManagers() throws SSLContextFactoryException {
		KeyManager[] keyManagers = getRawKeyManagers();
		X509KeyManagerWrapper wrapper = x509KeyManagerWrapper;
		if ((wrapper != null) && (keyManagers != null)) {
			for (int i = 0; i < keyManagers.length; i++) {
				if (keyManagers[i] instanceof X509KeyManager)
					keyManagers[i] = wrapper
							.wrapKeyManager((X509KeyManager) keyManagers[i]);
			}
		}
		return keyManagers;
	}

	/**
	 * Builds TrustManagers from the trust store provided in the constructor,
	 * using a SunX509 TrustManagerFactory.
	 * 
	 * @return SunX509-based trust managers corresponding to the trust store.
	 */
	protected TrustManager[] getRawTrustManagers()
			throws SSLContextFactoryException {
		if (this.trustStore != null) {
			try {
				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance("SunX509");
				tmf.init(this.trustStore);
				return tmf.getTrustManagers();
			} catch (NoSuchAlgorithmException e) {
				throw new SSLContextFactoryException(e);
			} catch (KeyStoreException e) {
				throw new SSLContextFactoryException(e);
			}
		} else {
			return null;
		}
	}

	/**
	 * Builds TrustManagers by wrapping getRawTrustManagers(), if a wrapper has
	 * been set up (return the original trust manager otherwise).
	 * 
	 * @return Wrapped trust managers from getRawTrustManagers().
	 */
	@Override
	public TrustManager[] getTrustManagers() throws SSLContextFactoryException {
		TrustManager[] trustManagers = getRawTrustManagers();
		X509TrustManagerWrapper wrapper = x509TrustManagerWrapper;
		if ((wrapper != null) && (trustManagers != null)) {
			for (int i = 0; i < trustManagers.length; i++) {
				if (trustManagers[i] instanceof X509TrustManager)
					trustManagers[i] = wrapper
							.wrapTrustManager((X509TrustManager) trustManagers[i]);
			}
		}
		return trustManagers;
	}

	/**
	 * This is an exception that should occur when trying to set properties that
	 * should no longer be set.
	 * 
	 * @author Bruno Harbulot.
	 */
	public static class LockedSettingsException extends Exception {
		private static final long serialVersionUID = 3649279179955493548L;

		public LockedSettingsException() {
			super();
		}

		public LockedSettingsException(Throwable ex) {
			super(ex);
		}

		public LockedSettingsException(String message) {
			super(message);
		}

		public LockedSettingsException(String message, Throwable ex) {
			super(message, ex);
		}
	}
}