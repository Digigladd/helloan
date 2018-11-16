/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Data;

import java.util.Optional;

@Data
public class Orateur implements CompressedJsonable {
	final String nom;
	final Optional<String> qualite;
}
