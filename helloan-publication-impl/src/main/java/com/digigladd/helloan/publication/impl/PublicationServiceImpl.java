/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Flow;
import com.digigladd.helloan.publication.api.Publication;
import com.digigladd.helloan.publication.api.PublicationEvent;
import com.digigladd.helloan.publication.api.PublicationService;
import com.digigladd.helloan.publication.api.Session;
import com.digigladd.helloan.sync.api.SyncEvent;
import com.digigladd.helloan.sync.api.SyncService;
import com.digigladd.helloan.sync.api.SyncStatus;
import com.digigladd.helloan.utils.ArchiveParser;
import com.digigladd.helloan.utils.Metadonnees;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import com.lightbend.lagom.javadsl.server.ServerServiceCall;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.digigladd.helloan.utils.CompletionStageUtils.askDone;
import static com.digigladd.helloan.utils.CompletionStageUtils.doAll;
import static com.digigladd.helloan.utils.CompletionStageUtils.orDone;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class PublicationServiceImpl implements PublicationService {
	private final PersistentEntityRegistry persistentEntityRegistry;
	private final PublicationRepository publicationRepository;
	private final SyncService syncService;
	private boolean syncing = false;
	
	private final Logger log = LoggerFactory.getLogger(PublicationServiceImpl.class);
	
	
	@Inject
	public PublicationServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
								  SyncService syncService,
								  PublicationRepository publicationRepository
								  ) {
		this.persistentEntityRegistry = persistentEntityRegistry;
		this.persistentEntityRegistry.register(PublicationEntity.class);
		this.publicationRepository = publicationRepository;
		this.syncService = syncService;
		this.syncService.syncEvents().subscribe().atLeastOnce(
				Flow.<SyncEvent>create().map(this::getCRI).mapAsync(1, this::createPublication)
		);
	}
	
	private Optional<Metadonnees> getCRI(SyncEvent event) {
		Optional<Metadonnees> metadonnees = Optional.empty();
		if (event.getRef().isPresent()) {
			final String ref = event.getRef().get();
			Metadonnees meta = ArchiveParser.getCRI(ref);
			
			if (meta != null) {
				meta.setRef(ref);
				metadonnees = Optional.of(meta);
			}
		}
		return metadonnees;
	}
	
	private Optional<Metadonnees> getCRI(final String ref) {
		Optional<Metadonnees> metadonnees = Optional.empty();
		if (ref != null) {
			Metadonnees meta = ArchiveParser.getCRI(ref);
			
			if (meta != null) {
				meta.setRef(ref);
				metadonnees = Optional.of(meta);
			}
		}
		return metadonnees;
	}
	
	private CompletionStage<Done> createPublication(Optional<Metadonnees> metadonnees) {
		return metadonnees
				.map(
						meta -> {
							PersistentEntityRef<PublicationCommand> entityRef = this.persistentEntityRegistry.refFor(PublicationEntity.class, meta.getNumeroGrebiche());
							return entityRef.ask(new PublicationCommand.AddPublication(
									new com.digigladd.helloan.publication.impl.Publication(
											meta.getDateParution(),
											meta.getNumParution(),
											meta.getNumeroGrebiche(),
											meta.getDateSeance(),
											meta.getNrSeance(),
											meta.getPeriodeDu(),
											meta.getPeriodeAu(),
											meta.getTypeSession(),
											meta.getNumSeance(),
											meta.getRef()
									))).thenApply(
											done -> askDone(log,"createPublication")
									);
						}
				)
				.orElse(orDone(log,"createPublication"));
	}
	
	@Override
	public ServiceCall<NotUsed, Optional<Publication>> getPublication(String numeroGrebiche) {
		
		return request -> {
			PersistentEntityRef<PublicationCommand> ref = this.persistentEntityRegistry.refFor(PublicationEntity.class, numeroGrebiche);
			return ref.ask(new PublicationCommand.Get()).thenApply(
					state -> state.map(
							this::toPublication
					)
			);
		};
		
	}
	
	@Override
	public ServiceCall<NotUsed, PSequence<Publication>> listPublications() {
		return request -> publicationRepository.getPublications()
				.thenApply(
						PSequence::stream
				)
				.thenApply(
						publications -> publications.map(this::toPublication)
				)
				.thenApply(
						publications -> publications.collect(Collectors.toList())
				).thenApply(
						TreePVector::from
				);
	}
	
	@Override
	public ServiceCall<NotUsed, PSequence<String>> sync() {
		
		CompletionStage<SyncStatus> syncStatus =  syncService.hello().invoke();
		CompletionStage<PSequence<com.digigladd.helloan.publication.impl.Publication>> listPublications = publicationRepository.getPublications();
		CompletionStage<PSequence<String>> notSync = syncStatus.thenCombine(listPublications, (status, publications) -> {
			final Set<String> uniquePublication = publications.stream()
					.map(m -> m.ref)
					.collect(Collectors.toSet());
			return TreePVector.from(status.datasets.stream()
					.filter(f -> f.fetched && !uniquePublication.contains(f.ref))
					.map(m -> m.ref)
					.collect(Collectors.toList()));
		});
		
		return request -> {
			if (!syncing) {
				return notSync.thenCompose(this::parseUnsync);
			} else {
				return notSync;
			}
		};
	}
	
	private CompletionStage<PSequence<String>> parseUnsync(PSequence<String> unsync) {
		log.info("{} unsync publication(s), proceeding with analysis!");
		return doAll(unsync.stream().map(this::getCRI).map(this::createPublication).collect(Collectors.toList())).thenApply(done -> unsync);
	}
	
	@Override
	public ServerServiceCall<NotUsed, NotUsed> status() {
		return HeaderServiceCall.of((requestHeader, request) -> {
			ResponseHeader responseHeader = ResponseHeader.OK;
			return completedFuture(Pair.create(responseHeader, NotUsed.getInstance()));
		});
	}
	
	@Override
	public Topic<PublicationEvent> pubEvents() {
		return TopicProducer.singleStreamWithOffset(offset -> {
			return persistentEntityRegistry
					.eventStream(PublicationEventTag.INSTANCE, offset)
					.map(this::convertEvent);
		});
	}
	
	private Publication toPublication(com.digigladd.helloan.publication.impl.Publication publication) {
		return new Publication(
				publication.dateParution,
				publication.numParution,
				publication.numeroGrebiche,
				publication.dateSeance,
				publication.nrSeance,
				publication.numSeance,
				new Session(
						publication.periodeDu,
						publication.periodeAu,
						publication.typeSession
				)
		);
	}
	
	private Pair<PublicationEvent, Offset> convertEvent(Pair<com.digigladd.helloan.publication.impl.PublicationEvent, Offset> pair) {
		PublicationEvent evt;
		if (pair.first() instanceof com.digigladd.helloan.publication.impl.PublicationEvent.PublicationAdded) {
			com.digigladd.helloan.publication.impl.Publication publication = ((com.digigladd.helloan.publication.impl.PublicationEvent.PublicationAdded) pair.first()).getPublication();
			evt = new PublicationEvent.PublicationAdded(publication.ref, publication.numeroGrebiche, publication.nrSeance);
		} else {
			evt = new PublicationEvent.ToIgnore();
		}
		return Pair.create(evt, pair.second());
	}
	
}
