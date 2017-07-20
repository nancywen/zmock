package com.zf.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 从配置文件获了配置项</br> 新特性：</br> <li>能实现动太加载配置文件 <li>配置文件路径可从spring配置文件注入
 * 
 * @author wen 2017-07-19
 */
public class PropUtil {
	private static final Logger log = Logger.getLogger(PropUtil.class);

	private static String configFile = "/opt/fonsview/NE/zmock/etc/zmock-config.properties";

	private static Properties prop = new Properties();
	
	static {
		loadproperties();
	}
	
	private static void loadproperties() {
		File file = null;
		FileReader reader = null;

		try {
			file = new File(configFile);
			reader = new FileReader(file);

			if (file.exists() && file.isFile()) {
				prop.clear();
				prop.load(reader);
				log.debug("load config file(" + configFile + ") success");
			} else {
				log.error(configFile + " not found");
				throw new Exception("config file(" + configFile + ") not exist");
			}
		} catch (Exception e) {
			log.error("load config file(cms-config.properties) error" + e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			if (reader != null) { 
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public static Boolean getBoolean(String key) {
		if (prop != null) {
			String value = getString(key);
			if (value != null && !"".equals(value.trim())) {
				Boolean result = false;
				try {
					result = Boolean.parseBoolean(value);
					return result;
				} catch (Exception e) {
					e.printStackTrace();
					log.error("config value(" + value
							+ ") can not a Boolean value", e);
				}
			}
		}
		return false;
	}

	public static Integer getInteger(String key) {
		if (prop != null) {
			String value = getString(key);
			if (value != null && !"".equals(value.trim())) {
				Integer result = null;
				try {
					result = Integer.parseInt(value);
					return result;
				} catch (Exception e) {
					e.printStackTrace();
					log.error("config value(" + value
							+ ") can not a Integer value", e);
				}
			}
		}
		return null;
	}

	public static Long getLong(String key) {
		if (prop != null) {
			String value = getString(key);
			if (value != null && !"".equals(value.trim())) {
				Long result = null;
				try {
					result = Long.parseLong(value);
					return result;
				} catch (Exception e) {
					e.printStackTrace();
					log.error("config value(" + value
							+ ") can not a Long value", e);
				}
			}
		}
		return null;
	}

	public static String getString(String key) {
		if (prop != null) {
			return prop.get(key) == null ? null : prop.get(key).toString()
					.trim();
		}
		return null;
	}

	public static String getString(String key, String defaultValue) {
		if (prop != null) {
			return prop.getProperty(key, defaultValue).trim();
		}
		return "";
	}

	// 如果该是属性是从来配置文件来注入的，则必须有get和set方法
	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String file) {
		configFile = file;
	}
	
	
}
