package com.zf.utils;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class StringUtils {
	/**
	 * 将对象转换成json字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String obj2Json(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
		StringWriter writer = new StringWriter();
		JsonGenerator json = null;
		String re = null;
		try {
			json = new JsonFactory().createJsonGenerator(writer);
			mapper.writeValue(json, obj);
			re = writer.toString();
		} catch (Exception e) {
			System.out.println("tojsonerror." + e);
		} finally {
			try {
				if (json != null) {
					json.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
		return re;
	}

	public static final boolean isEmpty(final String str) {
		return str == null || str.trim().length() <= 0;
	}
}
