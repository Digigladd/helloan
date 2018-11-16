/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;

@Data
public class SousSection1 {
	String text;
	
	public SousSection1(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return "\t"+text;
	}
}
