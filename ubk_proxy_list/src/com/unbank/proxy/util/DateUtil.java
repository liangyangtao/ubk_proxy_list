package com.unbank.proxy.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	// 按照给定的格式化字符串格式化日期
	public static String formatDate(Date date, String formatStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		return sdf.format(date);
	}

	// 按照给定的格式化字符串解析日期
	public static Date parseDate(String dateStr, String formatStr) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	// 从字符串中分析日期
	public static Date parseDate(String dateStr) {
		Date date = null;
		String farmatStr = "yyyyMMddHHmmss"; // 格式化字符串
		String[] dateArray = dateStr.split("\\D+"); // +防止多个非数字字符在一起时导致解析错误
		for (String string : dateArray) {
			System.out.println(string);
		}
		int dateLen = dateArray.length;
		if (dateLen > 0) {
			if (dateLen == 1 && dateStr.length() == farmatStr.length()) {
				// 如果字符串长度为14位并且不包含其他非数字字符，则按照（yyyyMMddHHmmss）格式解析
				date = parseDate(dateStr, "yyyyMMddHHmmss");
			} else {
				String fDateStr = dateArray[0];
				for (int i = 1; i < dateLen; i++) {
					// 左补齐是防止十位数省略的情况
					fDateStr += leftPad(dateArray[i], "0", 2);
				}
				date = parseDate(fDateStr,
						farmatStr.substring(0, (dateLen - 1) * 2 + 4));
			}
		}

		return date;
	}

	// 左补齐
	public static String leftPad(String str, String pad, int len) {
		String newStr = (str == null ? "" : str);
		while (newStr.length() < len) {
			newStr = pad + newStr;
		}
		if (newStr.length() > len) {
			newStr = newStr.substring(newStr.length() - len);
		}
		return newStr;
	}

	public static void main(String[] args) {

		String[] dateStrArray = new String[] { "2014-03-12 12:05:34",
				"2014-03-12 12:05", "2014-03-12 12", "2014-03-12", "2014-03",
				"2014", "20140312120534", "2014/03/12 12:05:34",
				"2014/3/12 12:5:34", "2014年3月12日 12时5分34秒" };

		for (int i = 0; i < dateStrArray.length; i++) {
			Date date = parseDate(dateStrArray[i]);
			System.out.println(dateStrArray[i] + " ==> "
					+ formatDate(date, "yyyy-MM-dd HH:mm:ss"));
		}
	}
}
