/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Data;
import org.pcollections.PSequence;

@Data
public class Seance implements CompressedJsonable {
	
	final String presidentSeance;
	final String qualitePresident;
	final PSequence<SeanceTag> events;
	
	@JsonCreator
	public Seance(String presidentSeance,
				  String qualitePresident,
				  PSequence<SeanceTag> events) {
		this.presidentSeance = presidentSeance;
		this.qualitePresident = qualitePresident;
		this.events = events;
	}
}
