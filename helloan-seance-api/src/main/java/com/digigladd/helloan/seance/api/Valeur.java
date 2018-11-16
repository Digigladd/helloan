/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

@Value
@JsonDeserialize
public class Valeur {
	public final String libelle;
	public final Integer valeur;
}
