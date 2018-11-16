/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;


public interface PublicationEvent extends CompressedJsonable, AggregateEvent<PublicationEvent> {
	@Override
	default public AggregateEventTag<PublicationEvent> aggregateTag() {
		return PublicationEventTag.INSTANCE;
	}
	
	@SuppressWarnings("serial")
	@Value
	@JsonDeserialize
	final class PublicationAdded implements PublicationEvent {
		public final Publication publication;
		
		@JsonCreator
		PublicationAdded(Publication publication) {
			this.publication = publication;
		}
	}
}
