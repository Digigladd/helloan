/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

public class SeanceEntity extends PersistentEntity<SeanceCommand, SeanceEvent, SeanceState> {
	@Override
	public Behavior initialBehavior(Optional<SeanceState> snapshotState) {
		BehaviorBuilder b = newBehaviorBuilder(
				snapshotState.orElse(SeanceState.empty())
		);
		
		b.setCommandHandler(SeanceCommand.AddSeance.class, (cmd,ctx) ->
			ctx.thenPersist(
					new SeanceEvent.SeanceAdded(cmd.getSeance()),
					evt -> ctx.reply(Done.getInstance())
			)
		);
		
		b.setEventHandler(SeanceEvent.SeanceAdded.class, evt -> new SeanceState(Optional.of(evt.getSeance())));
		
		b.setReadOnlyCommandHandler(SeanceCommand.Get.class, (cmd,ctx) -> ctx.reply(state().getSeance()));
		return b.build();
	}
}
