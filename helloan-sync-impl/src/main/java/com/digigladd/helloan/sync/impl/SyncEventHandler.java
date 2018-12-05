/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.impl;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.datastax.driver.core.BoundStatement;
import com.digigladd.helloan.sync.impl.actors.SyncActor;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class SyncEventHandler {
	
	private static final Logger log = LoggerFactory.getLogger(SyncEventHandler.class);
	private final CassandraSession session;
	
	@Inject
	public SyncEventHandler(CassandraSession session,
							ReadSide readSide) {
		this.session = session;
		readSide.register(SyncEventProcessor.class);
	}
	
	public static class SyncEventProcessor extends ReadSideProcessor<SyncEvent> {
		
		private final CassandraReadSide readSide;
		private final CassandraSession session;
		private final ActorRef syncActor;
		private final ActorSystem system;
		private static final Logger log = LoggerFactory.getLogger(SyncEventProcessor.class);
		
		@Inject
		public SyncEventProcessor(CassandraReadSide readSide,
								  CassandraSession session,
								  @Named("syncActor") ActorRef syncActor,
								  ActorSystem system) {
			this.readSide = readSide;
			this.session = session;
			this.syncActor = syncActor;
			this.system = system;
		}
		
		@Override
		public ReadSideHandler<SyncEvent> buildHandler() {
			return readSide.<SyncEvent>builder("syncEventHandlerOffset")
					.setGlobalPrepare(this::globalPrepare)
					.setPrepare(this::prepare)
					.setEventHandler(SyncEvent.DatasetAdded.class, this::datasetAdded)
					.setEventHandler(SyncEvent.DatasetFetched.class, this::datasetFetched)
					.build();
		}
		
		private CompletionStage<List<BoundStatement>> datasetFetched(SyncEvent.DatasetFetched event) {
			log.info("Event Handler {}", event);
			return CompletableFuture.completedFuture(new ArrayList<>());
		}
		
		private CompletionStage<List<BoundStatement>> datasetAdded(SyncEvent.DatasetAdded event) {
			log.info("Event Handler {}", event);
			this.syncActor.tell(new SyncActor.Fetch(event.getDataset()), null);
			return CompletableFuture.completedFuture(new ArrayList<>());
		}
		
		private CompletionStage<Done> prepare(AggregateEventTag<SyncEvent> syncEventAggregateEventTag) {
			log.info("Event Handler prepare {}", syncEventAggregateEventTag);
			Duration duration = Duration.ofSeconds(30);
			SyncActor.Tick tick = new SyncActor.Tick();
			
			log.info("Event Handler scheduling {} to {} in {}", tick, this.syncActor, duration);
			this.system.scheduler().scheduleOnce(
					duration,
					this.syncActor,
					tick,
					this.system.dispatcher(),
					null
			);
			return CompletableFuture.completedFuture(Done.getInstance());
		}
		
		@Override
		public PSequence<AggregateEventTag<SyncEvent>> aggregateTags() {
			TreePVector<AggregateEventTag<SyncEvent>> pVector = TreePVector.empty();
			return pVector.plus(SyncEventTag.INSTANCE);
		}
		
		private CompletionStage<Done> globalPrepare() {
			log.info("Event Handler globalPrepare");
			return CompletableFuture.completedFuture(Done.getInstance());
		}
		
	}
}
