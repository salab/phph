package jp.ac.titech.c.phph;

import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Log4j2
public class TaskQueue<T> {
    private final ExecutorService pool;

    private final Queue<Future<Consumer<T>>> queue = new ArrayDeque<>();

    public TaskQueue(int nthreads) {
        if (nthreads == 0) {
            final int nprocs = Runtime.getRuntime().availableProcessors();
            nthreads = nprocs > 1 ? nprocs - 1 : 1;
        }
        if (nthreads == 1) {
            this.pool = MoreExecutors.newDirectExecutorService();
        } else {
            this.pool = Executors.newFixedThreadPool(nthreads);
        }
    }

    public TaskQueue() {
        this(0);
    }

    public void register(final Callable<Consumer<T>> fn) {
        final Future<Consumer<T>> future = pool.submit(fn);
        queue.add(future);
    }

    public void consumeAll(final T t) {
        try {
            while (!queue.isEmpty()) {
                queue.poll().get().accept(t);
            }
        } catch (final ExecutionException e) {
            log.error(e.getMessage(), e);
            log.error(e.getCause().getMessage(), e.getCause());
        } catch (final InterruptedException e) {
            log.error(e.getMessage(), e);
       } finally {
            pool.shutdown();
       }
    }
}
