/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.management.AkkaManagement;
import com.digigladd.helloan.seance.api.Evenement;
import com.digigladd.helloan.seance.api.PullSeance;
import com.digigladd.helloan.seance.api.SeanceService;
import com.digigladd.helloan.seance.api.Valeur;
import com.digigladd.helloan.utils.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
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
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class SeanceServiceImpl implements SeanceService {
	
	private final PersistentEntityRegistry persistentEntityRegistry;
	
	private final Logger log = LoggerFactory.getLogger(SeanceServiceImpl.class);
	
	@Inject
	public SeanceServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
							 ActorSystem system) {
		this.persistentEntityRegistry = persistentEntityRegistry;
		this.persistentEntityRegistry.register(SeanceEntity.class);
	}
	
	@Override
	public ServiceCall<PullSeance, Done> pull() {
		return request ->
		{
			CompteRendu compteRendu = ArchiveParser.getCompteRendu(request.publication, request.session);
			String id = request.numeroGrebiche + "#" + request.session;
			Seance seance = new Seance(
					compteRendu.getPresidentSeance(),
					compteRendu.getQualitePresident(),
					TreePVector.from(compteRendu.getEvenements()
							.stream()
							.map(
									this::convert
							)
							.collect(Collectors.toList()))
			
			);
			return entityRef(id).ask(new SeanceCommand.AddSeance(seance));
		};
	}
	
	@Override
	public ServiceCall<NotUsed, Optional<com.digigladd.helloan.seance.api.Seance>> get(String id, String session) {
		return request -> {
			String uid = id+"#"+session;
			return entityRef(uid).ask(new SeanceCommand.Get()).thenApply(
					state -> state.map(
							this::toApi
					)
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
	
	private SeanceTag convert(Object tag) {
		SeanceTag seanceTag = null;
		if (tag instanceof Para) {
			seanceTag = convertPara((Para)tag);
		} else if (tag instanceof Nota) {
			seanceTag = convertNota((Nota)tag);
		} else if (tag instanceof Section) {
			seanceTag = convertSection((Section)tag);
		} else if (tag instanceof SousSection1) {
			seanceTag = convertSection((SousSection1)tag);
		} else if (tag instanceof SousSection2) {
			seanceTag = convertSection((SousSection2)tag);
		} else if (tag instanceof ResultatVote) {
			seanceTag = convertResultatVote((ResultatVote)tag);
		}
		return seanceTag;
	}
	
	private SeanceTag convertPara(Para para) {
		Optional<Orateur> orateur = Optional.empty();
		if (!para.getOrateur().isEmpty()) {
			Optional<String> qualite = Optional.empty();
			if (!para.getQualite().isEmpty()) {
				qualite = Optional.of(para.getQualite());
			}
			orateur = Optional.of(new Orateur(para.getOrateur(),qualite));
		}
		return new SeanceTag(
				SeanceTagType.PARA,
				orateur,
				Optional.of(para.getText()),
				Optional.empty()
		);
	}
	
	private SeanceTag convertNota(Nota nota) {
		return new SeanceTag(
				SeanceTagType.NOTA,
				Optional.empty(),
				Optional.of(nota.getText()),
				Optional.empty()
		);
	}
	
	private SeanceTag convertSection(Section section) {
		return new SeanceTag(
				SeanceTagType.SECTION,
				Optional.empty(),
				Optional.of(section.getText()),
				Optional.empty()
		);
	}
	
	private SeanceTag convertSection(SousSection1 section) {
		return new SeanceTag(
				SeanceTagType.SOUS_SECTION_1,
				Optional.empty(),
				Optional.of(section.getText()),
				Optional.empty()
		);
	}
	
	private SeanceTag convertSection(SousSection2 section) {
		return new SeanceTag(
				SeanceTagType.SOUS_SECTION_2,
				Optional.empty(),
				Optional.of(section.getText()),
				Optional.empty()
		);
	}
	
	private SeanceTag convertResultatVote(ResultatVote vote) {
		PSequence<Pair<String,Integer>> valeur = TreePVector.from(vote.getValeurs().stream().map(
				m -> Pair.create(m.getLibelle(), m.getValeur())
		).collect(Collectors.toList()));
		
		return new SeanceTag(
				SeanceTagType.RESULTAT_VOTE,
				Optional.empty(),
				Optional.empty(),
				Optional.of(valeur)
		);
	}
	
	private PersistentEntityRef<SeanceCommand> entityRef(String id) {
		return this.persistentEntityRegistry.refFor(SeanceEntity.class, id);
	}
	
	private com.digigladd.helloan.seance.api.Seance toApi(Seance seance) {
		return new com.digigladd.helloan.seance.api.Seance(
			seance.getPresidentSeance(),
			seance.getQualitePresident(),
			TreePVector.from(seance.getEvents().stream().map(this::toEvenement).collect(Collectors.toList()))
		);
	}
	
	private Evenement toEvenement(SeanceTag tag) {
		Evenement e = null;
		switch (tag.getType()) {
			case PARA:
				e = toPara(tag);
				break;
			case NOTA:
				e = toNota(tag);
				break;
			case SECTION:
				e = toSection(tag);
				break;
			case SOUS_SECTION_1:
				e = toSousSection(tag);
				break;
			case SOUS_SECTION_2:
				e = toSousSection2(tag);
				break;
			case RESULTAT_VOTE:
				e = toResultatVote(tag);
				break;
		}
		return e;
	}
	
	private Evenement.Para toPara(SeanceTag tag) {
		if (tag.getOrateur().isPresent()) {
			return new Evenement.Para(
					Optional.of(tag.getOrateur().get().getNom()),
					tag.getOrateur().get().getQualite(),
					tag.getText()
			);
		} else {
			return new Evenement.Para(
					Optional.empty(),
					Optional.empty(),
					tag.getText()
			);
		}
	}
	
	private Evenement.Nota toNota(SeanceTag tag) {
		return new Evenement.Nota(
				tag.getText()
		);
	}
	
	private Evenement.Section toSection(SeanceTag tag) {
		return new Evenement.Section(
				tag.getText()
		);
	}
	
	private Evenement.SousSection toSousSection(SeanceTag tag) {
		return new Evenement.SousSection(
				tag.getText()
		);
	}
	
	private Evenement.SousSection2 toSousSection2(SeanceTag tag) {
		return new Evenement.SousSection2(
				tag.getText()
		);
	}
	
	private Evenement.ResultatVote toResultatVote(SeanceTag tag) {
		PSequence<Valeur> valeurs = TreePVector.empty();
		if (tag.getResultatVote().isPresent()) {
			valeurs = TreePVector.from(tag.getResultatVote().get().stream().map(
					pair -> new Valeur(pair.first(),pair.second())
			).collect(Collectors.toList()));
		}
		return new Evenement.ResultatVote(
				valeurs
		);
	}
}
