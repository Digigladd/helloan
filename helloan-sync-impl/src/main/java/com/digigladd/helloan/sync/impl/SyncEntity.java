package com.digigladd.helloan.sync.impl;

import akka.Done;
import com.digigladd.helloan.sync.api.SyncStatus;
import com.digigladd.helloan.sync.impl.actors.SyncActor;
import com.digigladd.helloan.utils.Constants;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import com.digigladd.helloan.sync.impl.SyncCommand.*;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import org.pcollections.HashTreePSet;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.text.html.Option;
import java.security.cert.CollectionCertStoreParameters;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyncEntity extends PersistentEntity<SyncCommand, SyncEvent, SyncState> {
    
    private final Logger log = LoggerFactory.getLogger(SyncEntity.class);
    
    @Override
    public Behavior initialBehavior(Optional<SyncState> snapshotState) {
		log.info("Initial behavior from state: {}", snapshotState.orElse(SyncState.EMPTY));
		BehaviorBuilder b = newBehaviorBuilder(
        	snapshotState.orElse(SyncState.EMPTY)
        );
        
        b.setCommandHandler(AddDataset.class, (cmd, ctx) -> {
        			String toAdd = cmd.getRef();
        			boolean alreadyExists = state().getDatasets().stream().filter(f -> f.getRef() == toAdd).count() > 0;
        			log.info("{} dataset is already present {}", toAdd, alreadyExists);
        			
        			if (!alreadyExists) {
        				final SyncEvent.DatasetAdded datasetsAdded = new SyncEvent.DatasetAdded(toAdd, Optional.of(LocalDateTime.now()));
                        return ctx.thenPersist(datasetsAdded, evt -> {
                        			ctx.reply(Done.getInstance());
								}
                        );
                    } else {
        				log.info("No dataset to add");
                        return ctx.done();
                    }
                }
        );
        
        b.setCommandHandler(FetchDataset.class, (cmd, ctx) -> {
            final SyncEvent.DatasetFetched datasetFetched = new SyncEvent.DatasetFetched(cmd.getRef(),cmd.getSize());
            
            return ctx.thenPersist(datasetFetched, evt -> ctx.reply(Done.getInstance()));
        });

        b.setReadOnlyCommandHandler(Get.class, (cmd, ctx) -> {
        			ctx.reply(state());
				}
        
        );
        
        b.setEventHandler(SyncEvent.DatasetAdded.class, evt -> {
            Optional<LocalDateTime> lastParsed = state().getLastParsed();
            SyncState newState = new SyncState(
					Optional.of(
							state().getDatasets().plus(new Dataset(evt.dataset, Optional.empty(), Optional.empty()))
					),
					lastParsed
			);
            log.info("Dataset added {}", evt.getDataset());
            return newState;
        });
        
        b.setEventHandler(SyncEvent.DatasetFetched.class, evt -> {
            SyncState newState = new SyncState(
					Optional.of(
							TreePVector.from(state().getDatasets().stream().map(
									dataset -> {
										if (dataset.ref == evt.ref) {
											return new Dataset(evt.getRef(), Optional.of(evt.getSize()), Optional.of(true));
										} else {
											return dataset;
										}
									}
							).collect(Collectors.toList()))),
					state().lastParsed
			);
            log.info("Dataset fetched {}", evt.getRef());
            return newState;
        });
        return b.build();
    }
    
}
