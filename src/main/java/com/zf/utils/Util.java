package com.zf.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Wrapper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;


public class Util {
	
	final static Logger log = Logger.getLogger(Util.class);
	private static SAXReader xmlParser = new SAXReader();
	public static final Integer POST_TIMEOUT = 300; 

	/**
	 * 发送post消息并返回对方回复的内容
	 * 
	 * @param url
	 * @param content
	 * @return
	 */
	public static String post(String url, String content) {
		Integer timeoutInConf = POST_TIMEOUT;
		log.debug("TIMEOUT = " + timeoutInConf);
		final int TIMEOUT = 1000 * timeoutInConf;// 发送post消息的超时时间

		log.debug("url = " + url);
		log.debug("json = " + (content == null ? "null" : content.toString()));
		StringBuffer sb = new StringBuffer();
		OutputStreamWriter osw = null;
		BufferedReader in = null;
		try {
			URL urls = new URL(url);
			HttpURLConnection uc = (HttpURLConnection) urls.openConnection();
			uc.setRequestMethod("POST");
			uc.setRequestProperty("content-type", "application/json");
			// uc.setRequestProperty("content-type","application/x-www-form-urlencoded");
			uc.setRequestProperty("charset", "UTF-8");
			uc.setDoOutput(true);
			uc.setDoInput(true);
			uc.setReadTimeout(TIMEOUT);
			uc.setConnectTimeout(TIMEOUT);
			// 此处要对流指定字符集，否则服务端接收会有中文件乱码
			if (content != null) {
				osw = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
				osw.write(content);
				osw.flush();

			}

			// 接收返回消息时要指定流的字符集，否则会有中文乱码
			InputStream is = uc.getInputStream();
			if (is != null) {
				in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String readLine = "";
				while ((readLine = in.readLine()) != null) {
					sb.append(readLine);
				}
				in.close();
			}
			log.debug(">>>>>responseCode: " + uc.getResponseCode());
			if (!(uc.getResponseCode() + "").startsWith("2")) {
				return "{\"ResultCode\": -1}";
			}
		} catch (Exception e) {
			log.error("send post message to " + url + " error:", e);
			e.printStackTrace();
		} finally {
			try {
				if (osw != null) {
					osw.close();
				}

				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 发送Post消息，内容以application/xml的形式
	 * 
	 * @author faker
	 * @param url
	 * @param content
	 * @return
	 */
	public static String post4xml(String url, String content) {
		final int TIMEOUT = 1000 * 30;// 发送post消息的超时时间

		log.debug("url = " + url);
		log.debug("xml = " + (content == null ? "null" : content.toString()));
		StringBuffer sb = new StringBuffer();
		OutputStreamWriter osw = null;
		BufferedReader in = null;
		try {
			URL urls = new URL(url);
			HttpURLConnection uc = (HttpURLConnection) urls.openConnection();
			uc.setRequestMethod("POST");
			uc.setRequestProperty("content-type", "application/xml");
			// uc.setRequestProperty("content-type","application/x-www-form-urlencoded");
			uc.setRequestProperty("charset", "UTF-8");
			uc.setRequestProperty("Accept", "application/xml");
			uc.setDoOutput(true);
			uc.setDoInput(true);
			uc.setReadTimeout(TIMEOUT);
			uc.setConnectTimeout(TIMEOUT);
			// 此处要对流指定字符集，否则服务端接收会有中文件乱码
			if (content != null) {
				osw = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
				osw.write(content);
				osw.flush();

			}

			// 接收返回消息时要指定流的字符集，否则会有中文乱码
			InputStream is = uc.getInputStream();
			if (is != null) {
				in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String readLine = "";
				while ((readLine = in.readLine()) != null) {
					sb.append(readLine);
				}
				in.close();
			}
			log.info(">>>>>responseCode: " + uc.getResponseCode());
		} catch (Exception e) {
			log.error("send post message to " + url + " error:", e);
			e.printStackTrace();
		} finally {
			try {
				if (osw != null) {
					osw.close();
				}

				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 发送delete消息并返回对方回复的内容
	 * 
	 * @param url
	 * @param content
	 * @return
	 */
	public static String delete(String url, String content) {
		final int TIMEOUT = 1000 * 30;// 发送post消息的超时时间
		log.debug("url = " + url);
		log.debug("json = " + (content == null ? "null" : content.toString()));
		String ret = null;
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			HttpEntityEnclosingRequestBase delete = new HttpEntityEnclosingRequestBase() {
				@Override
				public String getMethod() {
					// TODO Auto-generated method stub
					return "DELETE";
				}
			};
			if (content == null) {
				content = "";
			}
			delete.setURI(URI.create(url));
			delete.setEntity(new StringEntity(content, "UTF-8"));
			delete.addHeader("Content-Type", "application/json");
			HttpResponse response = httpClient.execute(delete);
			ret = EntityUtils.toString(response.getEntity(), "UTF-8");

		} catch (Exception e) {
			log.error("send delete message to " + url + " error:", e);
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null) {
					httpClient.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return ret;
	}

	/**
	 * 发送get消息并返回对方回复的内容
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url) {
		final int TIMEOUT = 1000 * 30;// 发送get消息的超时时间

		log.debug("url = " + url);
		StringBuffer sb = new StringBuffer();
		OutputStreamWriter osw = null;
		BufferedReader in = null;
		try {
			URL urls = new URL(url);
			HttpURLConnection uc = (HttpURLConnection) urls.openConnection();
			uc.setRequestMethod("GET");
			uc.setRequestProperty("charset", "UTF-8");
			uc.setDoOutput(true);
			uc.setDoInput(true);
			uc.setReadTimeout(TIMEOUT);
			uc.setConnectTimeout(TIMEOUT);

			// 接收返回消息时要指定流的字符集，否则会有中文乱码
			in = new BufferedReader(new InputStreamReader(uc.getInputStream(),
					"UTF-8"));
			String readLine = "";
			while ((readLine = in.readLine()) != null) {
				sb.append(readLine);
			}
			in.close();
		} catch (Exception e) {
			log.error("send get message to " + url + " error:", e);
			e.printStackTrace();
		} finally {
			try {
				if (osw != null) {
					osw.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 判断给定的两个字符串是否相等,null与空串认为是相等，即使空串有多个空格
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean equals(String str1, String str2) {
		String a = (str1 == null || str1.trim().equals("")) ? null : str1;
		String b = (str2 == null || str2.trim().equals("")) ? null : str2;

		if (a == null) {
			if (b == null) {
				return true;
			}
		} else {
			if (b != null && a.equals(b)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断给定的两个Map是否相等
	 */
	public static boolean equals(Map<?, ?> map1, Map<?, ?> map2) {
		if (map1 == null && map2 == null) {
			return true;
		}

		if (map1 != null && map2 != null && map1.size() == map2.size()) {
			for (Object k : map1.keySet()) {
				if (!map2.containsKey(k)) {
					return false;
				}
			}
			for (Object v : map1.values()) {
				if (!map2.containsValue(v)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	
	/**
	 * 由开始时间和结束时间，计算时长
	 * 
	 * @param startTime
	 *            开始时间，如：02:21:00
	 * @param endTime
	 *            开始时间，如：02:26:19
	 * @return
	 * @throws ParseException
	 */
	public static String calLengthByStartEnd(String startTime, String endTime) throws ParseException {
		Date start = FsvDateFormatUtils.parse(startTime,"HH:mm:ss");
		Date end = FsvDateFormatUtils.parse(endTime,"HH:mm:ss");
		long length = (end.getTime() / 1000) - (start.getTime() / 1000);
		long hour = length / 3600;
		long minute = (length % 3600) / 60;
		long second = length % 60;
		return new StringBuilder().append(hour < 10 ? "0" + hour : hour).append(minute < 10 ? "0" + minute : minute).append(second < 10 ? "0" + second : second).append("00").toString();
	}
	
	/**
	 * 指定时间转换成UTC时间
	 * 
	 * @param time
	 * @return
	 */
	public static String getUTCTime(String time){
		String pattern = "yyyy-MM-dd HH:mm:ss";
		Date time_date = null;
		try {
			time_date = FsvDateFormatUtils.parse(time,pattern);
			return FsvDateFormatUtils.formatUTC(time_date, pattern);
		} catch (Exception e) {
			log.error("",e);
			return null;
		}
		
		
	
	}
	

	
	/**
	 * 读取指定的文件，返因sql语句的集合
	 * 
	 * @param fileName
	 * @return <li>sql语句仅支持单行整行注释，可采用#、-- （单行双长划线）两种注释风格，双长划线后必须有空格，不支持多行及行尾注释
	 *         <li>sql语句支持换行，且sql语句要以分号结尾，分号必须在行尾
	 */
	public static List<String> getSqlList(String fileName) {
		log.debug("begin read file:" + fileName);
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			log.error("sql script file: " + fileName + " not exist");
			return null;
		}
		BufferedReader reader = null;

		List<String> sqlList = new LinkedList<String>();

		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			StringBuilder sql = new StringBuilder();
			String tempLine = null;
			// int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempLine = reader.readLine()) != null) {
				if (tempLine != null && !"".equals(tempLine.trim())
						&& !tempLine.trim().startsWith("#")
						&& !tempLine.trim().startsWith("-- ")) {
					sql.append(tempLine);
					// 如果以分号结尾，则说明该句sql语句结束
					if (tempLine.trim().endsWith(";")) {
						// 去除最后面的分号并加到list中
						sqlList.add(sql.toString().substring(0,
								sql.toString().indexOf(";")));
						sql = new StringBuilder();
					} else {
						sql.append("\r\n");
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			log.error("", e);
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return sqlList;
	}
	
	public static <T extends Wrapper> void  closeStmtOrConnctn(T... objs) {
		for (T t : objs) {
			if (t != null) {
				try {
					if (t instanceof Connection) {
						((Connection)t).close();
					} else
					if (t instanceof Statement) {
						((Statement)t).close();
					} else
					if (t instanceof PreparedStatement) {
						((PreparedStatement) t).close();
					}
				} catch (Exception e) {
					log.error("handle exception: ", e);
				}
			}
		}
	}

	/**
	 * 发送告警
	 * 
	 * @param ProbableCause
	 *            可能原因
	 * @param specificReason
	 *            特殊原因
	 * @param severity
	 *            告警级别
	 * @param message
	 *            告警信息
	 */
	public static void sendAlarm(String ProbableCause, String specificReason,
			String severity, String message) {
		final String targetIp = "127.0.0.1";// 发送消息到本地

		String sendStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message><MessageType>alarm</MessageType><ProbableCause>"
				+ ProbableCause
				+ "</ProbableCause><SpecificReason>"
				+ specificReason
				+ "</SpecificReason><CreateTime>"
				+ getCurTime()
				+ "</CreateTime><EntityType>app</EntityType><EntityInstance>CMS</EntityInstance><Severity>"
				+ severity
				+ "</Severity><Message>"
				+ message
				+ "</Message></message>";
		byte[] sendDataByte = getEncodeStr(sendStr, "UTF-8").getBytes();
		DatagramSocket datagramSocket = null;
		try {
			DatagramPacket dataPacket = new DatagramPacket(sendDataByte,
					sendDataByte.length, InetAddress.getByName(targetIp), 8004);
			datagramSocket = new DatagramSocket();
			datagramSocket.send(dataPacket);

			log.info("sendAlarm:" + sendStr + " to " + targetIp + " success");
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if (datagramSocket != null) {
				datagramSocket.close();
			}
		}
	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	public static String getCurTime() {
		GregorianCalendar gc = new GregorianCalendar();

		String mDateTime = FsvDateFormatUtils.formatDefault(gc.getTime());
		return mDateTime;
	}

	public static String getBeforeTime(int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - hour);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(hour + "小时前的时间：" + df.format(calendar.getTime()));
		System.out.println("当前的时间：" + df.format(new Date()));

		return df.format(calendar.getTime());
	}

	public static String getEncodeStr(String theString, String charSet) {
		String decodeStr;
		try {
			decodeStr = new String(theString.getBytes(charSet));
		} catch (UnsupportedEncodingException e) {
			decodeStr = new String(theString);
		}
		return decodeStr;

	}

	/**
	 * 发送SAOP消息
	 * 
	 * @param sendMsgURL
	 *            对端URL
	 * @param localName
	 *            对端方法名
	 * @param prefix
	 *            前缀
	 * @param uri
	 * @param paramMap
	 *            消息参数，支持单级和多级。例如， 单级：<ContentID>test1</ContentID>， 多级：
	 *            <ContentDesc> <ContentID>test1</ContentID> </ContentDesc>
	 * @return 消息是否发送成功
	 */
	public static SOAPBody sendSoapMessage(String sendMsgURL, String localName,
			String prefix, String uri, Map<String, Object> paramMap) {
		SOAPConnection connection = null;
		try {
			// 创建连接
			// ==================================================
			// SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory
			// .newInstance();
			// SOAPConnection connection = soapConnFactory.createConnection();
			connection = new HttpSOAPConnection();

			// 创建消息对象
			// ===========================================
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage message = messageFactory.createMessage();
			message.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");

			MimeHeaders headers = message.getMimeHeaders();
			headers.addHeader("SOAPAction", "");

			// 创建soap消息主体==========================================
			SOAPPart soapPart = message.getSOAPPart();// 创建soap部分
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.removeChild(envelope.getHeader());
			SOAPBody body = envelope.getBody();

			// 构建方法和参数
			SOAPElement bodyElement = body.addChildElement(envelope.createName(
					localName, prefix, uri));
			StringBuilder paramMapStr = new StringBuilder();
			// 通过递归支持多级参数
			addChildElement(paramMap, bodyElement, paramMapStr);
			// Save the message
			message.saveChanges();

			// 打印客户端发出的soap报文
			log.info("send message to " + paramMapStr.toString() + " URL="
					+ sendMsgURL);

			URL url = new URL(new URL(sendMsgURL), sendMsgURL,
					new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(URL url)
								throws IOException {
							URL target = new URL(url.toString());
							URLConnection connection = target.openConnection();
					// 设置连接超时
							connection.setConnectTimeout(10000); // 10 sec
							connection.setReadTimeout(60000); // 1 min
							return (connection);
						}
					});

			// 响应消息
			SOAPMessage reply = connection.call(message, url);
			// 将相应的消息转换为String，打印到日志中
			Source source = reply.getSOAPPart().getContent();
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			ByteArrayOutputStream myOutStr = new ByteArrayOutputStream();
			StreamResult res = new StreamResult();
			res.setOutputStream(myOutStr);
			transformer.transform(source, res);
			String result = myOutStr.toString("UTF-8");

			log.info("recieve message from  " + paramMapStr.toString()
					+ " Result=" + result);

			return reply.getSOAPBody();
		} catch (Exception e) {
			log.error("", e);
			return null;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SOAPException e) {
				}
			}
		}

	}

	/**
	 * 通过递归支持多级参数
	 * 
	 * @param paramMap
	 * @param bodyElement
	 * @param paramMapStr
	 * @throws SOAPException
	 */
	@SuppressWarnings("unchecked")
	private static void addChildElement(Map<String, Object> paramMap,
			SOAPElement bodyElement, StringBuilder paramMapStr)
			throws SOAPException {
		String key = "";
		Object value = null;
		for (Entry<String, Object> entry : paramMap.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (value instanceof String) {
				bodyElement.addChildElement(key).addTextNode((String) value);
				paramMapStr.append(key).append("=").append((String) value)
						.append(" ");
			} else {
				SOAPElement childElement = bodyElement.addChildElement(key);
				addChildElement((Map<String, Object>) value, childElement,
						paramMapStr);
			}
		}
	}

	private static Object getValueByReflect(Class clz, Object obj, String name)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field field = clz.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(obj);
	}

	private static void setValueByReflect(Class clz, Object obj, String name,
			Object value) throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field field = clz.getDeclaredField(name);
		field.setAccessible(true);
		field.set(obj, value);
	}

	private static Object excuteByReflect(Class clz, Object obj, String name,
			Class[] clzParams, Object[] params) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (clzParams == null || clzParams.length == 0) {
			Method method = clz.getDeclaredMethod(name);
			method.setAccessible(true);
			return method.invoke(obj);
		} else {
			Method method = clz.getDeclaredMethod(name, clzParams);
			method.setAccessible(true);
			return method.invoke(obj, params);
		}
	}

	private static Object newInstanceByReflect(Class clz, Class[] clzParams,
			Object[] params) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Constructor c = clz.getConstructor(clzParams);
		c.setAccessible(true);
		return c.newInstance(params);

	}

	/**
	 * 流的复制，自动flush和关闭流
	 * 
	 * @param in
	 * @param out
	 * @param bufferSize
	 * @return 传输的Byte
	 * @throws IOException
	 */
	public static int copyStream(InputStream in, OutputStream out,
			int bufferSize) throws IOException {
		byte[] buff = new byte[bufferSize];
		int total = 0;
		while (true) {
			int len = in.read(buff);
			if (len > 0) {
				out.write(buff, 0, len);
				out.flush();
				total += len;
			} else {
				break;
			}
		}
		return total;

	}


	/**
	 * 下载文件
	 */
	public static String downloadFile(String ftpURL, String localPath,
			String reg) {
		if (ftpURL != null) {
			Pattern p = Pattern.compile(reg);
			Matcher m = p.matcher(ftpURL);
			if (!m.find()) {
				log.error("adiXmlUrl("
						+ ftpURL
						+ ") url format is not correct, "
						+ "correct format:ftp://ftpuser:ftpuser@1.1.1.1:21/a/b.csv");
				return null;
			}

			String userName = m.group(1);
			String password = m.group(2);
			String hostAndPort = m.group(3);
			int port = 21;
			String host = hostAndPort;

			if (hostAndPort != null && hostAndPort.indexOf(":") > -1) {
				host = hostAndPort.substring(0, hostAndPort.indexOf(":"));
				String portStr = hostAndPort
						.substring(hostAndPort.indexOf(":") + 1);
				try {
					port = Integer.parseInt(portStr);
				} catch (NumberFormatException e) {
					log.warn("port (" + port
							+ ") format error,now set default value:21");
				}
			}
			String remotePath = m.group(4);
			String fileName = m.group(6);

			log.info("ftphost=" + host + ",ftpuser=" + userName + ",port="
					+ port + ",ftpRemotePath=" + remotePath + ",fileName="
					+ fileName);

			try {
				FTPUtil ftpUtil = new FTPUtil(host, port, userName, password);
				return ftpUtil.downFile(remotePath, fileName, localPath);// 下载成功后返回文件的本地全路径
			} catch (Exception e) {
				log.error("", e);
			}
		}

		return null;
	}


	/**
	 * 发送post消息并返回对方回复的内容
	 * 
	 * @param url
	 * @param content
	 * @return
	 */
	public static String post4Tcgs(String url, String content) {
		final int TIMEOUT = 1000 * 30;// 发送post消息的超时时间

		log.debug("url = " + url);
		log.debug("json = " + (content == null ? "null" : content.toString()));
		StringBuffer sb = new StringBuffer();
		OutputStreamWriter osw = null;
		BufferedReader in = null;
		try {
			URL urls = new URL(url);
			HttpURLConnection uc = (HttpURLConnection) urls.openConnection();
			uc.setRequestMethod("POST");
			uc.setRequestProperty("content-type", "application/json");
			// uc.setRequestProperty("content-type","application/x-www-form-urlencoded");
			uc.setRequestProperty("charset", "UTF-8");
			uc.setDoOutput(true);
			uc.setDoInput(true);
			uc.setReadTimeout(TIMEOUT);
			uc.setConnectTimeout(TIMEOUT);
			// 此处要对流指定字符集，否则服务端接收会有中文件乱码
			if (content != null) {
				osw = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
				osw.write(content);
				osw.flush();

			}

			// 接收返回消息时要指定流的字符集，否则会有中文乱码
			InputStream is = uc.getInputStream();
			if (is != null) {
				in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String readLine = "";
				while ((readLine = in.readLine()) != null) {
					sb.append(readLine);
				}
				in.close();
			}
			log.info(">>>>>responseCode: " + uc.getResponseCode());
			if (uc.getResponseCode() == 200) {
				return "{\"ResultCode\": 0}";
			}
		} catch (Exception e) {
			log.error("send post message to " + url + " error:", e);
			e.printStackTrace();
		} finally {
			try {
				if (osw != null) {
					osw.close();
				}

				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}


	/**
	 * 获取URL，统一设置连接超时20S,ReadTimeout 1min
	 * 
	 * @param sendMsg2CSPURL
	 * @return
	 * @throws MalformedURLException
	 */
	public static URL getUrl(String sendMsg2CSPURL) throws MalformedURLException{
		URL url = new URL(new URL(sendMsg2CSPURL),"",new URLStreamHandler() {
			   @Override
			   protected URLConnection openConnection(URL url) throws IOException {
			    URL target = new URL(url.toString());
			    URLConnection connection = target.openConnection();
			    // Connection settings
			    connection.setConnectTimeout(20000); // 20 sec
			    connection.setReadTimeout(20000); // 20sec
			    return(connection);
			   }
			   });
		return url;
	}
	

	/**
	 * 
	 * @param url
	 * @param content
	 * @return 是否发送成功
	 */
	public static boolean sendMsg(String url, String content) {
		DataOutputStream out = null;
		log.debug("begin send message(" + content + ") to " + url);
		try {
			URL distURL = new URL(url);
			HttpURLConnection urlConn = (HttpURLConnection) distURL
					.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setReadTimeout(30000);
			urlConn.setRequestMethod("POST");
			urlConn.setUseCaches(false);
			urlConn.setInstanceFollowRedirects(true);
			urlConn.setRequestProperty("Content-Type",
					"application/json;charset=UTF-8");
			out = new DataOutputStream(urlConn.getOutputStream());
			out.writeBytes(content);
			out.flush();
			int status = urlConn.getResponseCode();
			if (status == 200) {
				return true;
			}
			log.debug("send message(" + content + ") to " + url + " fail");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
