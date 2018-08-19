package com.cloud.zlist.autofire;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * jstarseven
 * 通用时间处理类  return Date
 * */
public class DateParser {
	private static int timezone = 0;
	private static final Pattern[] DPTN = {

			Pattern.compile(
					"(\\d{1,2})[\\s\\-\\/](\\d{1,2})[\\s\\-\\/](20\\d{2})\\s{0,2}((\\d{1,2})[:\\s](\\d{1,2})[:\\s]?(\\d{1,2})?)?"),

			Pattern.compile(
					"((20)?\\d{2}) {0,2}[\\.\\-/年] {0,2}(\\d{1,2}) {0,2}[\\.\\-/月] {0,2}(\\d{1,2}) {0,2}[日 \\s]{0,2}((上午)|(下午))?\\s{0,2}((\\d{1,2})[:\\s时](\\d{1,2})[:\\s分]?(\\d{1,2})?)?"),

			Pattern.compile("((20)?\\d{2})/(\\d{2})(\\d{2})"),

			Pattern.compile(
					"(\\d{1,2})[\\.\\-\\s/月](\\d{1,2})[日\\s]{0,2}((上午)|(下午))?\\s{0,2}((\\d{1,2})[:\\s](\\d{1,2})[:\\s]?(\\d{1,2})?)?"),

			Pattern.compile("([今前昨]天)?\\s{0,4}(\\d{1,2})[:\\s]{1,3}(\\d{1,2})[:\\s]?(\\d{1,2})?"),

			Pattern.compile("[今前昨]天"),

			Pattern.compile("((\\d{1,2})|(半))\\s*个?([天秒小时分钟周月年]{1,2})前"),

			Pattern.compile("(\\d{1,2})小?时(\\d{1,2})分钟?前"),

			Pattern.compile("(20\\d{2})[01]?(\\d{2})[012]?(\\d{2})") };

	public static Date parse(Object obj) {
		if (obj == null) {
			return null;
		}
		if ((obj instanceof Date)) {
			return (Date) obj;
		}
		if ((obj instanceof Number)) {
			return new Date(((Number) obj).longValue());
		}
		String str = ((String) obj).trim();
		if ((str.length() == 0) || ("null".equalsIgnoreCase(str))) {
			return null;
		}
		str = transZH(str);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		Matcher mt = DPTN[0].matcher(str);
		if (mt.find()) {
			int date = Integer.parseInt(mt.group(2));
			if ((date == 0) || (date > 31)) {
				return null;
			}
			int month = Integer.parseInt(mt.group(1));
			if (month <= 0) {
				return null;
			}
			if (month > 12) {
				if ((date > 0) && (date <= 12) && (month < 32)) {
					int tmp = month;
					month = date;
					date = tmp;
				} else {
					return null;
				}
			}
			String sy = mt.group(3);
			int year = Integer.parseInt(sy);
			if ((year < 2000) || (year > 2099)) {
				return null;
			}
			String hms = mt.group(4);
			if ((hms == null) || (hms.length() == 0)) {
				c.set(year, month - 1, date, timezone > 0 ? timezone : 0, 0, 0);
				return c.getTime();
			}
			int hour = Integer.parseInt(mt.group(5));
			if (hour >= 24) {
				return null;
			}
			int min = Integer.parseInt(mt.group(6));
			if (min >= 60) {
				return null;
			}
			String ssec = mt.group(7);
			int sec = (ssec == null) || (ssec.length() == 0) ? 0 : Integer.parseInt(ssec);
			c.set(year, month - 1, date, hour, min, sec);
			return c.getTime();
		}
		mt = DPTN[1].matcher(str);
		if (mt.find()) {
			String sy = mt.group(1);
			if (sy.length() == 2) {
				sy = "20" + sy;
			}
			int year = Integer.parseInt(sy);
			if ((year < 2000) || (year > 2099)) {
				return null;
			}
			int month = Integer.parseInt(mt.group(3)) - 1;
			if ((month < 0) || (month > 11)) {
				return null;
			}
			int date = Integer.parseInt(mt.group(4));
			if (date > 31) {
				return null;
			}
			String ss = mt.group(8);
			if ((ss == null) || (ss.length() == 0)) {
				c.set(year, month, date, timezone > 0 ? timezone : 0, 0, 0);
				return c.getTime();
			}
			int hour = Integer.parseInt(mt.group(9));
			if (hour >= 24) {
				return null;
			}
			int min = Integer.parseInt(mt.group(10));
			if (min >= 60) {
				return null;
			}
			String ssec = mt.group(11);
			int sec = (ssec == null) || (ssec.length() == 0) ? 0 : Integer.parseInt(ssec);
			if (("下午".equals(mt.group(5))) && (hour < 12)) {
				hour += 12;
			}
			c.set(year, month, date, hour, min, sec);
			return c.getTime();
		}
		mt = DPTN[2].matcher(str);
		if (mt.find()) {
			String strYear = mt.group(1);
			if (!strYear.startsWith("20")) {
				strYear = "20" + strYear;
			}
			int year = Integer.parseInt(strYear);
			int month = Integer.parseInt(mt.group(3)) - 1;
			int day = Integer.parseInt(mt.group(4));
			c.set(year, month, day, 0, 0, 0);
			return c.getTime();
		}
		mt = DPTN[3].matcher(str);
		if (mt.find()) {
			int year = c.get(1);
			int month = Integer.parseInt(mt.group(1)) - 1;
			if (month < 0) {
				return null;
			}
			if (month > c.get(2)) {
				year--;
			}
			int date = Integer.parseInt(mt.group(2));
			if (date > 31) {
				return null;
			}
			String p = mt.group(6);
			if ((p == null) || (p.length() == 0)) {
				c.set(year, month, date, timezone > 0 ? timezone : 0, 0, 0);
				return c.getTime();
			}
			int hour = Integer.parseInt(mt.group(7));
			if (hour >= 24) {
				return null;
			}
			int min = Integer.parseInt(mt.group(8));
			if (min >= 60) {
				return null;
			}
			String ssec = mt.group(9);
			int sec = (ssec == null) || (ssec.length() == 0) ? 0 : Integer.parseInt(ssec);
			if (("下午".equals(mt.group(3))) && (hour < 12)) {
				hour += 12;
			}
			c.set(year, month, date, hour, min, sec);
			return c.getTime();
		}
		mt = DPTN[4].matcher(str);
		if (mt.find()) {
			int hour = Integer.parseInt(mt.group(2));
			if (hour >= 24) {
				return null;
			}
			int min = Integer.parseInt(mt.group(3));
			if (min >= 60) {
				return null;
			}
			String day = mt.group(1);
			if ("昨天".equals(day)) {
				c.add(5, -1);
			} else if ("前天".equals(day)) {
				c.add(5, -2);
			}
			c.set(11, hour);
			c.set(12, min);
			return c.getTime();
		}
		mt = DPTN[5].matcher(str);
		if (mt.find()) {
			String day = mt.group(0);
			if ("昨天".equals(day)) {
				c.add(5, -1);
			} else if ("前天".equals(day)) {
				c.add(5, -2);
			}
			return c.getTime();
		}
		mt = DPTN[6].matcher(str);
		if (mt.find()) {
			String s = mt.group(4);
			long t;
			if ("年".equals(s)) {
				t = 31536000000L;
			} else {
				if ("月".equals(s)) {
					t = 2592000000L;
				} else {
					if ("周".equals(s)) {
						t = 604800000L;
					} else {
						if ("天".equals(s)) {
							t = 86400000L;
						} else {
							if ("小时".equals(s)) {
								t = 3600000L;
							} else {
								if ("时".equals(s)) {
									t = 3600000L;
								} else {
									if ("分钟".equals(s)) {
										t = 60000L;
									} else {
										if ("分".equals(s)) {
											t = 60000L;
										} else {
											if ("秒".equals(s)) {
												t = 1000L;
											} else {
												return null;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			String vs = mt.group(1);
			if ("半".equals(vs)) {
				t = System.currentTimeMillis() - t / 2L;
			} else {
				t = System.currentTimeMillis() - Integer.parseInt(vs) * t;
			}
			return new Date(t);
		}
		mt = DPTN[7].matcher(str);
		if (mt.find()) {
			int hh = Integer.parseInt(mt.group(1));
			int nn = Integer.parseInt(mt.group(2));
			long t = 3600000 * hh + 60000 * nn;
			return new Date(System.currentTimeMillis() - t);
		}
		mt = DPTN[8].matcher(str);
		if (mt.find()) {
			String sy = mt.group(1);
			int year = Integer.parseInt(sy);
			if ((year < 2000) || (year > 2099)) {
				return null;
			}
			int month = Integer.parseInt(mt.group(2)) - 1;
			if ((month < 0) || (month > 11)) {
				return null;
			}
			int date = Integer.parseInt(mt.group(3));
			if (date > 31) {
				return null;
			}
			c.set(year, month, date, timezone > 0 ? timezone : 0, 0, 0);
			return c.getTime();
		}
		return null;
	}

	private static String transZH(String string) {
		String zh = "〇一二三四五六七八九";
		string = string.replace("整", "0分").replaceAll("[上下]午", "");
		StringBuffer buffer = new StringBuffer();
		for (Character Char : string.toCharArray()) {
			int index = zh.indexOf(Char);
			if (index >= 0) {
				buffer.append(index);
			} else {
				buffer.append(Char);
			}
		}
		String str = buffer.toString();
		int index = str.indexOf("十");
		if (index == -1) {
			return str;
		} else {
			if (!Character.isDigit(str.charAt(index-1)) && !Character.isDigit(str.charAt(index+1))) {
				str=str.replace("十", "10");
			}else if (Character.isDigit(str.charAt(index-1)) && !Character.isDigit(str.charAt(index+1))) {
				str=str.replace("十", "0");
			}else if(!Character.isDigit(str.charAt(index-1)) && Character.isDigit(str.charAt(index+1))){
				str=str.replace("十", "1");
			}else if(Character.isDigit(str.charAt(index-1)) && Character.isDigit(str.charAt(index+1))){
				str=str.replace("十", "");
			}
			return str;
		}
		
	}

	public static void main(String[] args) {
	    System.out.println(parse("1982-01-01 00:00:00"));
		System.out.println(transZH("二〇一七年九月十日 上午十时整"));
		System.out.println(transZH("二〇一七年九月二十日 上午九时整"));
		System.out.println(transZH("二〇一七年九月十九日 上午九时整"));
		System.out.println(transZH("二〇一七年九月二十三日 上午九时整"));
		System.out.println("timezone=" + timezone);
		String[] testdata = { "1982-01-01 00:00:00","11-13 15:24", "2009-8-30 16:42:10", "8-23 15:24", "2周前", "3  天前", "12  分钟前", "3天前",
				"前天  09:36", "昨天 09:21 ", "2010-12-17 00:23 ", "2010-12-17 ", "昨天 12:37 ", "2011-8-15 08:42",
				"25-7-2011 11:43:57", "1-9-2011", "06-03", "半小时前", "今天发表", "昨天发表", "前天发表", "06-03-2010",
				"02-01-2010 00:39", "3小时26分钟前", "2010-8-24 上午 01:17:32", "2010-8-24 下午 01:17:32", "7小时前   »",
				"4/29/2010 1:31:00", "2012 年 1 月 31 日", "17时20分前", "2017年10月12日 14时30分", "二〇一七年九月十九日 上午九时整" };

		DateFormat df = DateFormat.getDateTimeInstance(2, 2);
		for (String s : testdata) {
			Date d = parse(s);
			System.out.println(s + "\t\t" + (d == null ? d : df.format(d)));
		}
	}

}
