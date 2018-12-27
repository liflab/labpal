package ca.uqac.lif.labpal.config;

import java.util.HashMap;

public class Config {

	public static ENV env = ENV.WEB;

	public static final HashMap<String, String> props= new HashMap<String, String>() {
	

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("namefileTempExp", "Experiments");
			put("pathInput", "../data/Experiments.html");
			put("pathOutput", "../results/");
			put("pdfName", "labpal-plots");
			put("imageName", "img");
			put("zipName", "LabpalStatic");
			
			//put("pathExportStatic","C:\\Users\\chafik.meniar\\Documents\\test\\Results\\");
		}
	};

	public static void setProp(String key, String value) {
		props.put(key, value);
	}

	public static String getProp(String key) {
		return props.get(key);
	}

	public static enum ENV {

		WINDOWS_LINUX,CODEOCEAN,STATIC, WEB,ALL

	}
}
