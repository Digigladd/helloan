/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;

@Data
public class Section {
	final String text;
	
	public Section(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
