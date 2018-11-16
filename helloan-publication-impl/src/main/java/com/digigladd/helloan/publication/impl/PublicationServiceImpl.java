/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.digigladd.helloan.publication.api.Publication;
import com.digigladd.helloan.publication.api.PublicationService;
import com.digigladd.helloan.publication.api.Session;
import com.digigladd.helloan.seance.api.PullSeance;
import com.digigladd.helloan.seance.api.SeanceService;
import com.digigladd.helloan.sync.api.SyncEvent;
import com.digigladd.helloan.sync.api.SyncService;
import com.digigladd.helloan.utils.ArchiveParser;
import com.digigladd.helloan.utils.Metadonnees;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.digigladd.helloan.utils.CompletionStageUtils.doAll;

public class PublicationServiceImpl implements PublicationService {
	private final PersistentEntityRegistry persistentEntityRegistry;
	
	private final PublicationRepository publicationRepository;
	
	private final Logger log = LoggerFactory.getLogger(PublicationServiceImpl.class);
	
	
	@Inject
	public PublicationServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
								  SyncService syncService,
								  PublicationRepository publicationRepository,
								  SeanceService seanceService) {
		this.persistentEntityRegistry = persistentEntityRegistry;
		this.persistentEntityRegistry.register(PublicationEntity.class);
		this.publicationRepository = publicationRepository;
		
		syncService.syncEvents().subscribe().atLeastOnce(
				Flow.fromFunction((SyncEvent message) -> {
					if (message instanceof SyncEvent.DatasetFetched) {
						String ref = ((SyncEvent.DatasetFetched)message).getRef();
						log.info("New dataset to parse {}", ref);
						Metadonnees metadonnees = ArchiveParser.getCRI(ref);
						if (metadonnees != null) {
							log.info("New publication {}, {} meeting(s)", metadonnees.getNumeroGrebiche(), metadonnees.getNrSeance());
							PersistentEntityRef<PublicationCommand> entityRef = this.persistentEntityRegistry.refFor(PublicationEntity.class, metadonnees.getNumeroGrebiche());
							entityRef.ask(PublicationCommand.createAddPublication(
									new com.digigladd.helloan.publication.impl.Publication(
											metadonnees.getDateParution(),
											metadonnees.getNumParution(),
											metadonnees.getNumeroGrebiche(),
											metadonnees.getDateSeance(),
											metadonnees.getNrSeance(),
											metadonnees.getPeriodeDu(),
											metadonnees.getPeriodeAu(),
											metadonnees.getTypeSession(),
											metadonnees.getNumSeance()
									)
							)).thenApply(
									publication -> {
										return doAll(
												Stream.iterate(1, n -> n + 1).limit(metadonnees.getNrSeance()).map(
													nrSeance -> {
														log.info("Request an extraction for meeting {}:{}", ref,nrSeance);
														return seanceService.pull().invoke(new PullSeance(ref, nrSeance,metadonnees.getNumeroGrebiche())).thenApply(
																done -> {
																	log.info("Meeting {}:{} extracted", ref, nrSeance);
																	return done;
																}
														);
													}
												).collect(Collectors.toList())
										);
									}
							);
							return Done.getInstance();
						} else {
							return Done.getInstance();
						}
					} else {
						return Done.getInstance();
					}
				})
		);
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
}
