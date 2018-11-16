/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

@Value
@JsonDeserialize
public class PullSeance {
	public final String publication;
	public final Integer session;
	public final String numeroGrebiche;
	
	@JsonCreator
	public PullSeance(String publication,
					  Integer session,
					  String numeroGrebiche) {
		this.publication = Preconditions.checkNotNull(publication, "publication");
		this.session = Preconditions.checkNotNull(session, "session");
		this.numeroGrebiche = Preconditions.checkNotNull(numeroGrebiche, "numeroGrebiche");
	}
}
