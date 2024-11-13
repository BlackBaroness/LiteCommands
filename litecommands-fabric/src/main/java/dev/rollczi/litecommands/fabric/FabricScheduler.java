package dev.rollczi.litecommands.fabric;

import dev.rollczi.litecommands.scheduler.Scheduler;
import dev.rollczi.litecommands.scheduler.SchedulerPoll;
import dev.rollczi.litecommands.shared.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class FabricScheduler implements Scheduler {
    private final Supplier<Boolean> isMainThread;

    public FabricScheduler(Supplier<Boolean> isMainThread) {
        this.isMainThread = isMainThread;
    }

    @Override
    public <T> CompletableFuture<T> supplyLater(SchedulerPoll type, Duration delay, ThrowingSupplier<T, Throwable> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        SchedulerPoll resolve = type.resolve(SchedulerPoll.MAIN, SchedulerPoll.ASYNCHRONOUS);
        if (resolve.equals(SchedulerPoll.MAIN) && delay.isZero() && isMainThread.get()) {
            return tryRun(supplier, future);
        } else {
            submit(resolve, delay, supplier, future);
        }
        return future;
    }

    protected static <T> CompletableFuture<T> tryRun(ThrowingSupplier<T, @NotNull Throwable> supplier, CompletableFuture<T> future) {
        try {
            future.complete(supplier.get());
        } catch (Throwable e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public abstract <T> void submit(SchedulerPoll type, Duration delay, ThrowingSupplier<T, @NotNull Throwable> supplier, CompletableFuture<T> future);

    @Override
    public void shutdown() {

    }
}
