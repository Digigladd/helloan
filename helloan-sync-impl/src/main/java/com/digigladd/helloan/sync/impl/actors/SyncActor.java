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

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
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
import scala.concurrent.duration.FiniteDuration;

public class SyncActor extends AbstractActorWithTimers {
	
	private final Logger log = LoggerFactory.getLogger(SyncActor.class);
	
	private final PersistentEntityRegistry persistentEntityRegistry;
	private final Http http = Http.get(context().system());
	private final Materializer materializer = ActorMaterializer.create(context());
	private final PersistentEntityRef<SyncCommand> ref;
	private final Environment environment;
	private final ExecutionContextExecutor executor;
	private ConcurrentLinkedQueue<String> datasetQueue = new ConcurrentLinkedQueue<>();
	private SyncState state = null;
	private boolean parsing = false;
	
	public static Props props() {
		return Props.create(SyncActor.class);
	}
	
	public static final class Tick {
	
	}
	
	public static final class Fetch {
		public final Optional<String> ref;
		
		public Fetch(String ref) {
			this.ref = Optional.of(ref);
		}
	}
	
	
	@Inject
	public SyncActor(PersistentEntityRegistry persistentEntityRegistry,
					 Environment environment) {
		this.persistentEntityRegistry = persistentEntityRegistry;
		this.persistentEntityRegistry.register(SyncEntity.class);
		this.ref = persistentEntityRegistry.refFor(SyncEntity.class, Constants.SYNC_ENTITY_ID).withAskTimeout(FiniteDuration.create(3, TimeUnit.SECONDS));
		this.environment = environment;
		this.executor = context().dispatcher();
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Tick.class, this::tick)
				.match(Fetch.class, this::fetch)
				.match(SyncCommand.AddDataset.class, this::handleEntityCommand)
				.match(SyncState.class, this::handleSyncState)
				.build();
	}
	
	private void handleSyncState(SyncState state) {
		this.state = state;
		self().tell(new Tick(), self());
	}
	
	private void fetch(Fetch fetch) {
		log.info("fetch {}", fetch.ref.get());
		if (fetch.ref.isPresent()) {
			this.fetchDataset(fetch.ref.get());
		}
	}
	
	private void tick(Tick tick) {
		//a tick triggers a full parsing of the external repo to get the list of all datasets available.
		log.info("tick");
		if (state != null) {
			if (!parsing) {
				parsing = true;
				CompletionStage<PSet<String>> datasets = CompletableFuture.completedFuture(HashTreePSet.empty());
				
				for (CompletionStage<Set<String>> stage : Stream.iterate(Integer.parseInt(Constants.START_YEAR), n -> n + 1)
						.limit(Integer.parseInt(getCurrentYear()) - Integer.parseInt(Constants.START_YEAR) + 1)
						.map(this::getDatasets)
						.collect(Collectors.toList())) {
					datasets = datasets.thenCombine(
							stage, (d1, d2) -> d1.plusAll(d2)
					);
				}
				
				
				datasets.thenAccept(
						set -> {
							datasetQueue.addAll(set);
							createDataset();
						}
				);
			}
		} else {
			CompletionStage<SyncState> currentState = ref.ask(new SyncCommand.Get());
			currentState.exceptionally(t -> {
				log.error("tick, get failed {}",t.getMessage());
				self().tell(new Tick(), self());
				return null;
			});
			currentState.thenAccept(
					state -> {
						this.state = state;
						self().tell(new Tick(),self());
					}
			);
		}
		
		
		
	}
	
	private void createDataset() {
		if (datasetQueue.size() > 0) {
			log.info("createDataset, queue size {}", datasetQueue.size());
			self().tell(new SyncCommand.AddDataset(datasetQueue.poll()), self());
		} else {
			log.info("createDataset, no more dataset to fetch");
			parsing = false;
			log.info("Schedule next time of redemption, {} datasets to fetch", datasetQueue.size());
			getTimers().startSingleTimer("time-of-redemption", new Tick(), Duration.ofHours(12));
		}
	}
	
	private CompletionStage<Done> handleEntityCommand(SyncCommand.AddDataset cmd) {
		return ref.ask(cmd).thenApply(
				done -> {
					log.info("handleEntityCommand {}, {}", cmd, done);
					createDataset();
					return Done.getInstance();
				}
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
								datasets.add(ref);
								log.info("Found Dataset {}, all = {}", ref, datasets.size());
							}
						} catch (Exception e) {
							log.info("Error while decoding url {}: {}",matcher.group(1),e);
						}
					}
					return datasets;
				}
		);
	}
	
	private CompletionStage<Done> fetchDataset(final String fetch) {
		log.info("fetchDataset {}", fetch);
		String url = Urls.getDataset(fetch);
		Path uploadPath = Constants.getDatasetPath(fetch);
		if (!uploadPath.toFile().exists()) {
			try {
				FileUtils.copyURLToFile(new URL(url), uploadPath.toFile());
				return ref.ask(new SyncCommand.FetchDataset(fetch, uploadPath.toFile().length()));
			} catch (Exception e) {
				log.error("fetchDataset error {}: {}", fetch, e);
				return CompletableFuture.completedFuture(Done.getInstance());
			}
		} else {
			log.info("fetchDataset skip downloading {}", fetch);
			final long length = uploadPath.toFile().length();
			if (length > state.getDatasets()
					.stream()
					.filter(
							f -> f.getRef().equalsIgnoreCase(fetch)
					)
					.mapToLong(
							l -> l.size
					)
					.boxed()
					.findFirst()
					.orElse(0L)) {
				return ref.ask(new SyncCommand.FetchDataset(fetch, uploadPath.toFile().length()));
			} else {
				log.info("fetchDataset skip cmd {}", fetch);
				return CompletableFuture.completedFuture(Done.getInstance());
			}
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
