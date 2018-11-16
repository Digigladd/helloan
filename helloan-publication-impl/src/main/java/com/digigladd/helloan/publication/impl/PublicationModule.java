/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.impl;

import com.digigladd.helloan.publication.api.PublicationService;
import com.digigladd.helloan.seance.api.SeanceService;
import com.digigladd.helloan.sync.api.SyncService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.client.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.typesafe.config.Config;
import play.Environment;
import play.libs.akka.AkkaGuiceSupport;

public class PublicationModule extends AbstractModule implements ServiceGuiceSupport, AkkaGuiceSupport {
	
	private final Environment environment;
	private final Config config;
	
	public PublicationModule(Environment environment,
							 Config config) {
		this.environment = environment;
		this.config = config;
	}
	
	@Override
	protected void configure() {
		bindService(PublicationService.class, PublicationServiceImpl.class);
		bindClient(SyncService.class);
		bindClient(SeanceService.class);
		if (environment.isProd()) {
			bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);
		}
	}
}
