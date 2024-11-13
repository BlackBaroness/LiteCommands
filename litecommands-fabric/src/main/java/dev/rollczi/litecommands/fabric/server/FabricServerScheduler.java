package dev.rollczi.litecommands.fabric.server;

import dev.rollczi.litecommands.fabric.FabricScheduler;
import dev.rollczi.litecommands.scheduler.SchedulerExecutorPoolImpl;
import dev.rollczi.litecommands.scheduler.SchedulerPoll;
import dev.rollczi.litecommands.shared.ThrowingSupplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class FabricServerScheduler extends FabricScheduler {

    private final ScheduledExecutorService asyncExecutor;

    public FabricServerScheduler() {
        this(-1);
    }

    public FabricServerScheduler(int pool) {
        super(new ServerGetter());
        AtomicInteger asyncCount = new AtomicInteger();
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(String.format("scheduler-litecommands-fabric-async-%d", asyncCount.getAndIncrement()));

            return thread;
        };

        this.asyncExecutor = Executors.newScheduledThreadPool(
            Math.max(pool, Runtime.getRuntime().availableProcessors()) / 2,
            factory
        );
    }

    @Override
    public <T> void submit(SchedulerPoll type, Duration delay, ThrowingSupplier<T, @NotNull Throwable> supplier, CompletableFuture<T> future) {
        if (type.equals(SchedulerPoll.ASYNCHRONOUS)) {
            asyncExecutor.schedule(() -> {
                tryRun(supplier, future);
            }, delay.toMillis(), TimeUnit.MILLISECONDS);
        } else if (delay.isZero()) {
            ServerGetter.currentServer.execute(() -> tryRun(supplier, future));
        } else {
            ServerGetter.currentServer.execute(new LaterTask<>(delay, supplier, future));
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        SchedulerExecutorPoolImpl.close(asyncExecutor);
    }

    private static class LaterTask<T> implements Runnable {
        private final ThrowingSupplier<T, @NotNull Throwable> supplier;
        private final CompletableFuture<T> future;

        private int tick;
        private final int end;

        private LaterTask(Duration delay, ThrowingSupplier<T, @NotNull Throwable> supplier, CompletableFuture<T> future) {
            this.supplier = supplier;
            this.future = future;
            end = Math.toIntExact(delay.toMillis() / 50);
        }

        @Override
        public void run() {
            ++tick;
            if (!hasNext()) {
                tryRun(supplier, future);
                return;
            }
            MinecraftServer server = ServerGetter.currentServer;
            server.send(server.createTask(this));
        }

        boolean hasNext() {
            return end >= tick;
        }
    }

    private static class ServerGetter implements Supplier<Boolean> {
        private static MinecraftServer currentServer;

        static {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> ServerGetter.currentServer = server);
        }

        @Override
        public Boolean get() {
            if (currentServer != null) {
                return currentServer.isOnThread();
            }
            return false;
        }
    }
}
