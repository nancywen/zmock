package com.zf.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyHttpClient {

	static Logger log = LoggerFactory.getLogger(MyHttpClient.class);
	/**
	 * 发送post消息并返回对方回复的内容
	 * 
	 * @param url
	 * @param contentw
	 * @return
	 */
	public static String post(String url, String content) {
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
}
