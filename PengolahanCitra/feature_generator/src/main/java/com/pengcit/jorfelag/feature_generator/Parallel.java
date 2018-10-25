package com.pengcit.jorfelag.feature_generator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * https://stackoverflow.com/a/5849282
 */
public class Parallel {
    private static final int iCPU = Runtime.getRuntime().availableProcessors();

    public static <T> void ForEach(Iterable<T> parameters,
                                   final LoopBody<T> loopBody) {
        ExecutorService executor = Executors.newFixedThreadPool(iCPU);
        List<Future<?>> futures = new LinkedList<Future<?>>();

        for (final T param : parameters) {
            Future<?> future = executor.submit(new Runnable() {
                public void run() {
                    loopBody.run(param);
                }
            });

            futures.add(future);
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }

        executor.shutdown();
    }

    public static void For(int start,
                           int stop,
                           final LoopBody<Integer> loopBody) {
        ExecutorService executor = Executors.newFixedThreadPool(iCPU);
        List<Future<?>> futures = new LinkedList<Future<?>>();

        for (int i = start; i < stop; i++) {
            final Integer k = i;
            Future<?> future = executor.submit(new Runnable() {
                public void run() {
                    loopBody.run(k);
                }
            });
            futures.add(future);
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }

        executor.shutdown();
    }
}
