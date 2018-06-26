package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by matthias on 13.01.16.
 */
public class TaskResultHelper<V> {

    private final ExecutorService executorService;
    private final ExecutorCompletionService<V> executorCompletionService;
    private final Semaphore futuresSubmitted = new Semaphore(0, true);
    private final List<Future<V>> futures = new ArrayList<>();

    public TaskResultHelper(ExecutorService executorService) {
        this.executorService = executorService;
        executorCompletionService = new ExecutorCompletionService<V>(executorService);
    }

    public static <T> T getResult(final Future<T> future, final String message) throws InterruptedException {
        try {
            return future.get();
        } catch (final ExecutionException e) {
            return handleExecutionException(message, e);
        }
    }

    public static <T> T handleExecutionException(final String message, final ExecutionException e)
            throws InterruptedException {
        if (e.getCause() instanceof InterruptedException || e.getCause() instanceof InterruptedIOException) {
            final InterruptedException interruptedException = new InterruptedException();
            interruptedException.initCause(e);
            throw interruptedException;
        }
        throw new IllegalStateException(message, e);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    public Future<V> submit(Callable<V> task) {
        Future<V> future = executorCompletionService.submit(task);
        futuresSubmitted.release();
        futures.add(future);
        return future;
    }

    public Future<V> submit(Runnable task, V result) {
        Future<V> future = executorCompletionService.submit(task, result);
        futuresSubmitted.release();
        futures.add(future);
        return future;
    }

    public Future<?> submit(Runnable task) {
        return this.submit(task, null);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return executorService.invokeAll(tasks, timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }

    public void execute(Runnable command) {
        this.submit(command);
    }

    public List<V> collectOrThrowExceptions() throws ExecutionException, InterruptedException {
        try {
            ArrayList<V> list = new ArrayList<>();
            while (futuresSubmitted.tryAcquire()) {
                final Future<V> future = tryTake();
                list.add(future.get());
            }
            return list;
        } catch (ExecutionException | CancellationException e) {
            try {
                while (futuresSubmitted.tryAcquire()) {
                    Future<V> future = tryTake();
                    try {
                        future.get();
                    } catch (ExecutionException | CancellationException e1) {
                        e.addSuppressed(e1);
                    }
                }
            } catch (InterruptedException e1) {
                e1.addSuppressed(e);
                throw cancelAndAddAsSuppressed(e1);
            }
            throw e;
        } catch (InterruptedException e) {
            throw cancelAndAddAsSuppressed(e);
        }
    }

    private Future<V> tryTake() throws InterruptedException {
        try {
            return executorCompletionService.take();
        } catch (InterruptedException e) {
            futuresSubmitted.release();
            throw e;
        }
    }

    private <T extends Throwable> T cancelAndAddAsSuppressed(T e) {
        for (Future<V> future : futures) {
            future.cancel(true);
        }
        while (futuresSubmitted.tryAcquire()) {
            try {
                Future<V> future = tryTake();
                future.get();
            } catch (ExecutionException | CancellationException | InterruptedException e1) {
                e.addSuppressed(e1);
            }
        }
        return e;
    }
}
