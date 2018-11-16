/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;

@Data
public class Para {
	String orateur = "";
	String qualite = "";
	String text = "";
	
	@Override
	public String toString() {
		return orateur+qualite+": "+text;
	}
}
