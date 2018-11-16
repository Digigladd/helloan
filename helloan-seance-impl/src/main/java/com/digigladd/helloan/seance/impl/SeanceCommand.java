/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.util.Optional;

public interface SeanceCommand extends CompressedJsonable {
	
	@SuppressWarnings("serial")
	@Value
	@JsonDeserialize
	final class AddSeance implements SeanceCommand, PersistentEntity.ReplyType<Done> {
		public final Seance seance;
		
		@JsonCreator
		AddSeance(Seance seance) {
			this.seance = seance;
		}
	}
	
	@SuppressWarnings("serial")
	@Value
	@JsonDeserialize
	final class Get implements SeanceCommand, PersistentEntity.ReplyType<Optional<Seance>> {
		@JsonCreator
		Get() {
		
		}
	}
}
