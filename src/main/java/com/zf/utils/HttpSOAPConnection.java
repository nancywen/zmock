package com.zf.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.JaxmURI;
import com.sun.xml.internal.messaging.saaj.util.ParseUtil;

public class HttpSOAPConnection extends SOAPConnection {
	public static final String vmVendor = System.getProperty("java.vendor.url");
	private static final String sunVmVendor = "http://java.sun.com/";
	private static final String ibmVmVendor = "http://www.ibm.com/";
	private static final boolean isSunVM = "http://java.sun.com/"
			.equals(vmVendor);
	private static final boolean isIBMVM = "http://www.ibm.com/"
			.equals(vmVendor);
	private static final String JAXM_URLENDPOINT = "javax.xml.messaging.URLEndpoint";
	protected static final Logger log = Logger.getLogger(
			"javax.xml.messaging.saaj.client.p2p",
			"com.sun.xml.internal.messaging.saaj.client.p2p.LocalStrings");

	private static final String defaultProxyHost = null;
	private static final int defaultProxyPort = -1;
	MessageFactory messageFactory = null;

	boolean closed = false;

	private String proxyHost = null;

	private int proxyPort = -1;
	private static final String SSL_PKG;
	private static final String SSL_PROVIDER;
	private static final int dL = 0;

	public HttpSOAPConnection() throws SOAPException {
		this.proxyHost = defaultProxyHost;
		this.proxyPort = -1;
		try {
			this.messageFactory = MessageFactory
					.newInstance("Dynamic Protocol");
		} catch (NoSuchMethodError localNoSuchMethodError) {
			this.messageFactory = MessageFactory.newInstance();
		} catch (Exception localException) {
			log.log(Level.SEVERE, "SAAJ0001.p2p.cannot.create.msg.factory",
					localException);
			throw new SOAPExceptionImpl("Unable to create message factory",
					localException);
		}
	}

	public void close() throws SOAPException {
		if (this.closed) {
			log.severe("SAAJ0002.p2p.close.already.closed.conn");
			throw new SOAPExceptionImpl("Connection already closed");
		}

		this.messageFactory = null;
		this.closed = true;
	}

	public SOAPMessage call(SOAPMessage paramSOAPMessage, Object paramObject)
			throws SOAPException {
		if (this.closed) {
			log.severe("SAAJ0003.p2p.call.already.closed.conn");
			throw new SOAPExceptionImpl("Connection is closed");
		}

		Class localClass = null;
		ClassLoader localClassLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			if (localClassLoader != null)
				localClass = localClassLoader
						.loadClass("javax.xml.messaging.URLEndpoint");
			else
				localClass = Class.forName("javax.xml.messaging.URLEndpoint");
		} catch (ClassNotFoundException localClassNotFoundException) {
			log.finest("SAAJ0090.p2p.endpoint.available.only.for.JAXM");
		}

		if ((localClass != null) && (localClass.isInstance(paramObject))) {
			String str = null;
			try {
				Method localMethod = localClass.getMethod("getURL",
						(Class[]) null);
				str = (String) localMethod.invoke(paramObject, (Object[]) null);
			} catch (Exception localException2) {
				log.log(Level.SEVERE, "SAAJ0004.p2p.internal.err",
						localException2);
				throw new SOAPExceptionImpl("Internal error: "
						+ localException2.getMessage());
			}
			try {
				paramObject = new URL(str);
			} catch (MalformedURLException localMalformedURLException2) {
				log.log(Level.SEVERE, "SAAJ0005.p2p.",
						localMalformedURLException2);
				throw new SOAPExceptionImpl("Bad URL: "
						+ localMalformedURLException2.getMessage());
			}

		}

		if (paramObject instanceof String) {
			try {
				paramObject = new URL((String) paramObject);
			} catch (MalformedURLException localMalformedURLException1) {
				log.log(Level.SEVERE, "SAAJ0006.p2p.bad.URL",
						localMalformedURLException1);
				throw new SOAPExceptionImpl("Bad URL: "
						+ localMalformedURLException1.getMessage());
			}
		}

		if (paramObject instanceof URL)
			try {
				PriviledgedPost localPriviledgedPost = new PriviledgedPost(
						this, paramSOAPMessage, (URL) paramObject);

				SOAPMessage localSOAPMessage = (SOAPMessage) AccessController
						.doPrivileged(localPriviledgedPost);

				return localSOAPMessage;
			} catch (Exception localException1) {
				throw new SOAPExceptionImpl(localException1);
			}
		log.severe("SAAJ0007.p2p.bad.endPoint.type");
		throw new SOAPExceptionImpl("Bad endPoint type " + paramObject);
	}

	public void setProxy(String paramString, int paramInt) {
		try {
			this.proxyPort = paramInt;
			PriviledgedSetProxyAction localPriviledgedSetProxyAction = new PriviledgedSetProxyAction(
					paramString, paramInt);
			this.proxyHost = ((String) AccessController
					.doPrivileged(localPriviledgedSetProxyAction));
		} catch (Exception localException) {
			throw new RuntimeException(localException);
		}
	}

	public String getProxyHost() {
		return this.proxyHost;
	}

	public int getProxyPort() {
		return this.proxyPort;
	}

	SOAPMessage post(SOAPMessage paramSOAPMessage, URL paramURL)
			throws SOAPException {
		int i = 0;

		URL localURL = null;
		HttpURLConnection localHttpURLConnection = null;

		int j = 0;
		Object localObject1;
		Object localObject2;
		Object localObject3;
		int k;
		Object localObject4;
		try {
			if (paramURL.getProtocol().equals("https")) {
				initHttps();
			}
			JaxmURI localJaxmURI = new JaxmURI(paramURL.toString());
			localObject1 = localJaxmURI.getUserinfo();

			localURL = paramURL;

			if ((!(localURL.getProtocol().equalsIgnoreCase("http")))
					&& (!(localURL.getProtocol().equalsIgnoreCase("https")))) {
				log.severe("SAAJ0052.p2p.protocol.mustbe.http.or.https");
				throw new IllegalArgumentException("Protocol "
						+ localURL.getProtocol() + " not supported in URL "
						+ localURL);
			}

			localHttpURLConnection = createConnection(localURL);

			localHttpURLConnection.setRequestMethod("POST");

			localHttpURLConnection.setDoOutput(true);
			localHttpURLConnection.setDoInput(true);
			localHttpURLConnection.setUseCaches(false);
			HttpURLConnection.setFollowRedirects(true);

			if (paramSOAPMessage.saveRequired()) {
				paramSOAPMessage.saveChanges();
			}
			localObject2 = paramSOAPMessage.getMimeHeaders();

			localObject3 = ((MimeHeaders) localObject2).getAllHeaders();
			k = 0;
			while (((Iterator) localObject3).hasNext()) {
				localObject4 = (MimeHeader) ((Iterator) localObject3).next();

				String[] arrayOfString = ((MimeHeaders) localObject2)
						.getHeader(((MimeHeader) localObject4).getName());

				if (arrayOfString.length == 1) {
					localHttpURLConnection.setRequestProperty(
							((MimeHeader) localObject4).getName(),
							((MimeHeader) localObject4).getValue());
				} else {
					StringBuffer localStringBuffer = new StringBuffer();
					int i1 = 0;
					while (i1 < arrayOfString.length) {
						if (i1 != 0)
							localStringBuffer.append(',');
						localStringBuffer.append(arrayOfString[i1]);
						++i1;
					}

					localHttpURLConnection.setRequestProperty(
							((MimeHeader) localObject4).getName(),
							localStringBuffer.toString());
				}

				if ("Authorization".equals(((MimeHeader) localObject4)
						.getName())) {
					k = 1;
					log.fine("SAAJ0091.p2p.https.auth.in.POST.true");
				}
			}

			if ((k == 0) && (localObject1 != null)) {
				initAuthUserInfo(localHttpURLConnection, (String) localObject1);
			}

			localObject4 = localHttpURLConnection.getOutputStream();
			paramSOAPMessage.writeTo((OutputStream) localObject4);

			((OutputStream) localObject4).flush();
			((OutputStream) localObject4).close();

			localHttpURLConnection.connect();
			try {
				j = localHttpURLConnection.getResponseCode();

				if (j == 500) {
					i = 1;
				} else if (j / 100 != 2) {
					log.log(Level.SEVERE, "SAAJ0008.p2p.bad.response",
							new String[] { localHttpURLConnection
									.getResponseMessage() });

					throw new SOAPExceptionImpl("Bad response: (" + j
							+ localHttpURLConnection.getResponseMessage());
				}

			} catch (IOException localIOException) {
				j = localHttpURLConnection.getResponseCode();
				if (j == 500)
					i = 1;
				else {
					throw localIOException;
				}
			}
		} catch (SOAPException localSOAPException1) {
			throw localSOAPException1;
		} catch (Exception localException1) {
			log.severe("SAAJ0009.p2p.msg.send.failed");
			throw new SOAPExceptionImpl("Message send failed", localException1);
		}

		SOAPMessage localSOAPMessage = null;
		if ((j == 200) || (i != 0)) {
			try {
				localObject1 = new MimeHeaders();

				k = 1;
				while (true) {
					localObject2 = localHttpURLConnection.getHeaderFieldKey(k);
					localObject3 = localHttpURLConnection.getHeaderField(k);

					if ((localObject2 == null) && (localObject3 == null)) {
						break;
					}
					if (localObject2 != null) {
						localObject4 = new StringTokenizer(
								(String) localObject3, ",");

						while (((StringTokenizer) localObject4).hasMoreTokens())
							((MimeHeaders) localObject1).addHeader(
									(String) localObject2,
									((StringTokenizer) localObject4)
											.nextToken().trim());
					}
					++k;
				}
				if (((MimeHeaders) localObject1).getHeader("Content-Type")==null){
					((MimeHeaders) localObject1).addHeader("Content-Type", "text/xml; charset=UTF-8");
				}

				localObject4 = (i != 0) ? localHttpURLConnection
						.getErrorStream() : localHttpURLConnection
						.getInputStream();

				byte[] arrayOfByte = readFully((InputStream) localObject4);

				int l = (localHttpURLConnection.getContentLength() == -1) ? arrayOfByte.length
						: localHttpURLConnection.getContentLength();

				if (l == 0) {
					localSOAPMessage = null;
					log.warning("SAAJ0014.p2p.content.zero");
				} else {
					ByteInputStream localByteInputStream = new ByteInputStream(
							arrayOfByte, l);
					localSOAPMessage = this.messageFactory.createMessage(
							(MimeHeaders) localObject1, localByteInputStream);
				}

				((InputStream) localObject4).close();
				localHttpURLConnection.disconnect();
			} catch (SOAPException localSOAPException2) {
				throw localSOAPException2;
			} catch (Exception localException2) {
				log.log(Level.SEVERE, "SAAJ0010.p2p.cannot.read.resp",
						localException2);
				throw new SOAPExceptionImpl("Unable to read response: "
						+ localException2.getMessage());
			}
		}

		return ((SOAPMessage) (SOAPMessage) (SOAPMessage) (SOAPMessage) localSOAPMessage);
	}

	public SOAPMessage get(Object paramObject) throws SOAPException {
		if (this.closed) {
			log.severe("SAAJ0011.p2p.get.already.closed.conn");
			throw new SOAPExceptionImpl("Connection is closed");
		}
		Class localClass = null;
		try {
			localClass = Class.forName("javax.xml.messaging.URLEndpoint");
		} catch (Exception localException1) {
		}
		if ((localClass != null) && (localClass.isInstance(paramObject))) {
			String str = null;
			try {
				Method localMethod = localClass.getMethod("getURL",
						(Class[]) null);
				str = (String) localMethod.invoke(paramObject, (Object[]) null);
			} catch (Exception localException3) {
				log.severe("SAAJ0004.p2p.internal.err");
				throw new SOAPExceptionImpl("Internal error: "
						+ localException3.getMessage());
			}
			try {
				paramObject = new URL(str);
			} catch (MalformedURLException localMalformedURLException2) {
				log.severe("SAAJ0005.p2p.");
				throw new SOAPExceptionImpl("Bad URL: "
						+ localMalformedURLException2.getMessage());
			}

		}

		if (paramObject instanceof String) {
			try {
				paramObject = new URL((String) paramObject);
			} catch (MalformedURLException localMalformedURLException1) {
				log.severe("SAAJ0006.p2p.bad.URL");
				throw new SOAPExceptionImpl("Bad URL: "
						+ localMalformedURLException1.getMessage());
			}
		}

		if (paramObject instanceof URL)
			try {
				PriviledgedGet localPriviledgedGet = new PriviledgedGet(this,
						(URL) paramObject);
				SOAPMessage localSOAPMessage = (SOAPMessage) AccessController
						.doPrivileged(localPriviledgedGet);

				return localSOAPMessage;
			} catch (Exception localException2) {
				throw new SOAPExceptionImpl(localException2);
			}
		throw new SOAPExceptionImpl("Bad endPoint type " + paramObject);
	}

	SOAPMessage get(URL paramURL) throws SOAPException {
		int i = 0;

		URL localURL = null;
		HttpURLConnection localHttpURLConnection = null;

		int j = 0;
		Object localObject1;
		try {
			if (paramURL.getProtocol().equals("https")) {
				initHttps();
			}
			JaxmURI localJaxmURI = new JaxmURI(paramURL.toString());
			localObject1 = localJaxmURI.getUserinfo();

			localURL = paramURL;

			if ((!(localURL.getProtocol().equalsIgnoreCase("http")))
					&& (!(localURL.getProtocol().equalsIgnoreCase("https")))) {
				log.severe("SAAJ0052.p2p.protocol.mustbe.http.or.https");
				throw new IllegalArgumentException("Protocol "
						+ localURL.getProtocol() + " not supported in URL "
						+ localURL);
			}

			localHttpURLConnection = createConnection(localURL);

			localHttpURLConnection.setRequestMethod("GET");

			localHttpURLConnection.setDoOutput(true);
			localHttpURLConnection.setDoInput(true);
			localHttpURLConnection.setUseCaches(false);
			HttpURLConnection.setFollowRedirects(true);

			localHttpURLConnection.connect();
			try {
				j = localHttpURLConnection.getResponseCode();

				if (j == 500) {
					i = 1;
				} else if (j / 100 != 2) {
					log.log(Level.SEVERE, "SAAJ0008.p2p.bad.response",
							new String[] { localHttpURLConnection
									.getResponseMessage() });

					throw new SOAPExceptionImpl("Bad response: (" + j
							+ localHttpURLConnection.getResponseMessage());
				}

			} catch (IOException localIOException) {
				j = localHttpURLConnection.getResponseCode();
				if (j == 500)
					i = 1;
				else {
					throw localIOException;
				}
			}
		} catch (SOAPException localSOAPException1) {
			throw localSOAPException1;
		} catch (Exception localException1) {
			log.severe("SAAJ0012.p2p.get.failed");
			throw new SOAPExceptionImpl("Get failed", localException1);
		}

		SOAPMessage localSOAPMessage = null;
		if ((j == 200) || (i != 0)) {
			try {
				localObject1 = new MimeHeaders();

				int k = 1;
				while (true) {
					String str1 = localHttpURLConnection.getHeaderFieldKey(k);
					String str2 = localHttpURLConnection.getHeaderField(k);

					if ((str1 == null) && (str2 == null)) {
						break;
					}
					if (str1 != null) {
						StringTokenizer localObject2 = new StringTokenizer(str2, ",");

						while (((StringTokenizer) localObject2).hasMoreTokens())
							((MimeHeaders) localObject1).addHeader(str1,
									((StringTokenizer) localObject2)
											.nextToken().trim());
					}
					++k;
				}

				Object localObject2 = (i != 0) ? localHttpURLConnection
						.getErrorStream() : localHttpURLConnection
						.getInputStream();

				byte[] arrayOfByte = readFully((InputStream) localObject2);

				int l = (localHttpURLConnection.getContentLength() == -1) ? arrayOfByte.length
						: localHttpURLConnection.getContentLength();

				if (l == 0) {
					localSOAPMessage = null;
					log.warning("SAAJ0014.p2p.content.zero");
				} else {
					ByteInputStream localByteInputStream = new ByteInputStream(
							arrayOfByte, l);
					localSOAPMessage = this.messageFactory.createMessage(
							(MimeHeaders) localObject1, localByteInputStream);
				}

				((InputStream) localObject2).close();
				localHttpURLConnection.disconnect();
			} catch (SOAPException localSOAPException2) {
				throw localSOAPException2;
			} catch (Exception localException2) {
				log.log(Level.SEVERE, "SAAJ0010.p2p.cannot.read.resp",
						localException2);

				throw new SOAPExceptionImpl("Unable to read response: "
						+ localException2.getMessage());
			}
		}

		return ((SOAPMessage) (SOAPMessage) localSOAPMessage);
	}

	private byte[] readFully(InputStream paramInputStream) throws IOException {
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		byte[] arrayOfByte1 = new byte[1024];
		int i = 0;

		while ((i = paramInputStream.read(arrayOfByte1)) != -1) {
			localByteArrayOutputStream.write(arrayOfByte1, 0, i);
		}

		byte[] arrayOfByte2 = localByteArrayOutputStream.toByteArray();

		return arrayOfByte2;
	}

	private void initHttps() {
		String str = System.getProperty("java.protocol.handler.pkgs");
		log.log(Level.FINE, "SAAJ0053.p2p.providers", new String[] { str });

		if ((str == null) || (str.indexOf(SSL_PKG) < 0)) {
			if (str == null)
				str = SSL_PKG;
			else
				str = str + "|" + SSL_PKG;
			System.setProperty("java.protocol.handler.pkgs", str);
			log.log(Level.FINE, "SAAJ0054.p2p.set.providers",
					new String[] { str });
			try {
				Class localClass = Class.forName(SSL_PROVIDER);
				Provider localProvider = (Provider) localClass.newInstance();
				Security.addProvider(localProvider);
				log.log(Level.FINE, "SAAJ0055.p2p.added.ssl.provider",
						new String[] { SSL_PROVIDER });
			} catch (Exception localException) {
			}
		}
	}

	private void initAuthUserInfo(HttpURLConnection paramHttpURLConnection,
			String paramString) {
		if (paramString == null)
			return;
		int i = paramString.indexOf(58);
		String str1;
		String str2;
		if (i == -1) {
			str1 = ParseUtil.decode(paramString);
			str2 = null;
		} else {
			str1 = ParseUtil.decode(paramString.substring(0, i++));
			str2 = ParseUtil.decode(paramString.substring(i));
		}

		String str3 = str1 + ":";
		byte[] arrayOfByte1 = str3.getBytes();
		byte[] arrayOfByte2 = str2.getBytes();

		byte[] arrayOfByte3 = new byte[arrayOfByte1.length
				+ arrayOfByte2.length];

		System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
		System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte1.length,
				arrayOfByte2.length);

		String str4 = "Basic " + new String(Base64.encode(arrayOfByte3));
		paramHttpURLConnection.setRequestProperty("Authorization", str4);
	}

	private void d(String paramString) {
		log.log(Level.SEVERE, "SAAJ0013.p2p.HttpSOAPConnection",
				new String[] { paramString });

		System.err.println("HttpSOAPConnection: " + paramString);
	}

	private HttpURLConnection createConnection(URL paramURL) throws IOException {
		return ((HttpURLConnection) paramURL.openConnection());
	}

	static {
		if (isIBMVM) {
			SSL_PKG = "com.ibm.net.ssl.internal.www.protocol";
			SSL_PROVIDER = "com.ibm.net.ssl.internal.ssl.Provider";
		} else {
			SSL_PKG = "com.sun.net.ssl.internal.www.protocol";
			SSL_PROVIDER = "com.sun.net.ssl.internal.ssl.Provider";
		}
	}

	static class PriviledgedGet implements PrivilegedExceptionAction {
		HttpSOAPConnection c;
		URL endPoint;

		PriviledgedGet(HttpSOAPConnection paramHttpSOAPConnection, URL paramURL) {
			this.c = paramHttpSOAPConnection;
			this.endPoint = paramURL;
		}

		public Object run() throws Exception {
			return this.c.get(this.endPoint);
		}
	}

	static class PriviledgedPost implements PrivilegedExceptionAction {
		HttpSOAPConnection c;
		SOAPMessage message;
		URL endPoint;

		PriviledgedPost(HttpSOAPConnection paramHttpSOAPConnection,
				SOAPMessage paramSOAPMessage, URL paramURL) {
			this.c = paramHttpSOAPConnection;
			this.message = paramSOAPMessage;
			this.endPoint = paramURL;
		}

		public Object run() throws Exception {
			return this.c.post(this.message, this.endPoint);
		}
	}

	static class PriviledgedSetProxyAction implements PrivilegedExceptionAction {
		String proxyHost = null;
		int proxyPort = 0;

		PriviledgedSetProxyAction(String paramString, int paramInt) {
			this.proxyHost = paramString;
			this.proxyPort = paramInt;
		}

		public Object run() throws Exception {
			System.setProperty("http.proxyHost", this.proxyHost);
			System.setProperty("http.proxyPort",
					new Integer(this.proxyPort).toString());
			HttpSOAPConnection.log.log(Level.FINE, "SAAJ0050.p2p.proxy.host",
					new String[] { this.proxyHost });

			HttpSOAPConnection.log.log(Level.FINE, "SAAJ0051.p2p.proxy.port",
					new String[] { new Integer(this.proxyPort).toString() });

			return this.proxyHost;
		}
	}
}