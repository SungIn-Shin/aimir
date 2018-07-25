package com.aimir.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
	public static String makeDuplicationSafeFileName(String basepath, String filename) {
		String originalFilename = filename;
		
		File f = new File(makePath(basepath, filename));
		if (!f.exists()) 
			System.out.println("file not exists");
		
		int duplicateIdx = 0;
		while (f.exists()) {
			int idx = originalFilename.indexOf(".");
			duplicateIdx++;
			filename = originalFilename.substring(0, idx) + "_" + duplicateIdx + originalFilename.substring(idx);
			f = new File(makePath(basepath, filename));			
		}
		return filename;
	}

	public static boolean removeExistingFile(String path) {
		File f = new File(path);
		return f.delete();
	}
	
	public static boolean exists(String path) {
		File f = new File(path);
		return f.exists();
	}
	
	public static String makePath(String basepath, String filename) {
		if ("/".equals(basepath.substring(basepath.length()))) 
			return basepath + filename;
		else
			return basepath + "/" + filename;
	}
	
	public static void copy(String originalPath, String copyPath) throws IOException {

		File originalFile = new File(originalPath);
		File copyingFile = new File(copyPath);
		
		FileInputStream in = new FileInputStream(originalFile);
		FileOutputStream out = new FileOutputStream(copyingFile);
		
		int c;
		while ((c=in.read())!=-1) out.write(c);
		in.close();
		out.close();
	}
	
	/**
	 * 겹치지 않는 파일 Path 를 만들어 리턴한다. 포함된 디렉토리는 자동 생성된다.
	 * 
	 * @param homePath
	 *            홈 디렉토리
	 * @param fileName
	 *            파일명(확장자 제외)
	 * @param ext
	 *            파일 확장자('.' 제외)
	 * @param deletable
	 *            중복된 파일 삭제
	 * @return
	 */
	public static String makeFirmwareDirectory(String homePath, String fileName, String ext, boolean deletable) {
		File file = null;
		StringBuilder firmwareDir = new StringBuilder();
		firmwareDir.append(homePath);
		firmwareDir.append("/");
		firmwareDir.append(fileName);

		file = new File(firmwareDir.toString());
		if (!file.exists()) {
			file.mkdirs();
		}
		firmwareDir.append("/");
		firmwareDir.append(fileName);
		firmwareDir.append(".");
		firmwareDir.append(ext);

		file = new File(firmwareDir.toString());

		boolean result = false;
		if (deletable && file.exists()) {
			result = file.delete();
		} else {
			result = true;
		}

		if (!result) {
			//새로운 이름 규칙은 기존 이름+(n) 방식이다.
			if (fileName.matches(".*\\([0-9]*\\)")) {
				//기존 파일 이름이 중복 규칙에 의해 만들어진 파일 명이라면 숫자를 증가시켜 이름을 다시 만든다.
				int number = Integer.valueOf(fileName.replaceAll(".*\\(([0-9]*)\\)", "$1"));
				fileName = fileName.replaceAll("(.*)\\([0-9]*\\)", String.format("$1(%d)", number++));
			} else {
				// 파일 이름에 중복 이름 규칙을 적용한다.
				fileName = String.format("%s(0)", fileName);
			}

			//중복되는지 제귀하여 확인한다.
			return makeFirmwareDirectory(homePath, fileName, ext, deletable);
		}
		return file.getPath();
	}
	
}
