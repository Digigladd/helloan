/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Publication implements CompressedJsonable {
	final LocalDate dateParution;
	final Integer numParution;
	final String numeroGrebiche;
	final LocalDate dateSeance;
	final Integer nrSeance;
	final LocalDate periodeDu;
	final LocalDate periodeAu;
	final String typeSession;
	final Integer numSeance;
	
	@JsonCreator
	public Publication(LocalDate dateParution,
					   Integer numParution,
					   String numeroGrebiche,
					   LocalDate dateSeance,
					   Integer nrSeance,
					   LocalDate periodeDu,
					   LocalDate periodeAu,
					   String typeSession,
					   Integer numSeance) {
		this.dateParution = dateParution;
		this.numParution = numParution;
		this.numeroGrebiche = numeroGrebiche;
		this.dateSeance = dateSeance;
		this.nrSeance = nrSeance;
		this.periodeDu = periodeDu;
		this.periodeAu = periodeAu;
		this.typeSession = typeSession;
		this.numSeance = numSeance;
	}
}
