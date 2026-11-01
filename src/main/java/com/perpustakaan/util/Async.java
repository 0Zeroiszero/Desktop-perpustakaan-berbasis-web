package com.perpustakaan.util;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Menjalankan pekerjaan database/IO di luar JavaFX Application Thread.
 * Callback sukses/gagal selalu dipanggil di JavaFX Application Thread.
 */
public final class Async {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "pustaka-worker-" + COUNTER.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    };
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(THREAD_FACTORY);

    private Async() {
    }

    public static <T> void run(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };
        task.setOnSucceeded(event -> {
            try {
                onSuccess.accept(task.getValue());
            } catch (Throwable error) {
                onError.accept(error);
            }
        });
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            onError.accept(error == null ? new IllegalStateException("Terjadi kesalahan tidak diketahui.") : error);
        });
        EXECUTOR.execute(task);
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
