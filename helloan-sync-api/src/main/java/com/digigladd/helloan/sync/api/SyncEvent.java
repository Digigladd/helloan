/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = SyncEvent.DatasetFetched.class, name = "dataset-fetched"),
		@JsonSubTypes.Type(value = SyncEvent.ToIgnore.class, name = "to-ignore")
})
public interface SyncEvent {
	
	@Value
	final class DatasetFetched implements SyncEvent {
		public final String ref;
		
		@JsonCreator
		public DatasetFetched(String ref) {
			this.ref = Preconditions.checkNotNull(ref, "ref");
		}
	}
	
	@Value
	final class ToIgnore implements SyncEvent {
		
		@JsonCreator
		public ToIgnore() {
		
		}
	}
}
