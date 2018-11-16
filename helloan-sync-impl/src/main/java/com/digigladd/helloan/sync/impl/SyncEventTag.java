/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class SyncEventTag {
	public static final AggregateEventTag<SyncEvent> INSTANCE = AggregateEventTag.of(SyncEvent.class);
}
