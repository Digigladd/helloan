/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.Service.named;

public interface PublicationService extends Service {
	
	ServiceCall<NotUsed, Optional<Publication>> getPublication(String numeroGrebiche);
	
	ServiceCall<NotUsed, PSequence<Publication>> listPublications();
	
	@Override
	default Descriptor descriptor() {
		return named("publication")
				.withCalls(
						pathCall("/api/publication/:numeroGrebiche", this::getPublication),
						pathCall("/api/publications", this::listPublications)
				)
				.withAutoAcl(true);
	}
}
