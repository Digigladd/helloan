package com.digigladd.helloan.sync.impl;

import akka.Done;
import com.digigladd.helloan.sync.impl.actors.SyncActor;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import com.digigladd.helloan.sync.impl.SyncCommand.*;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.security.cert.CollectionCertStoreParameters;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyncEntity extends PersistentEntity<SyncCommand, SyncEvent, SyncState> {
    
    private final Logger log = LoggerFactory.getLogger(SyncEntity.class);
    @Override
    public Behavior initialBehavior(Optional<SyncState> snapshotState) {
        
        BehaviorBuilder b = newBehaviorBuilder(
            snapshotState.orElse(new SyncState(Optional.empty(), Optional.empty(), Optional.empty()))
        );
        
        b.setCommandHandler(AddDatasets.class, (cmd, ctx) -> {
                    List<String> newRef = cmd.getRefs().stream().filter(ref -> state().addDataset(ref)).collect(Collectors.toList());
                    log.info("Sync need to add {} datasets",newRef.size());
                    if (newRef.size() > 0) {
                        final SyncEvent.DatasetsAdded datasetsAdded = new SyncEvent.DatasetsAdded(newRef, cmd.getYear(), Optional.of(LocalDateTime.now()));
                        return ctx.thenPersist(datasetsAdded, evt -> ctx.reply(Done.getInstance()));
                    } else {
                        return ctx.done();
                    }
                }
        );
        
        b.setCommandHandler(AddYear.class, (cmd, ctx) -> {
            if (state().addYear(cmd.getYear())) {
                log.info("Sync need to add {}", cmd.getYear());
                final SyncEvent.YearAdded yearAdded = new SyncEvent.YearAdded(cmd.getYear());
                return ctx.thenPersist(yearAdded, evt -> ctx.reply(Done.getInstance()));
            } else {
                return ctx.done();
            }
        });
        
        b.setCommandHandler(FetchDataset.class, (cmd, ctx) -> {
            final SyncEvent.DatasetFetched datasetFetched = new SyncEvent.DatasetFetched(cmd.getRef(),cmd.getSize());
            
            return ctx.thenPersist(datasetFetched, evt -> ctx.reply(Done.getInstance()));
        });

        b.setReadOnlyCommandHandler(Get.class, (cmd, ctx) -> {
        			ctx.reply(state());
				}
        
        );
        
        b.setEventHandler(SyncEvent.DatasetsAdded.class, evt -> {
            log.info("Datasets added {}", evt.datasets.size());
            Optional<LocalDateTime> lastParsed = state().getLastParsed();
            if (evt.getYear() == String.valueOf(LocalDateTime.now().getYear())) {
                lastParsed = evt.getLastParsed();
            }
            return new SyncState(
                    Optional.of(state().getParsedYears()),
                    Optional.of(
                            TreePVector.from(Stream.concat(
                                state().getDatasets().stream(),
                                evt.datasets.stream().map(
                                    m -> state().createDataset(m)
                                )
                            ).collect(Collectors.toList()))
                    ),
                    lastParsed
            );
        });
        
        b.setEventHandler(SyncEvent.YearAdded.class, evt -> {
            log.info("Year added {}", evt.getYear());
            String currentYear = String.valueOf(LocalDate.now().getYear());
            Optional<LocalDateTime> lastParsed = Optional.empty();
            if (currentYear.equalsIgnoreCase(evt.getYear())) {
                lastParsed = Optional.of(evt.lastParsed);
            }
            return new SyncState(
                    Optional.of(
                            state().getParsedYears().plusAll(Stream.of(evt.getYear()).collect(Collectors.toList()))
                    ),
                    Optional.of(state().getDatasets()),
                    lastParsed
            );
        });
        
        b.setEventHandler(SyncEvent.DatasetFetched.class, evt -> {
            log.info("Dataset fetched {}: {} bytes", evt.ref, evt.size);
            return new SyncState(
              Optional.of(state().getParsedYears()),
              Optional.of(
                      TreePVector.from(state().getDatasets().stream().map(
                      dataset -> {
                          if (dataset.ref == evt.ref) {
                              return state().datasetFetched(evt.ref,evt.size);
                          } else {
                              return dataset;
                          }
                      }
              ).collect(Collectors.toList()))),
              state().lastParsed
            );
        });
        return b.build();
    }
    
}
