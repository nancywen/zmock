package com.zf.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;

public class FsvDateFormatUtils extends DateFormatUtils{
	
	private static final Logger log = Logger.getLogger(FsvDateFormatUtils.class);

	public static final Date parse(String dateStr,String pattern, TimeZone timeZone,Locale locale){
		try {
			return FastDateFormat.getInstance(pattern, timeZone, locale).parse(dateStr);
		} catch (ParseException e) {
			log.error("",e);
			throw new RuntimeException(e);
		}
	}
	
	public static final Date parse(String dateStr,String pattern,TimeZone timeZone){
		return parse(dateStr, pattern, timeZone, null);
	}
	
	public static final Date parse(String dateStr,String pattern,Locale locale){
		return parse(dateStr, pattern, null, locale);
	}

	public static final Date parse(String dateStr,String pattern){
		return parse(dateStr, pattern, null, null);
	}

	
	private final static FastDateFormat DEFAULT_FAST_DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	
	private final static FastDateFormat DEFAULT_FAST_DATE_FORMAT_2 = FastDateFormat.getInstance("yyyyMMddHHmmss");
	
	/**
	 * 按默认格式 yyyy-MM-dd HH:mm:ss 转换
	 * @param dateStr
	 * @return
	 */
	public static final Date parseDefault(String dateStr){
		try {
			return DEFAULT_FAST_DATE_FORMAT.parse(dateStr);
		} catch (ParseException e) {
			log.error("",e);
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 按默认格式 yyyy-MM-dd HH:mm:ss 转换
	 * @param dateStr
	 * @return
	 */
	public static final Date parseDefault2(String dateStr){
		try {
			return DEFAULT_FAST_DATE_FORMAT_2.parse(dateStr.replaceAll("[^0-9]", ""));
		} catch (ParseException e) {
			log.error("",e);
			throw new RuntimeException(e);
		}
	}
	

	/**
	 * 按默认格式 yyyy-MM-dd HH:mm:ss 转换
	 * @param dateStr
	 * @return
	 */
	public static final Date parseDefault3(String dateStr){
		try {
			return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse(dateStr);
		} catch (ParseException e) {
			log.error("",e);
			throw new RuntimeException(e);
		}
	}
	
	

	
	/**
	 * 按默认格式 yyyy-MM-dd HH:mm:ss 格式化
	 * @param dateStr
	 * @return
	 */
	public static final String formatDefault(Date date){
		return DEFAULT_FAST_DATE_FORMAT.format(date);
	}
	
	
	public static void main(String[] args) {
		
		parseDefault3("2015-11-25 11:11:23");
		
		
		long l = System.currentTimeMillis();
		for(int i = 0;i<10000;i++){
			parseDefault3("2015-11-25 11:11:23");
		}
		System.out.println(System.currentTimeMillis()-l);
	}
	
	
}
