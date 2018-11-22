/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PublicationEntity extends PersistentEntity<PublicationCommand, PublicationEvent, PublicationState> {
	
	private final Logger log = LoggerFactory.getLogger(PublicationEntity.class);
	
	@Override
	public Behavior initialBehavior(Optional<PublicationState> snapshotState) {
		BehaviorBuilder b = newBehaviorBuilder(
				snapshotState.orElse(PublicationState.empty())
		);
		
		b.setCommandHandler(PublicationCommand.AddPublication.class, (cmd, ctx) -> {
					log.info("Publication added command!");
					return ctx.thenPersist(new PublicationEvent.PublicationAdded(
							cmd.getPublication()
					), evt -> ctx.reply(Done.getInstance()));
				}
			
		);
		
		b.setEventHandler(PublicationEvent.PublicationAdded.class, evt -> new PublicationState(
				Optional.of(evt.getPublication())
		));
		
		b.setReadOnlyCommandHandler(PublicationCommand.Get.class, (cmd, ctx) -> ctx.reply(state().getPublication()));
		
		return b.build();
	}
}
