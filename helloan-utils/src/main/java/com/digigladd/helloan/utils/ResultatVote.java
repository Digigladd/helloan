/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ResultatVote {
	PSequence<LibelleValeur> valeurs = TreePVector.empty();
	
	public void addValeur(LibelleValeur valeur) {
		this.valeurs = this.valeurs.plus(valeur);
	}
	
	@Override
	public String toString() {
		return "Resultat Vote: \n" +
				String.join("\n", this.valeurs.stream().map(m -> m.getLibelle()+": "+m.getValeur()).collect(Collectors.toList()));
	}
}
