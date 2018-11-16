/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;

@Data
public class Nota {
	final String text;
	
	public Nota(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
