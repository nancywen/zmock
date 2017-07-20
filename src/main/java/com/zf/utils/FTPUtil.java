package com.zf.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ftp工具类
 * 
 * @author ding 2014-05-28
 */
public class FTPUtil {
//	private final static Logger log = Logger.getLogger(FTPUtil.class);
	private static final Logger log = LoggerFactory.getLogger(FTPUtil.class);

	private String host;
	private int port;
	private String userName;
	private String password;
	// 连接超时时间，默认10秒，可以在配置文件中配置
	private static int connectTimeout = 10000;
	private static int ftpBufferSize = 1024 * 512;
	private static final String defaultEncoding = "UTF-8";
	public static final String FTP_USERNAME = "userName";
	public static final String FTP_PWD = "password";
	public static final String FTP_HOST = "host";
	public static final String FTP_PORT = "port";
	public static final String FTP_PATH = "path";
	public static final String FTP_FILENAME = "fileName";

	/**
	 * 在构造函数中初始化必要的成员变量，如一些不能为空的变更值为空则抛出异常
	 * 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param log
	 * @throws Exception
	 */
	public FTPUtil(String host, int port, String userName, String password)
			throws Exception {
		if (host == null || "".equals(host.trim())) {
			log.error("host is null or empty");
			throw new Exception("parameter(host) is null or empty");
		}

		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
	}

	public static Map<String, String> parseFtpUrl(String fileUrl, Logger log) {

		if (StringUtils.isEmpty(fileUrl)) {
			return null;
		}

		Map<String, String> map = new HashMap<String, String>();

		try {

			map = new HashMap<String, String>();
			String[] params = fileUrl.split("/");

			String userName = params[2].substring(0, params[2].indexOf(":"));
			map.put(FTP_USERNAME, userName);

			String password = params[2].substring(params[2].indexOf(":") + 1,
					params[2].indexOf("@"));
			map.put(FTP_PWD, password);

			// 判断是否有端口
			boolean portExist = false;
			String[] hostparams = params[2].split(":");
			if (hostparams.length == 3) {
				portExist = true;
			}
			String host = "";
			String port = "";
			if (portExist) {
				host = params[2].substring(params[2].indexOf("@") + 1,
						params[2].lastIndexOf(":"));
				port = params[2].substring(params[2].lastIndexOf(":") + 1);

			} else {
				host = params[2].substring(params[2].indexOf("@") + 1);
				port = "21";
			}
			map.put(FTP_HOST, host);
			map.put(FTP_PORT, port);

			StringBuilder path = new StringBuilder(128);
			for (int j = 3; j < params.length - 1; j++) {
				// 去除第一个“/”，避免因FTP权限设置问题，某些情况下会引起目录匹配错误的问题
				if (j == 3) {
					path.append(params[j]);
				} else {
					path.append("/").append(params[j]);
				}

			}
			map.put(FTP_PATH, path.toString());

			String fileName = params[params.length - 1];
			map.put(FTP_FILENAME, fileName);

		} catch (Exception e) {
			log.error("parse ftp url error : {}" , e.getMessage());
			log.error(" {}", e);
			return null;
		}

		return map;
	}

	/**
	 * 上传文件
	 * 
	 * @param remotePath
	 *            ftp路径
	 * @param fileName
	 *            文件名称
	 * @param is
	 *            本地文件输入流
	 * @return 上传成功返回true，否则返回false
	 */
	public boolean uploadFile(String remotePath, String fileName, InputStream is) {
		boolean success = false;
		FTPClient ftp = null;

		/*********** check parameter ************/
		if (fileName == null || "".equals(fileName.trim())) {
			log.error("fileName is null or empty");
			return false;
		}
		if (is == null) {
			log.error("inputStream is null or empty");
			return false;
		}

		try {
			ftp = new FTPClient();
			// 打印出ftp的指令
			// ftp.addProtocolCommandListener(new PrintCommandListener(new
			// PrintWriter(System.out)));

			ftp.connect(host, port);// 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
			ftp.setControlEncoding("GBK");// 下面三行代码必须要，而且不能改变编码格式，否则不能正确下载中文文件
			FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
			conf.setServerLanguageCode("zh");

			// 登录ftp
			if (!ftp.login(userName, password)) {
				log.error(String.format("log in ftp server %s fail {}", host));
				return false;
			}

			ftp.enterLocalActiveMode();// 设置被动模式
			ftp.setFileType(FTP.BINARY_FILE_TYPE);// 设置以二进制方式传输

			int reply = ftp.getReplyCode();// 看返回的值是不是230，如果是，表示登陆成功
			// 以2开头的返回值就会为真
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				log.error("connect FTP server {}" , host , "fail,status code {}"
						, reply);
				return false;
			}

			// 只要涉及到文件名及路径都要进行转码，否则会出现中文乱码
			String currentDir = ftp.printWorkingDirectory();// ftp刚登录时当前路径在ftp根目录下
			log.debug("current work path is:{}" , currentDir);

			boolean b = ftp.changeWorkingDirectory(currentDir + "/"
					+ new String(remotePath.getBytes("GB2312"), "iso-8859-1"));

			// 转到指定上传目录,如果不能成功转到指定的目录，则说明该目录不存在，创建之
			if (!b) {
				// ftp.makeDirectory(s)只能一次创建一级目录，不能一次创建多条目录，
				// 故需分步创建目录并转到转向当前目录到新创建的目录
				log.debug("ftp path not exist,now create it...");
				String[] ss = remotePath.split("/");
				for (String s : ss) {
					String tempDir = ftp.printWorkingDirectory();
					ftp.makeDirectory(new String(s.getBytes("GB2312"),
							"iso-8859-1"));
					ftp.changeWorkingDirectory(new String((tempDir + "/" + s)
							.getBytes("GB2312"), "iso-8859-1"));
					tempDir = ftp.printWorkingDirectory();
				}
			}
			currentDir = ftp.printWorkingDirectory();// ftp刚登录时当前路径在ftp根目录下
			log.debug("current work path is:{}" , currentDir);

			ftp.setFileType(FTP.BINARY_FILE_TYPE);// 如果缺省该句 传输txt正常
													// 但图片和其他格式的文件传输出现乱码
			success = ftp.storeFile(new String(fileName.getBytes("GB2312"),
					"iso-8859-1"), is);

			boolean b2 = ftp.changeWorkingDirectory(currentDir);// 回到先前目录
			log.debug("back old workingDirectory :{}" , b2);
		} catch (IOException e) {
			log.error("can not upload file:{}" , e.getMessage());
			log.error(" {}", e);
		} finally {
			try {
				if (ftp.isConnected()) {
					ftp.logout();
					ftp.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * 删除
	 * 
	 * @param url
	 * @param port
	 * @param username
	 * @param password
	 * @param path
	 *            :路径
	 * @param filename
	 *            :要上传的文件
	 * @return
	 */
	/*
	 * public boolean deleteFile(String url, int port, String username, String
	 * password, String path, String filename) { boolean success = false;
	 * FTPClient ftp = new FTPClient(); try { String filename2 = new
	 * String(filename.getBytes("GBK"), "ISO-8859-1"); String path1 = new
	 * String(path.getBytes("GBK"), "ISO-8859-1"); // 转到指定上传目录
	 * ftp.changeWorkingDirectory(path1); ftp.deleteFile(filename2);
	 * ftp.logout(); success = true; } catch (IOException e) {
	 * logger.error("delete file error:"+e.getMessage()); } return success; }
	 */

	/**
	 * 下载程序 此方法由于时间关系没有验证
	 */

	/*
	 * public boolean downFile(String ftpServer, int port, String username,
	 * String password, String remotePath, String fileName, OutputStream
	 * outputStream) { boolean success = false; FTPClient ftp = new FTPClient();
	 * try { int reply; ftp.connect(ftpServer, port); // 下面三行代码必须要，而且不能改变编码格式
	 * ftp.setControlEncoding("GBK"); FTPClientConfig conf = new
	 * FTPClientConfig(FTPClientConfig.SYST_NT);
	 * conf.setServerLanguageCode("zh");
	 * 
	 * // 如果采用默认端口，可以使用ftp.connect(url) 的方式直接连接FTP服务器 ftp.login(username,
	 * password);// 登录 ftp.setFileType(FTPClient.BINARY_FILE_TYPE); reply =
	 * ftp.getReplyCode(); if (!FTPReply.isPositiveCompletion(reply)) {
	 * ftp.disconnect(); return success; }
	 * 
	 * ftp.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
	 * 
	 * FTPFile[] fs = ftp.listFiles(); // 得到目录的相应文件列表
	 * System.out.println(fs.length);// 打印列表长度 for (int i = 0; i < fs.length;
	 * i++) { FTPFile ff = fs[i]; if (ff.getName().equals(fileName)) { String
	 * filename = fileName; // 这个就就是弹出下载对话框的关键代码
	 * 
	 * // response.setHeader( "Content-disposition", // "attachment;filename=" +
	 * // URLEncoder.encode(filename, "utf-8"));
	 * 
	 * // 将文件保存到输出流outputStream中 ftp.retrieveFile(new
	 * String(ff.getName().getBytes("GBK"), "ISO-8859-1"), outputStream);
	 * outputStream.flush(); outputStream.close(); } }
	 * 
	 * ftp.logout(); success = true; } catch (IOException e) {
	 * e.printStackTrace(); } finally { if (ftp.isConnected()) { try {
	 * ftp.disconnect(); } catch (IOException ioe) { } } } return success; }
	 */

	// 判断是否有重名方法
	// 此方法未验证
	/*
	 * public boolean isDirExist(String fileName, FTPFile[] fs) { for (int i =
	 * 0; i < fs.length; i++) { FTPFile ff = fs[i]; if
	 * (ff.getName().equals(fileName)) { return true; // 如果存在返回 正确信号 } } return
	 * false; // 如果不存在返回错误信号 }
	 */

	// 根据重名判断的结果 生成新的文件的名称
	// 此方法未验证
	/*
	 * public String changeName(String filename, FTPFile[] fs) { int n = 0;
	 * //创建一个可变的字符串对象 即StringBuffer对象，把filename值付给该对象 StringBuffer filename1 =
	 * new StringBuffer(""); filename1 = filename1.append(filename);
	 * System.out.println(filename1); while
	 * (isDirExist(filename1.toString(),fs)) { n++; String a = "[" + n + "]";
	 * System.out.println("字符串a的值是：" + a); int b = filename1.lastIndexOf(".");//
	 * 最后一出现小数点的位置 int c = filename1.lastIndexOf("[");// 最后一次"["出现的位置 if (c < 0)
	 * { c = b; } StringBuffer name = new StringBuffer(filename1.substring(0,
	 * c));// 文件的名字 StringBuffer suffix = new StringBuffer(filename1.substring(b
	 * + 1));//后缀的名称 filename1 = name.append(a).append(".").append(suffix); }
	 * return filename1.toString(); }
	 */

	/**
	 * 从ftp服务器上载文件
	 * 
	 * @param remotePath
	 *            相对ftp服务器的目录
	 * @param fileName
	 *            文件的名称
	 * @param localPath
	 *            本地保存目录
	 * @return 返回文件在本地的全路径
	 */
	public String downFile(String remotePath, String fileName, String localPath) {
		FTPClient ftp = new FTPClient();

		// 当目录不存在应先创建
		File localDir = new File(localPath);
		if (!localDir.exists()) {
			localDir.mkdirs();
		}

		FileOutputStream out = null;
		try {
			int reply;
			ftp.setBufferSize(ftpBufferSize);
			ftp.setConnectTimeout(connectTimeout);
			ftp.setDefaultTimeout(connectTimeout);
			ftp.setDataTimeout(connectTimeout);
			ftp.connect(host, port);
			ftp.setControlEncoding("UTF-8");
			// ftp.setFileType(FTP.BINARY_FILE_TYPE);
			// FTPClientConfig conf = new
			// FTPClientConfig(FTPClientConfig.SYST_NT);
			// conf.setServerLanguageCode("zh"); //
			// 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
			ftp.login(userName, password);// 登录

			ftp.enterLocalPassiveMode();
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				log.error("Failed to login ftp, code:{}" , reply);
				return null;
			}

			if (remotePath == null || "".equals(remotePath.trim())) {
				// 若为/则切换到了系统根目录
				remotePath = "./";
			}

			boolean isSuccessful = ftp.changeWorkingDirectory(new String(
					remotePath.getBytes("UTF-8"), "iso-8859-1"));// 转移到FTP服务器目录
			// FTP服务器/etc/vsftpd/vsftpd.conf文件中，chroot_list_enable=YES
			// 用户登录后不能切换到自己目录以外的其它目录。
			// 如果设置chroot_list_enable=YES,那么只允许/etc/vsftpd.chroot_list中列出的用户具有该功能.
			// 如果希望所有的本地用户都执行者chroot,可以增加一行:chroot_local_user=YES
			// 设置chroot_local_user=YES和不设置时，对remotePath是否以“/”开头有不同要求。
			if (!isSuccessful) {
				if (remotePath.startsWith("/")) {
					ftp.changeWorkingDirectory(new String(remotePath.substring(
							1).getBytes("UTF-8"), "iso-8859-1"));
				} else {
					ftp.changeWorkingDirectory(new String(("/" + remotePath)
							.getBytes("UTF-8"), "iso-8859-1"));
				}
			}

			String _localPatn = localPath.endsWith(File.separator) ? localPath
					: localPath + File.separator;

			File localFile = new File(_localPatn + fileName);

			out = new FileOutputStream(localFile);
			boolean result=
			ftp.retrieveFile(new String(fileName.getBytes("UTF-8"),
					"iso-8859-1"), out);
			out.flush();
			if(result){
				fileName = _localPatn + fileName;
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (ftp.isConnected()) {
				try {
					ftp.logout();
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return fileName;
	}

	/**
	 * 从ftp服务器上载图片
	 * 
	 * @param remotePath
	 *            相对ftp服务器的目录
	 * @param fileName
	 *            文件的名称
	 * @param localPath
	 *            本地保存目录
	 * @return 返回文件在本地的全路径
	 */
	public String downImage(String remotePath, String fileName, String localPath) {
		FTPClient ftp = new FTPClient();

		// 当目录不存在应先创建
		File localDir = new File(localPath);
		if (!localDir.exists()) {
			localDir.mkdirs();
		}

		InputStream in = null;
		OutputStream out = null;
		try {
			ftp.setConnectTimeout(connectTimeout);
			ftp.connect(host, port);
			ftp.login(userName, password);// 登录
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.setControlEncoding(defaultEncoding);
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return null;
			}

			if (remotePath == null || "".equals(remotePath.trim())) {
				// 若为/则切换到了系统根目录
				remotePath = "./";
			}

			boolean isSuccessful = ftp.changeWorkingDirectory(new String(
					remotePath.getBytes("UTF-8"), "iso-8859-1"));// 转移到FTP服务器目录
			// FTP服务器/etc/vsftpd/vsftpd.conf文件中，chroot_list_enable=YES
			// 用户登录后不能切换到自己目录以外的其它目录。
			// 如果设置chroot_list_enable=YES,那么只允许/etc/vsftpd.chroot_list中列出的用户具有该功能.
			// 如果希望所有的本地用户都执行者chroot,可以增加一行:chroot_local_user=YES
			// 设置chroot_local_user=YES和不设置时，对remotePath是否以“/”开头有不同要求。
			if (!isSuccessful) {
				if (remotePath.startsWith("/")) {
					ftp.changeWorkingDirectory(new String(remotePath.substring(
							1).getBytes("UTF-8"), "iso-8859-1"));
				} else {
					ftp.changeWorkingDirectory(new String(("/" + remotePath)
							.getBytes("UTF-8"), "iso-8859-1"));
				}
			}
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (ff.getName().equals(fileName)) {
					fileName = localPath + "/" + ff.getName();
					out = new FileOutputStream(fileName);
					in = ftp.retrieveFileStream(new String(ff.getName()
							.getBytes("UTF-8"), "iso-8859-1"));
					int total = Util.copyStream(in, out, ftpBufferSize);
					log.debug("down image ,size = {},fileName= {}", total,
							fileName);
					;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error(" {}", e);
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.flush();
					out.close();
					out = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				if (ftp.isConnected()) {
					ftp.logout();
					ftp.disconnect();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return fileName;
	}

	/**
	 * 下载文件，返回指定本地存储地址
	 * @param ftpFileUrl
	 * @param localPath
	 * @return
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public static String downFile(String ftpFileUrl, String localPath)
			throws NumberFormatException, Exception {
		Map<String, String> fileFtpInfoMap = FTPUtil.parseFtpUrl(ftpFileUrl,
				log);
		if (null == fileFtpInfoMap) {// url解析出错
			return null;
		}
		FTPUtil downloadFileFtp = new FTPUtil(
				fileFtpInfoMap.get(FTP_HOST),
				Integer.parseInt(fileFtpInfoMap.get(FTP_PORT)),
				fileFtpInfoMap.get(FTP_USERNAME),
				fileFtpInfoMap.get(FTP_PWD));

		// 从对方Ftp下载文件到本地
		String localXmlFilePath = downloadFileFtp.downFile(
				fileFtpInfoMap.get(FTP_PATH),
				fileFtpInfoMap.get(FTP_FILENAME), localPath);

		return fileFtpInfoMap.get(FTP_FILENAME).equals(
				localXmlFilePath) ? null : localXmlFilePath;
	}
}