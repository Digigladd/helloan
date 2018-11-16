package com.digigladd.helloan.sync.impl;

import com.digigladd.helloan.sync.impl.actors.SyncActor;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import com.digigladd.helloan.sync.api.SyncService;
import play.libs.akka.AkkaGuiceSupport;

/**
 * The module that binds the SyncService so that it can be served.
 */
public class SyncModule extends AbstractModule implements ServiceGuiceSupport, AkkaGuiceSupport {
    @Override
    protected void configure() {
        bindService(SyncService.class, SyncServiceImpl.class);
        bindActor(SyncActor.class, "syncActor");
    }
}
