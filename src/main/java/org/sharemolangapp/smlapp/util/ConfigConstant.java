package org.sharemolangapp.smlapp.util;

import java.io.File;

public abstract class ConfigConstant {
	
	
	// json
	public static final String PREFERENCES_JSON_FILE = "preferences.json";
	
	
	// directories
	public static final StringBuilder LOCAL_MAIN_DIR = new StringBuilder();
	static {
		LOCAL_MAIN_DIR.append(System.getProperty("user.home")).append(File.separator);
		LOCAL_MAIN_DIR.append("Documents").append(File.separator);
		LOCAL_MAIN_DIR.append("smlapp").append(File.separator);
	}
	public static final String JSON_DIR = "json/";
	
	
	
	private ConfigConstant() {
		throw new UnsupportedOperationException(getClass().getName() + " is available. Use static fields and methods only.");
	}
	
	
	
	public static String getJsonDirFullPath() {
		return getJsonDirFullPath(null);
	}
	
	public static String getJsonDirFullPath(String filename) {
		return (filename != null
				? LOCAL_MAIN_DIR+JSON_DIR.replace("/",File.separator)+filename
				: LOCAL_MAIN_DIR+JSON_DIR.replace("/",File.separator));
	}

	
	
	// settings nodes
	public enum SettingsNode{
		VACCINATION_CARD("vaccination_card"),
		VACCINEE_PROFILE("vaccinee_profile"),
		DOSAGE_TAB("dosage_tab"),
		PREFERRED_CAMERA_DEVICE("preferred_camera_device"),
		LIST_REPORT_FILTER_BY_MONTH_LIMIT("list_report_filter_by_month_limit"),
		BARANGAY("barangay"),
		MUNICIPALITY("municipality"),
		PROVINCE("province"),
		VACCINE("vaccine"),
		CATEGORY("category"),
		DESIGNATION("designation"),
		WEBCAM_LIST("webcamList"),
		KEY("key"),
		DEPRECIATED_KEYS("depreciated_keys"),
		HOST("host"),
		PORT("port"),
		REGISTRATION_AGE_LIMIT("registration_age_limit"),
		DAYS_INTERVAL_NEXT_JAB("days_interval_next_jab"),
		REGISTRATION_SENIOR_AGE("60"),
		LAOANG_ID_NUMBER("laoang_id_number");
		
		private String value;
		SettingsNode(String value){
			this.value = value;
		}
		public String getNodeText() {
			return value;
		}
	}
}
