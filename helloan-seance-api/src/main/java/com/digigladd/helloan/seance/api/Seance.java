/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import org.pcollections.PSequence;

@Value
@JsonDeserialize
public class Seance {
	public final String president;
	public final String qualitePresident;
	public final PSequence<Evenement> evenements;
}
