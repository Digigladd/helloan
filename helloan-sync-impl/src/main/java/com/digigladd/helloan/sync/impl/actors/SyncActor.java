/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.sync.impl.actors;

import akka.Done;
import akka.actor.AbstractActorWithTimers;
import akka.actor.Props;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.http.scaladsl.model.headers.HttpEncodings;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.digigladd.helloan.sync.impl.*;
import com.digigladd.helloan.utils.Constants;
import com.digigladd.helloan.utils.Urls;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import static akka.pattern.PatternsCS.pipe;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import akka.http.javadsl.coding.Coder;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import play.Environment;
import scala.concurrent.ExecutionContextExecutor;

public class SyncActor extends AbstractActorWithTimers {
	
	private final Logger log = LoggerFactory.getLogger(SyncActor.class);
	
	private final PersistentEntityRegistry persistentEntityRegistry;
	private final Http http = Http.get(context().system());
	private final Materializer materializer = ActorMaterializer.create(context());
	private final PersistentEntityRef<SyncCommand> ref;
	private final Environment environment;
	private final ExecutionContextExecutor executor;
	
	public static Props props() {
		return Props.create(SyncActor.class);
	}
	
	public static final class Tick {
	
	}
	
	public static final class Fetch {
		public final Optional<String> ref;
		
		public Fetch() {
			this.ref = Optional.empty();
		}
		
		public Fetch(String ref) {
			this.ref = Optional.of(ref);
		}
	}
	
	
	@Inject
	public SyncActor(PersistentEntityRegistry persistentEntityRegistry,
					 Environment environment) {
		this.persistentEntityRegistry = persistentEntityRegistry;
		this.persistentEntityRegistry.register(SyncEntity.class);
		this.ref = persistentEntityRegistry.refFor(SyncEntity.class, Constants.SYNC_ENTITY_ID);
		this.environment = environment;
		this.executor = context().dispatcher();
		//need a initial tick!
		getTimers().startSingleTimer("time-of-origin", new Tick(), Duration.ofSeconds(30));
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Tick.class, this::tick)
				.match(Fetch.class, this::fetch)
				.build();
	}
	
	private void fetch(Fetch fetch) {
		log.info("Received Fetch! {}", fetch);
		final String lastRef = fetch.ref.orElse("");
		ref.ask(new SyncCommand.Get()).thenApply(
				state -> {
					Optional<Dataset> dataset = state.getDatasets().stream().filter(f -> !f.fetched && f.getRef() != lastRef).findFirst();
					if (dataset.isPresent()) {
						return this.fetchDataset(dataset.get().getRef());
					} else {
						log.info("No more dataset to fetch, schedule a tick in 12 hours");
						getTimers().startSingleTimer("time-of-redemption", new Tick(), Duration.ofHours(12));
						return CompletableFuture.completedFuture(Done.getInstance());
					}
				}
		);
	}
	
	private void tick(Tick tick) {
		//a tick triggers a full parsing of the external repo to get the list of all datasets available.
		log.info("Received tick!");
		CompletionStage<PSet<String>> datasets = CompletableFuture.completedFuture(HashTreePSet.empty());
		for (CompletionStage<Set<String>> stage : Stream.iterate(Integer.parseInt(Constants.START_YEAR), n -> n +1)
				.limit(Integer.parseInt(getCurrentYear())-Integer.parseInt(Constants.START_YEAR)+1)
				.map(this::getDatasets)
				.collect(Collectors.toList())) {
			datasets = datasets.thenCombine(
					stage, (d1,d2) -> d1.plusAll(d2)
			);
		}
		datasets.thenApply(
				set -> pipe(ref.ask(new SyncCommand.AddDatasets(set)), executor).to(self())
		);
	}
	
	private CompletionStage<Set<String>> getDatasets(final Integer year) {
		String url = Urls.getYear(String.valueOf(year));
		log.info("Parsing {}", year);
		CompletableFuture<HttpResponse> getResponse = http.singleRequest(HttpRequest.create(url)).toCompletableFuture();
		CompletableFuture<HttpResponse> getDecodedResponse = getResponse.thenApply(decodeResponse);
		CompletableFuture<String> getBody = getDecodedResponse.thenCompose(
				decoded -> Unmarshaller.entityToString().unmarshal(decoded.entity(), materializer)
		);
		return getBody.thenApply(
				body -> {
					Matcher matcher = Constants.linkPattern.matcher(body);
					Set<String> datasets = new HashSet<>();
					while (matcher.find()) {
						try {
							String href = java.net.URLDecoder.decode(matcher.group(1).replaceAll("\"","").replaceAll("'",""), "UTF-8");
							if (href.startsWith(Constants.DATA_ROOT_DIR)) {
								String ref = Urls.extractRef(href);
								log.info("Found Dataset {}", ref);
								datasets.add(ref);
							}
						} catch (Exception e) {
							log.info("Error while decoding url {}: {}",matcher.group(1),e);
						}
					}
					return datasets;
				}
		);
	}
	
	private CompletionStage<Done> fetchDataset(String fetch) {
		log.info("Fetch dataset {}!", fetch);
		String url = Urls.getDataset(fetch);
		Path uploadPath = Constants.getDatasetPath(fetch);
		
		try {
			FileUtils.copyURLToFile(new URL(url), uploadPath.toFile());
			log.info("Dataset uploaded, size {}",uploadPath.toFile().length());
			return ref.ask(new SyncCommand.FetchDataset(fetch, uploadPath.toFile().length()));
		} catch (Exception e) {
			log.error("Error while downloading {}: {}", fetch, e);
			return CompletableFuture.completedFuture(Done.getInstance());
		}
	}
	
	private String getCurrentYear() {
		return String.valueOf(LocalDate.now().getYear());
	}
	
	private LocalDateTime getNow() {
		return LocalDateTime.now();
	}
	
	final Function<HttpResponse, HttpResponse> decodeResponse = response -> {
		// Pick the right coder
		final Coder coder;
		if (HttpEncodings.gzip().equals(response.encoding())) {
			coder = Coder.Gzip;
		} else if (HttpEncodings.deflate().equals(response.encoding())) {
			coder = Coder.Deflate;
		} else {
			coder = Coder.NoCoding;
		}
		
		// Decode the entity
		return coder.decodeMessage(response);
	};
}
