/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PublicationEvent.PublicationAdded.class, name = "publication-added"),
		@JsonSubTypes.Type(value = PublicationEvent.ToIgnore.class, name = "to-ignore")
})
public interface PublicationEvent {
	
	Optional<String> getRef();
	
	@Value
	final class PublicationAdded implements PublicationEvent {
		public final Optional<String> ref;
		public final String numeroGrebiche;
		public final Integer sessions;
		
		@JsonCreator
		public PublicationAdded(String ref, String numeroGrebiche, Integer sessions) {
			this.ref = Optional.of(ref);
			this.numeroGrebiche = Preconditions.checkNotNull(numeroGrebiche, "numeroGrebiche");
			this.sessions = Preconditions.checkNotNull(sessions, "sessions");
		}
	}
	
	@Value
	final class ToIgnore implements PublicationEvent {
		public final Optional<String> ref;
		
		@JsonCreator
		public ToIgnore() {
			this.ref = Optional.empty();
		}
	}
}
