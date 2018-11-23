/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.publication.api;

import akka.NotUsed;
import com.digigladd.helloan.utils.Constants;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

import java.util.Optional;
import static com.lightbend.lagom.javadsl.api.Service.*;

public interface PublicationService extends Service {
	
	ServiceCall<NotUsed, Optional<Publication>> getPublication(String numeroGrebiche);
	
	ServiceCall<NotUsed, PSequence<Publication>> listPublications();
	
	ServiceCall<NotUsed, PSequence<String>> sync();
	
	ServiceCall<NotUsed, NotUsed> status();
	
	Topic<PublicationEvent> pubEvents();
	
	@Override
	default Descriptor descriptor() {
		return named("publication")
				.withCalls(
						restCall(Method.GET, "/api/publication/:numeroGrebiche", this::getPublication),
						pathCall("/api/publications", this::listPublications),
						restCall(Method.POST, "/api/publication/sync", this::sync),
						restCall(Method.HEAD, "/api/publication/status", this::status)
				)
				.withTopics(
						topic(Constants.PUBLICATION_EVENTS, this::pubEvents)
				)
				.withAutoAcl(true);
	}
}
