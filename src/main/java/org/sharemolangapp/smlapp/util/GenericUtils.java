package org.sharemolangapp.smlapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class GenericUtils {

	public static final int TRANSFER_RATE_MS = 500;
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	public static boolean IS_WINDOWS = true;
	static {
		String os = System.getProperty("os.name").toUpperCase();
		Pattern pattern = Pattern.compile("WINDOWS");
		Matcher matcher = pattern.matcher(os);
		
		if(matcher.find()) {
			IS_WINDOWS = true;
		} else {
			IS_WINDOWS = false;
		}
	}
	
	
	public static String toMB(double value) {
		double computedValue =  (value/1024)/1024;
		return String.format("%,.2f", computedValue)+"MB";
	}
	
}
