/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.util.Optional;

@SuppressWarnings("serial")
@Value
@JsonDeserialize
public class SeanceState implements CompressedJsonable {
	public final Optional<Seance> seance;
	
	@JsonCreator
	SeanceState(Optional<Seance> seance) {
		this.seance= seance;
	}
	
	public static SeanceState empty() {
		return new SeanceState(Optional.empty());
	}
}
