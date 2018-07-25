package com.aimir.mm;

import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		String string1 = "P161218173";
		String clean1 = string1.replaceAll("[^0-9]", "");
		
		System.out.println(clean1);
	}

}
