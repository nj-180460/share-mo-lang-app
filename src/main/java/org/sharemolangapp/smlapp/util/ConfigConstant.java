package org.sharemolangapp.smlapp.util;

import java.io.File;



public abstract class ConfigConstant {
	
	// reponses
	public static final String OK_RESPONSE = "OK";
	public static final String DECLINE_RESPONSE = "DECLINE";
	public static final String SERVER_UP_RESPONSE = "SERVER_UP";
	public static final String SERVER_DOWN_RESPONSE = "SERVER_DOWN";
	public static final String CLIENT_UP_RESPONSE = "CLIENT_UP";
	public static final String CLIENT_DOWN_RESPONSE = "CLIENT_DOWN";
	public static final String CLIENT_REQUESTING_RESPONSE = "CLIENT_REQUESTING";
	public static final String NONE_RESPONSE = "NONE";
	public static final String FILENAME_EMPTY_RESPONSE = "FILENAME_EMPTY";
	public static final String CONNECTION_LOST_RESPONSE = "CONNECTION_LOST";
	
	// json files
	public static final String PREFERENCES_JSON_FILE = "preferences.json";
	
	// resources dir
	public static final String APP_DIR = "preferences" + File.separator;
	public static final String JSON_DIR = "json/";
	
	// local dir
	public static final StringBuilder LOCAL_DEFAULT_OUTPUT_DIR = new StringBuilder();
	static {
		LOCAL_DEFAULT_OUTPUT_DIR.append(System.getProperty("user.home")).append(File.separator);
		LOCAL_DEFAULT_OUTPUT_DIR.append("Downloads").append(File.separator);
		LOCAL_DEFAULT_OUTPUT_DIR.append("smlapp").append(File.separator);
	}
	
	
	
	private ConfigConstant() {
		throw new UnsupportedOperationException(getClass().getName() + " is available. Use static fields and methods only.");
	}
	
	
	
	public static String getJsonDirFullPath() {
		return getJsonDirFullPath(null);
	}
	
	public static String getJsonDirFullPath(String filename) {
		return (filename != null
				? LOCAL_DEFAULT_OUTPUT_DIR+JSON_DIR.replace("/",File.separator)+filename
				: LOCAL_DEFAULT_OUTPUT_DIR+JSON_DIR.replace("/",File.separator));
	}
	
	public static String getJsonRelativePath(String filename) {
		return APP_DIR + (filename != null
				? JSON_DIR.replace("/",File.separator)+filename
				: JSON_DIR.replace("/",File.separator));
	}

	
	
	// settings nodes
	public enum SettingsNode{
		PREFERENCES("Preferences"),
		EXIT("Exit");
		private String value;
		SettingsNode(String value){
			this.value = value;
		}
		public String getNodeText() {
			return value;
		}
	}
}
