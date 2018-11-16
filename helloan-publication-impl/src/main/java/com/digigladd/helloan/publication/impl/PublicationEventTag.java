/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class PublicationEventTag {
	public static final AggregateEventTag<PublicationEvent> INSTANCE = AggregateEventTag.of(PublicationEvent.class);
}
