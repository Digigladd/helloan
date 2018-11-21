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
    public final PSequence<Dataset> datasets;
    public final Optional<LocalDateTime> lastParsed;
    
    public static final SyncState EMPTY = new SyncState(Optional.empty(), Optional.empty());

    @JsonCreator
    public SyncState(Optional<PSequence<Dataset>> datasets, Optional<LocalDateTime> lastParsed) {
        this.datasets = datasets.orElse(TreePVector.empty());
        this.lastParsed = lastParsed;
    }
    
    public SyncStatus toSyncStatus() {
        return new SyncStatus(
            Optional.of(this.datasets.stream().map(
                  dataset -> dataset.toSyncStatusDataset()
            ).collect(Collectors.toList())),
            this.lastParsed
        );
    }
    
}
