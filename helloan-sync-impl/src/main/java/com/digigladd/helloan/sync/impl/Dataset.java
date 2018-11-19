/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.impl;

import com.digigladd.helloan.sync.api.SyncDataset;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class Dataset implements CompressedJsonable {
	public final String ref;
	public final Long size;
	public final Boolean fetched;
	
	@JsonCreator
	public Dataset(String ref, Optional<Long> size, Optional<Boolean> fetched) {
		this.ref = Objects.requireNonNull(ref, "ref");
		this.size = size.orElse(0L);
		this.fetched = fetched.orElse(false);
	}
	
	public SyncDataset toSyncStatusDataset() {
		
		return new SyncDataset(
				this.ref,
				Optional.of(this.size),
				Optional.of(this.fetched)
		);
	}
}
