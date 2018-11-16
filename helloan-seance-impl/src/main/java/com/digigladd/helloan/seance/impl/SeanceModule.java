/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;

import com.digigladd.helloan.seance.api.SeanceService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.client.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.typesafe.config.Config;
import play.Environment;
import play.libs.akka.AkkaGuiceSupport;

public class SeanceModule extends AbstractModule implements ServiceGuiceSupport, AkkaGuiceSupport {
	
	private final Environment environment;
	private final Config config;
	
	public SeanceModule(Environment environment,
							 Config config) {
		this.environment = environment;
		this.config = config;
	}
	
	@Override
	protected void configure() {
		
		if (environment.isProd()) {
			bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);
		}
		
		bindService(SeanceService.class, SeanceServiceImpl.class);
	}
}
