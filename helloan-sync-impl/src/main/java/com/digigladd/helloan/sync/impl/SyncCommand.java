package com.digigladd.helloan.sync.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;
import org.pcollections.PSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This interface defines all the commands that the SyncEntity supports.
 * <p>
 * By convention, the commands should be inner classes of the interface, which
 * makes it simple to get a complete picture of what commands an entity
 * supports.
 */
public interface SyncCommand extends CompressedJsonable {
    
    /**
     * A command to say hello to someone using the current greeting message.
     * <p>
     * The reply type is String, and will contain the message to say to that
     * person.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class Get implements SyncCommand, PersistentEntity.ReplyType<SyncState> {
        
        @JsonCreator
        public Get() {
        
        }
    }
    
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class AddYear implements SyncCommand, PersistentEntity.ReplyType<Done> {
        private final String year;
        
        @JsonCreator
        AddYear(String year) {
            this.year = Objects.requireNonNull(year, "year");
        }
    }
    
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class AddDatasets implements SyncCommand, PersistentEntity.ReplyType<Done> {
        private final PSet<String> refs;
        
        @JsonCreator
        public AddDatasets(PSet<String> refs) {
            this.refs = Objects.requireNonNull(refs, "refs");
        }
    }
    
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class FetchDataset implements SyncCommand, PersistentEntity.ReplyType<Done> {
        private final String ref;
        private final Long size;
        
        @JsonCreator
        public FetchDataset(String ref, Long size) {
            this.ref = Objects.requireNonNull(ref, "ref");
            this.size = Objects.requireNonNull(size, "size");
        }
    }
    
    static Get get() {
        return new Get();
    }
    
    static AddYear addYear(String year) {
        return new AddYear(year);
    }
}
