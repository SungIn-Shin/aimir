package com.aimir.mm.util;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mobile Money properties
 * 
 * @author designer, 2017-06-12
 */
public class MmProperty {
	private static Log log = LogFactory.getLog(MmProperty.class);
	private static final Properties properties;

	static {
		String it = "/mm.properties";

		Properties result = new Properties();
		try {
			InputStream is = MmProperty.class.getResourceAsStream(it);
			result.load(is);
			is.close();

		} catch (Exception e) {
			log.error(e, e);
		}

		properties = result;
	}

	private MmProperty() {
		super();
	}

	/**
	 * get PropertyURL
	 *
	 * @param key
	 *            <code>String</code>
	 * @return url <code>URL</code>
	 */
	public static URL getPropertyURL(String key) {
		String val = properties.getProperty(key);

		URL url = null;
		try {
			url = new URL(val);
		} catch (Exception ex) {
		}

		if (url == null) {
			url = MmProperty.class.getResource(val);
		}

		return url;

	}

	/**
	 * get property
	 *
	 * @param key
	 *            <code>String</code> key
	 * @return value <code>String</code>
	 */
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * get property
	 *
	 * @param key
	 *            <code>String</code> key
	 * @param key
	 *            <code>String</code> value
	 * @return value <code>String</code>
	 */
	public static String getProperty(String key, String value) {
		return properties.getProperty(key, value);
	}

	/**
	 * get property names
	 *
	 * @return enumeration <code>Enumeration</code>
	 */
	public static Enumeration<?> propertyNames() {
		return properties.propertyNames();
	}
}
