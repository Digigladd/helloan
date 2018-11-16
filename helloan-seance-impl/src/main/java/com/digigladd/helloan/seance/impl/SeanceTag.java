/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import akka.japi.Pair;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Data;
import org.pcollections.PSequence;

import java.util.Optional;

@Data
public class SeanceTag implements CompressedJsonable {
	final SeanceTagType type;
	final Optional<Orateur> orateur;
	final Optional<String> text;
	final Optional<PSequence<Pair<String,Integer>>> resultatVote;
}
