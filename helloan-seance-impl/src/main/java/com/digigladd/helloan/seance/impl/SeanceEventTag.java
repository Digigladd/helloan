/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class SeanceEventTag {
	public static final AggregateEventTag<SeanceEvent> INSTANCE = AggregateEventTag.of(SeanceEvent.class);
}
