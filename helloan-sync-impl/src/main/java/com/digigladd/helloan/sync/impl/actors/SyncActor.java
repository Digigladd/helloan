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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;

import akka.http.javadsl.coding.Coder;
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
	
	private final class GetDatasets {
		public final String year;
		
		public GetDatasets(String year) {
			this.year = year;
		}
	}
	
	private final class Datasets {
		public final String year;
		public final List<String> datasets;
		
		public Datasets(String year, List<String> datasets) {
			this.year = year;
			this.datasets = datasets;
		}
	}
	
	private final class FetchDataset {
		public final String ref;
		
		public FetchDataset(String ref) {
			this.ref = ref;
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
				.match(SyncState.class, this::perform)
				.match(Tick.class, this::tick)
				.match(GetDatasets.class, this::getDatasets)
				.match(Datasets.class, this::saveDatasets)
				.match(FetchDataset.class, this::fetchDataset)
				.build();
	}
	
	private void perform(SyncState state) {
		log.info("Received SyncState!");
		String currentYear = getCurrentYear();
		LocalDateTime now = getNow();
		if (state.getLastParsed().isPresent()) {
			if (now.isAfter(state.getLastParsed().get().plusHours(1))) {
				log.info("Need a refresh!");
				self().tell(new GetDatasets(currentYear), self());
			} else {
				log.info("Check unfetch datasets!");
				Optional<Dataset> dataset = state.getDatasets().stream().filter(f -> f.size == 0 && !f.fetched).findFirst();
				if (dataset.isPresent()) {
					log.info("One dataset to fetch: {}", dataset.get().ref);
					self().tell(new FetchDataset(dataset.get().ref),self());
				} else {
					log.info("No dataset to fetch, schedule a refresh in 12h!");
					getTimers().startSingleTimer("refresh", new Tick(), Duration.ofHours(12));
				}
			}
		} else {
			if (state.getParsedYears().isEmpty()) {
				currentYear = Constants.START_YEAR;
			} else {
				Optional<String> maxParsedYear = state.getParsedYears().stream().max(Comparator.comparing(Integer::parseInt));
				if (maxParsedYear.isPresent() && !maxParsedYear.get().equalsIgnoreCase(currentYear)) {
					currentYear = String.valueOf(Integer.parseInt(maxParsedYear.get()) + 1);
				}
			}
			log.info("Need to get datasets for {}", currentYear);
			self().tell(new GetDatasets(currentYear), self());
		}
	}
	
	private void tick(Tick tick) {
		log.info("Received tick, state has changed!", tick);
		pipe(ref.ask(SyncCommand.get()), executor).to(self());
	}
	
	private void getDatasets(GetDatasets get) {
		String url = Urls.getYear(get.year);
		log.info("Received GetDatasets!");
		CompletableFuture<HttpResponse> getResponse = http.singleRequest(HttpRequest.create(url)).toCompletableFuture();
		CompletableFuture<HttpResponse> getDecodedResponse = getResponse.thenApply(decodeResponse);
		CompletableFuture<String> getBody = getDecodedResponse.thenCompose(
				decoded -> Unmarshaller.entityToString().unmarshal(decoded.entity(), materializer)
		);
		pipe(getBody.thenApply(
				body -> {
					Matcher matcher = Constants.linkPattern.matcher(body);
					List<String> datasets = new ArrayList<>();
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
					return new Datasets(get.year, datasets);
				}
		), executor).to(self());
	}
	
	private void saveDatasets(Datasets datasets) {
		log.info("Received Datasets {}, {}",datasets.year, datasets.datasets.size());
		CompletableFuture<Done> addDatasets = ref.ask(SyncCommand.addDatasets(datasets.datasets, datasets.year)).toCompletableFuture();
		pipe(addDatasets.thenCompose( done -> ref.ask(SyncCommand.addYear(datasets.year))), executor).to(self());
	}
	
	private void fetchDataset(FetchDataset fetch) {
		log.info("Received FetchDataset!");
		String url = Urls.getDataset(fetch.ref);
		Path uploadPath = Constants.getDatasetPath(fetch.ref);
		
		try {
			FileUtils.copyURLToFile(new URL(url), uploadPath.toFile());
			log.info("Dataset uploaded, size {}",uploadPath.toFile().length());
			if (environment.isDev()) {
				log.info("Development mode, do not pipe!");
				ref.ask(SyncCommand.fetchDataset(fetch.ref, uploadPath.toFile().length()));
			} else {
				pipe(ref.ask(SyncCommand.fetchDataset(fetch.ref, uploadPath.toFile().length())), executor).to(self());
			}
		} catch (Exception e) {
			log.error("Error while downloading {}: {}", fetch.ref, e);
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
