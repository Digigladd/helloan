/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.concurrent.Immutable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("serial")
@Immutable
public final class SyncStatus implements Jsonable {
	public final List<String> parsedYears;
	public final List<SyncDataset> datasets;
	public final Optional<LocalDateTime> lastParsed;
	public final Integer nrDataset;
	public final Long totalSize;
	
	@JsonCreator
	public SyncStatus(Optional<List<String>> parsedYears, Optional<List<SyncDataset>> datasets, Optional<LocalDateTime> lastParsed) {
		this.parsedYears = parsedYears.orElse(new ArrayList<>());
		this.datasets = datasets.orElse(new ArrayList<>());
		this.nrDataset = this.datasets.size();
		if (this.datasets.size() > 0) {
			this.totalSize = this.datasets.stream().map(dataset -> dataset.size).reduce(Long::sum).get();
		} else {
			this.totalSize = 0L;
		}
		this.lastParsed = lastParsed;
	}
}
