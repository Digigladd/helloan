/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.time.LocalDate;

@Value
@JsonDeserialize
public class Publication {
	public final LocalDate dateParution;
	public final int numParution;
	public final String numeroGrebiche;
	public final LocalDate dateSeance;
	public final int nrSeance;
	public final Session session;
	public final int numSeance;
	
	@JsonCreator
	public Publication(LocalDate dateParution,
						   int numParution,
						   String numeroGrebiche,
						   LocalDate dateSeance,
						   int nrSeance,
						   int numSeance,
						   Session session) {
		this.dateParution = Preconditions.checkNotNull(dateParution, "dateParution");
		this.numParution = Preconditions.checkNotNull(numParution, "numParution");
		this.numeroGrebiche = Preconditions.checkNotNull(numeroGrebiche, "numeroGrebiche");
		this.dateSeance = Preconditions.checkNotNull(dateSeance, "dateSeance");
		this.nrSeance = Preconditions.checkNotNull(nrSeance, "nrSeance");
		this.numSeance = Preconditions.checkNotNull(numSeance, "numSeance");
		this.session = Preconditions.checkNotNull(session, "session");
	}
}
