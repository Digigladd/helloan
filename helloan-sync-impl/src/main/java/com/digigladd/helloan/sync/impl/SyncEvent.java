package com.digigladd.helloan.sync.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;
import org.pcollections.PSet;

import java.time.LocalDateTime;
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
    final class DatasetAdded implements SyncEvent {
        public final String dataset;
        public final Optional<LocalDateTime> lastParsed;
        
        @JsonCreator
        public DatasetAdded(String dataset, Optional<LocalDateTime> lastParsed) {
            this.dataset = Preconditions.checkNotNull(dataset, "dataset");
            this.lastParsed = lastParsed;
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
