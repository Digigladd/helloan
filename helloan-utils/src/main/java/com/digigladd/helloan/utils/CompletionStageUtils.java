/*
 * Copyright (c) 2018.
 */

package com.digigladd.helloan.utils;

import akka.Done;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class CompletionStageUtils {
	public static CompletionStage<Done> doAll(CompletionStage<?>... stages) {
		CompletionStage<Done> result = CompletableFuture.completedFuture(Done.getInstance());
		for (CompletionStage<?> stage : stages) {
			result = result.thenCombine(stage, (d1, d2) -> Done.getInstance());
		}
		return result;
	}
	
	public static CompletionStage<Done> doAll(List<CompletionStage<?>> stages) {
		CompletionStage<Done> result = CompletableFuture.completedFuture(Done.getInstance());
		for (CompletionStage<?> stage : stages) {
			result = result.thenCombine(stage, (d1, d2) -> Done.getInstance());
		}
		return result;
	}
	
	public static CompletionStage<Done> orDone(Logger log, String from) {
		log.info("orDone from {}", from);
		return CompletableFuture.completedFuture(Done.getInstance());
	}
	
	public static Done askDone(Logger log, String from) {
		log.info("askDone from {}", from);
		return Done.getInstance();
	}
}
