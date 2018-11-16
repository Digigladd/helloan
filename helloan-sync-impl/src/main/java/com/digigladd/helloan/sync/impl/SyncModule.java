package com.digigladd.helloan.sync.impl;

import com.digigladd.helloan.sync.impl.actors.SyncActor;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.client.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import com.digigladd.helloan.sync.api.SyncService;
import com.typesafe.config.Config;
import play.Environment;
import play.libs.akka.AkkaGuiceSupport;

/**
 * The module that binds the SyncService so that it can be served.
 */
public class SyncModule extends AbstractModule implements ServiceGuiceSupport, AkkaGuiceSupport {
    
    private final Environment environment;
    private final Config config;
    
    public SyncModule(Environment environment,
                        Config config) {
        this.environment = environment;
        this.config = config;
    }
    
    @Override
    protected void configure() {
    
        if (environment.isProd()) {
            bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);
        }
        
        bindService(SyncService.class, SyncServiceImpl.class);
        bindActor(SyncActor.class, "syncActor");
    }
}
