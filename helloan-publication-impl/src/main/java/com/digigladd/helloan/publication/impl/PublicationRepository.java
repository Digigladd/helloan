/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import akka.Done;
import com.datastax.driver.core.*;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.digigladd.helloan.utils.CompletionStageUtils.doAll;
import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatements;


@Singleton
public class PublicationRepository {
	private final CassandraSession session;
	
	@Inject
	public PublicationRepository(CassandraSession session, ReadSide readSide) {
		this.session = session;
		readSide.register(PublicationEventProcessor.class);
	}
	
	CompletionStage<PSequence<Publication>> getPublications() {
		return session
				.selectAll(
						"SELECT * FROM publication"
				)
				.thenApply(
						List::stream
				)
				.thenApply(
						rows -> rows.map(
								PublicationRepository::toPublication
						)
				)
				.thenApply(
						publications -> publications.collect(Collectors.toList())
				)
				.thenApply(
						TreePVector::from
				);
	}
	
	private static Publication toPublication(Row publication) {
		return new Publication(
				LocalDate.ofEpochDay(publication.getDate("dateParution").getDaysSinceEpoch()),
				publication.getInt("numParution"),
				publication.getString("numeroGrebiche"),
				LocalDate.ofEpochDay(publication.getDate("dateSeance").getDaysSinceEpoch()),
				publication.getInt("nrSeance"),
				LocalDate.ofEpochDay(publication.getDate("periodeDu").getDaysSinceEpoch()),
				LocalDate.ofEpochDay(publication.getDate("periodeAu").getDaysSinceEpoch()),
				publication.getString("typeSession"),
				publication.getInt("numSeance")
		);
	}
	
	private static class PublicationEventProcessor extends ReadSideProcessor<PublicationEvent> {
		
		private final CassandraSession session;
		private final CassandraReadSide readSide;
		
		private PreparedStatement insertPublicationStatement;
		
		@Inject
		public PublicationEventProcessor(CassandraSession session, CassandraReadSide readSide) {
			this.session = session;
			this.readSide = readSide;
		}
		
		@Override
		public ReadSideHandler<PublicationEvent> buildHandler() {
			return readSide.<PublicationEvent>builder("publicationEventOffset")
					.setGlobalPrepare(this::createTables)
					.setPrepare(tag -> prepareStatements())
					.setEventHandler(PublicationEvent.PublicationAdded.class, e -> insertPublication(e.getPublication()))
					.build();
		}
		
		@Override
		public PSequence<AggregateEventTag<PublicationEvent>> aggregateTags() {
			TreePVector<AggregateEventTag<PublicationEvent>> pVector = TreePVector.empty();
			return pVector.plus(PublicationEventTag.INSTANCE);
		}
		
		private CompletionStage<Done> createTables() {
			return
					doAll(
						session.executeCreateTable(
								"CREATE TABLE IF NOT EXISTS publication (" +
										"dateParution date,"+
										"numParution int,"+
										"numeroGrebiche varchar PRIMARY KEY,"+
										"dateSeance date,"+
										"nrSeance int,"+
										"numSeance int,"+
										"periodeDu date,"+
										"periodeAu date,"+
										"typeSession varchar"+
										")"
						)
					);
			
		}
		
		private void registerCodec(Session session, TypeCodec<?> codec) {
			session.getCluster().getConfiguration().getCodecRegistry().register(codec);
		}
		
		private CompletionStage<Done>  prepareStatements() {
			return doAll(
					session.underlying().thenAccept(
							s -> registerCodec(s, LocalDateCodec.instance)
					),
					prepareInsertPublicationStatement()
			);
			
		}
		
		private CompletionStage<Done> prepareInsertPublicationStatement() {
			return session.prepare("INSERT INTO publication("+
					"dateParution,"+
					"numParution,"+
					"numeroGrebiche,"+
					"dateSeance,"+
					"nrSeance,"+
					"numSeance,"+
					"periodeDu,"+
					"periodeAu,"+
					"typeSession"+
					") VALUES ("+
					"?, "+
					"?, "+
					"?, "+
					"?, "+
					"?, "+
					"?, "+
					"?, "+
					"?, "+
					"? "+
					")"
			).thenApply(s -> {
				insertPublicationStatement = s;
				return Done.getInstance();
			});
		}
		
		private CompletionStage<List<BoundStatement>> insertPublication(Publication publication) {
			
			return completedStatements(
					insertPublicationStatement.bind(
							publication.dateParution,
							publication.numParution,
							publication.numeroGrebiche,
							publication.dateSeance,
							publication.nrSeance,
							publication.numSeance,
							publication.periodeDu,
							publication.periodeAu,
							publication.typeSession
					)
			);
		}
	}
}
