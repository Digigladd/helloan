package com.digigladd.helloan.sync.impl;

import com.digigladd.helloan.sync.api.SyncDataset;
import com.digigladd.helloan.sync.api.SyncStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The state for the {@link SyncEntity} entity.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class SyncState implements CompressedJsonable {
    public final List<String> parsedYears;
    public final List<Dataset> datasets;
    public final Optional<LocalDateTime> lastParsed;

    @JsonCreator
    public SyncState(Optional<List<String>> parsedYears, Optional<List<Dataset>> datasets, Optional<LocalDateTime> lastParsed) {
        this.parsedYears = parsedYears.orElse(new ArrayList<>());
        this.datasets = datasets.orElse(new ArrayList<>());
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
}
