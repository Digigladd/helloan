package com.digigladd.helloan.sync.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.util.List;
import java.util.Objects;

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
        Get() {
        
        }
    }
    
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class AddYear implements SyncCommand, PersistentEntity.ReplyType<SyncState> {
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
        private final List<String> refs;
        private final String year;
        
        @JsonCreator
        AddDatasets(List<String> refs, String year) {
            this.refs = Objects.requireNonNull(refs, "refs");
            this.year = year;
        }
    }
    
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class FetchDataset implements SyncCommand, PersistentEntity.ReplyType<SyncState> {
        private final String ref;
        private final Long size;
        
        @JsonCreator
        FetchDataset(String ref, Long size) {
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
    
    static AddDatasets addDatasets(List<String> refs, String year) {
        return new AddDatasets(refs, year);
    }
    
    static FetchDataset fetchDataset(String ref, Long size) {
        return new FetchDataset(ref, size);
    }
}
