/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("serial")
@Immutable
public final class SyncDataset implements Jsonable {
	public final String ref;
	public final Long size;
	public final Boolean fetched;
	
	@JsonCreator
	public SyncDataset(String ref, Optional<Long> size, Optional<Boolean> fetched) {
		this.ref = Objects.requireNonNull(ref, "ref");
		this.size = size.orElse(0L);
		this.fetched = fetched.orElse(false);
	}
}
