/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;

import java.time.LocalDate;

@SuppressWarnings("serial")
@Data
public class Metadonnees {
	private LocalDate dateParution = LocalDate.MIN;
	private int numParution = 0;
	private String numeroGrebiche = "";
	private LocalDate dateSeance = LocalDate.MIN;
	private LocalDate periodeDu = LocalDate.MIN;
	private LocalDate periodeAu = LocalDate.MIN;
	private String typeSession = "";
	private int nrSeance = 0;
	private int numSeance = 0;
}
