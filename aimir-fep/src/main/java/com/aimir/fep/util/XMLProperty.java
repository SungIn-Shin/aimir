package com.aimir.fep.util;

import java.io.FileReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XMLProperty {
	public static String[] getProperties (String filePath, String group, String[] key) {
		InputSource is;
		try {
			is = new InputSource(new FileReader(filePath));
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			XPath  xpath = XPathFactory.newInstance().newXPath();
			String expression = "";
			for(int i =0; i < key.length;  i++){
				expression = "//"+group+"/"+ key[i];
				String value = xpath.compile(expression).evaluate(document);
				key[i] = value;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return key;
	}
}
