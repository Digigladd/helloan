/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.time.LocalDate;
import java.util.Optional;

public interface PublicationCommand extends CompressedJsonable {
	
	static AddPublication createAddPublication(Publication publication) {
		return new AddPublication(publication);
	}
	
	@SuppressWarnings("serial")
	@Value
	@JsonDeserialize
	final class AddPublication implements PublicationCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
		public final Publication publication;
		
		@JsonCreator
		AddPublication(Publication publication) {
			this.publication = publication;
		}
	}
	
	@SuppressWarnings("serial")
	@Value
	@JsonDeserialize
	final class Get implements PublicationCommand, PersistentEntity.ReplyType<Optional<Publication>> {
		
		@JsonCreator
		Get() {
		
		}
	}
}
