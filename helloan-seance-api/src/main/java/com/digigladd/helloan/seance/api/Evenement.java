/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import org.pcollections.PSequence;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type",
		defaultImpl = Void.class)
@JsonSubTypes({
		@JsonSubTypes.Type(Evenement.Para.class),
		@JsonSubTypes.Type(Evenement.Nota.class),
		@JsonSubTypes.Type(Evenement.Section.class),
		@JsonSubTypes.Type(Evenement.SousSection.class),
		@JsonSubTypes.Type(Evenement.SousSection2.class),
		@JsonSubTypes.Type(Evenement.ResultatVote.class)
})
public interface Evenement {
	
	@JsonTypeName("para")
	@Value
	@JsonDeserialize
	final class Para implements Evenement {
		private final Optional<String> orateur;
		private final Optional<String> qualiteOrateur;
		private final Optional<String> message;
	}
	
	@JsonTypeName("nota")
	@Value
	@JsonDeserialize
	final class Nota implements Evenement {
		private final Optional<String> message;
	}
	
	@JsonTypeName("section")
	@Value
	@JsonDeserialize
	final class Section implements Evenement {
		private final Optional<String> message;
	}
	
	@JsonTypeName("sous_section1")
	@Value
	@JsonDeserialize
	final class SousSection implements Evenement {
		private final Optional<String> message;
	}
	
	@JsonTypeName("sous_section2")
	@Value
	@JsonDeserialize
	final class SousSection2 implements Evenement {
		private final Optional<String> message;
	}
	
	@JsonTypeName("resultat_vote")
	@Value
	@JsonDeserialize
	final class ResultatVote implements Evenement {
		private final PSequence<Valeur> valeurs;
	}
}
