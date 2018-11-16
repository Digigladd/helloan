/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.impl;


import com.digigladd.helloan.seance.api.SeanceService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import play.libs.akka.AkkaGuiceSupport;

public class SeanceModule extends AbstractModule implements ServiceGuiceSupport, AkkaGuiceSupport {
	@Override
	protected void configure() {
		bindService(SeanceService.class, SeanceServiceImpl.class);
	}
}
