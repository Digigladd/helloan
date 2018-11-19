package com.digigladd.helloan.sync.api;

import akka.NotUsed;
import com.digigladd.helloan.utils.Constants;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * The sync service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the SyncService.
 */
public interface SyncService extends Service {
    /**
     * Example:
     * curl http://localhost:9000/api/hello/Alice
     */
    ServiceCall<NotUsed, SyncStatus> hello();
    
    Topic<SyncEvent> syncEvents();
    
    ServiceCall<NotUsed, NotUsed> status();
    
    @Override
    default Descriptor descriptor() {
        return named("sync")
                .withCalls(
                        pathCall("/api/sync/hello", this::hello),
                        pathCall("/status", this::status)
                )
                .withTopics(
                        topic(Constants.SYNC_EVENTS, this::syncEvents)
                )
                .withAutoAcl(true);
    }
}
