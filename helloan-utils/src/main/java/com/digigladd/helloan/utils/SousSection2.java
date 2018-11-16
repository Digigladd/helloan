/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;

@Data
public class SousSection2 {
	String text;
	
	public SousSection2(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return "\t\t"+text;
	}
}
