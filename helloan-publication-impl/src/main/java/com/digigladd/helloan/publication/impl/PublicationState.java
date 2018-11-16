/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.util.Optional;

@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class PublicationState implements CompressedJsonable {
	public final Optional<Publication> publication;
	
	@JsonCreator
	PublicationState(Optional<Publication> publication) {
		this.publication = publication;
	}
	
	public static PublicationState empty() {
		return new PublicationState(Optional.empty());
	}
}
