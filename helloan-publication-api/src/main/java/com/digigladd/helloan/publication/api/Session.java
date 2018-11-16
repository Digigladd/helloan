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
public class Session {
	public final LocalDate periodeDu;
	public final LocalDate periodeAu;
	public final String typeSession;
	
	@JsonCreator
	public Session(LocalDate periodeDu,
						   LocalDate periodeAu,
						   String typeSession) {
		this.periodeDu = Preconditions.checkNotNull(periodeDu, "periodeDu");
		this.periodeAu = Preconditions.checkNotNull(periodeAu, "periodeAu");
		this.typeSession = Preconditions.checkNotNull(typeSession, "typeSession");
	}
}
