package com.digigladd.helloan.sync.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import com.digigladd.helloan.sync.api.SyncStatus;
import com.digigladd.helloan.sync.impl.actors.SyncActor;
import com.digigladd.helloan.utils.Constants;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import javax.inject.Named;

import com.digigladd.helloan.sync.api.SyncService;
import com.digigladd.helloan.sync.impl.SyncCommand.*;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import com.lightbend.lagom.javadsl.server.ServerServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the SyncService.
 */
public class SyncServiceImpl implements SyncService {
    private final PersistentEntityRegistry persistentEntityRegistry;
    private final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);
    private final ActorRef syncActor;
    private final PubSubRegistry pubSub;

    @Inject
    public SyncServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
                           @Named("syncActor") ActorRef syncActor,
                           PubSubRegistry pubSub,
                           Materializer materializer) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(SyncEntity.class);
        this.syncActor = syncActor;
        this.pubSub = pubSub;
        pubSub.refFor(TopicId.of(SyncEvent.class, Constants.SYNC_EVENTS)).subscriber().runForeach(
                evt -> {
                    log.info("Received new sync event {}", evt);
                    if (evt instanceof SyncEvent.YearAdded || evt instanceof SyncEvent.DatasetFetched) {
                        syncActor.tell(new SyncActor.Tick(), null);
                    }
                },
                materializer
        );
    }

    @Override
    public ServiceCall<NotUsed, SyncStatus> hello() {
        
        return request -> {
            PersistentEntityRef<SyncCommand> ref = persistentEntityRegistry.refFor(SyncEntity.class, Constants.SYNC_ENTITY_ID);
            return ref.ask(new Get()).thenApply(
                    syncState -> syncState.toSyncStatus()
            );
        };
    }
    
    @Override
    public ServerServiceCall<NotUsed, NotUsed> status() {
        return HeaderServiceCall.of((requestHeader, request) -> {
            ResponseHeader responseHeader = ResponseHeader.OK;
            return completedFuture(Pair.create(responseHeader, NotUsed.getInstance()));
        });
    }
    
    @Override
    public Topic<com.digigladd.helloan.sync.api.SyncEvent> syncEvents() {
        return TopicProducer.singleStreamWithOffset(offset -> {
            return persistentEntityRegistry
                    .eventStream(SyncEventTag.INSTANCE, offset)
                    .map(this::convertEvent);
        });
    }
    
    private Pair<com.digigladd.helloan.sync.api.SyncEvent, Offset> convertEvent(Pair<SyncEvent, Offset> pair) {
        com.digigladd.helloan.sync.api.SyncEvent evt;
        if (pair.first() instanceof SyncEvent.DatasetFetched) {
            SyncEvent.DatasetFetched dataset = (SyncEvent.DatasetFetched)pair.first();
            evt = new com.digigladd.helloan.sync.api.SyncEvent.DatasetFetched(dataset.ref);
        } else {
            evt = new com.digigladd.helloan.sync.api.SyncEvent.ToIgnore();
        }
        return Pair.create(evt, pair.second());
    }
    
    private Pair<SyncActor.Tick, Offset> consumeEvent(Pair<SyncEvent, Offset> pair) {
        log.info("Received sync event {}", pair.first());
        SyncActor.Tick tick = new SyncActor.Tick();
        this.syncActor.tell(tick, null);
        return Pair.create(tick, pair.second());
    }
}
