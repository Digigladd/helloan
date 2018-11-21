/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Value;

import javax.swing.text.html.Option;
import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = SyncEvent.DatasetFetched.class, name = "dataset-fetched"),
		@JsonSubTypes.Type(value = SyncEvent.ToIgnore.class, name = "to-ignore")
})
public interface SyncEvent {
	
	Optional<String> getRef();
	
	@Value
	final class DatasetFetched implements SyncEvent {
		public final Optional<String> ref;
		
		@JsonCreator
		public DatasetFetched(String ref) {
			this.ref = Optional.of(ref);
		}
	}
	
	@Value
	final class ToIgnore implements SyncEvent {
		public final Optional<String> ref;
		
		@JsonCreator
		public ToIgnore() {
			this.ref = Optional.empty();
		}
	}
}
