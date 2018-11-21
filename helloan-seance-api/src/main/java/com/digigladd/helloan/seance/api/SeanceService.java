/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.seance.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.Service.named;

public interface SeanceService extends Service {
	ServiceCall<NotUsed, Optional<Seance>> get(String id, String session);
	ServiceCall<NotUsed, NotUsed> status();
	
	@Override
	default Descriptor descriptor() {
		return named("seance")
				.withCalls(
						pathCall("/api/seance/:id/:session", this::get),
						pathCall("/api/seance/status", this::status)
				)
				.withAutoAcl(true);
	}
}
