/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import lombok.Data;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.Collections;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
@Data
public class CompteRendu {
	String presidentSeance;
	String qualitePresident;
	PSequence<Object> evenements = TreePVector.empty();
	
	public void addEvenement(Object evenement) {
		this.evenements = this.evenements.plus(evenement);
	}
	
	@Override
	public String toString() {
		String temp = this.presidentSeance +"\n"+this.qualitePresident+"\n";
		temp += String.join("\n",this.evenements.stream().map(Object::toString).collect(Collectors.toList()));
		return temp;
	}
}
