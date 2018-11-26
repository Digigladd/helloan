/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import play.filters.cors.CORSFilter;
import play.http.DefaultHttpFilters;

import javax.inject.Inject;

public class Filters extends DefaultHttpFilters {
	@Inject
	public Filters(CORSFilter corsFilter) {
		super(corsFilter);
	}
}
