package com.digigladd.helloan.sync.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This interface defines all the events that the HelloanEntity supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface SyncEvent extends CompressedJsonable, AggregateEvent<SyncEvent> {
    
    @Override
    default public AggregateEventTag<SyncEvent> aggregateTag() {
        return SyncEventTag.INSTANCE;
    }

    /**
     * An event that represents a change in greeting message.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class DatasetsAdded implements SyncEvent {
        public final List<String> datasets;
        public final String year;
        public final Optional<LocalDateTime> lastParsed;
        
        @JsonCreator
        DatasetsAdded(List<String> datasets, String year, Optional<LocalDateTime> lastParsed) {
            this.datasets = Preconditions.checkNotNull(datasets, "datasets");
            this.year = year;
            this.lastParsed = lastParsed;
        }
    }
    
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class YearAdded implements SyncEvent {
        public final String year;
        public final LocalDateTime lastParsed;
        
        @JsonCreator
        YearAdded(String year) {
            this.year = Preconditions.checkNotNull(year, "year");
            this.lastParsed = LocalDateTime.now();
        }
    }
    
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class DatasetFetched implements SyncEvent {
        public final String ref;
        public final Long size;
        
        @JsonCreator
        DatasetFetched(String ref, Long size) {
            this.ref = Preconditions.checkNotNull(ref, "ref");
            this.size = Preconditions.checkNotNull(size, "size");
        }
    }

}
