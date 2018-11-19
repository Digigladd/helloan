package com.digigladd.helloan.sync.impl;

import com.digigladd.helloan.sync.api.SyncStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The state for the {@link SyncEntity} entity.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class SyncState implements CompressedJsonable {
    public final PSequence<String> parsedYears;
    public final PSequence<Dataset> datasets;
    public final Optional<LocalDateTime> lastParsed;
    
    public static final SyncState EMPTY = new SyncState(Optional.empty(), Optional.empty(), Optional.empty());

    @JsonCreator
    public SyncState(Optional<PSequence<String>> parsedYears, Optional<PSequence<Dataset>> datasets, Optional<LocalDateTime> lastParsed) {
        this.parsedYears = parsedYears.orElse(TreePVector.empty());
        this.datasets = datasets.orElse(TreePVector.empty());
        this.lastParsed = lastParsed;
    }
    
    public SyncStatus toSyncStatus() {
        return new SyncStatus(
            Optional.of(this.parsedYears),
            Optional.of(this.datasets.stream().map(
                  dataset -> dataset.toSyncStatusDataset()
            ).collect(Collectors.toList())),
            this.lastParsed
        );
    }
    
    public boolean addDataset(String ref) {
        return this.getDatasets().stream().filter(f -> f.ref == ref).count() == 0;
    }
    
    public boolean addYear(String year) {
        return this.getParsedYears().stream().filter(f -> f == year).count() == 0;
    }
    
    public Dataset createDataset(String ref) {
        return new Dataset(ref, Optional.empty(),Optional.empty());
    }
    
    public Dataset datasetFetched(String ref, Long size) {
        return new Dataset(ref, Optional.of(size), Optional.of(true));
    }
    
    
}
