/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

public class Urls {
	public static String getYear(String year) {
		return Constants.DATA_ROOT_URL+Constants.DATA_ROOT_DIR+"/"+year;
	}
	
	public static String extractRef(String url) {
		return url.substring(url.indexOf("AN_"),url.indexOf(".taz"));
	}
	
	public static String getDataset(String ref) {
		return Constants.DATA_ROOT_URL+Constants.DATA_ROOT_DIR+"/"+ref.substring(3,7)+"/&download="+ref+".taz";
	}
}
