/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

public interface SeanceEvent extends CompressedJsonable, AggregateEvent<SeanceEvent> {
	
	@Override
	default public AggregateEventTag<SeanceEvent> aggregateTag() {
		return SeanceEventTag.INSTANCE;
	}
	
	@SuppressWarnings("serial")
	@Value
	@JsonDeserialize
	final class SeanceAdded implements SeanceEvent {
		public final Seance seance;
		
		@JsonCreator
		SeanceAdded(Seance seance) {
			this.seance = seance;
		}
	}
}
