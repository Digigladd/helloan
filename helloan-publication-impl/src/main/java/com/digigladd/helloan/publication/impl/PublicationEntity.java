/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

public class PublicationEntity extends PersistentEntity<PublicationCommand, PublicationEvent, PublicationState> {
	@Override
	public Behavior initialBehavior(Optional<PublicationState> snapshotState) {
		BehaviorBuilder b = newBehaviorBuilder(
				snapshotState.orElse(PublicationState.empty())
		);
		
		b.setCommandHandler(PublicationCommand.AddPublication.class, (cmd, ctx) ->
			ctx.thenPersist(new PublicationEvent.PublicationAdded(
					cmd.getPublication()
			), evt -> ctx.reply(Done.getInstance()))
		);
		
		b.setEventHandler(PublicationEvent.PublicationAdded.class, evt -> new PublicationState(
				Optional.of(evt.getPublication())
		));
		
		b.setReadOnlyCommandHandler(PublicationCommand.Get.class, (cmd, ctx) -> ctx.reply(state().getPublication()));
		
		return b.build();
	}
}
