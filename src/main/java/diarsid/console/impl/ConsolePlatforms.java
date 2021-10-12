package diarsid.console.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import diarsid.console.api.io.ConsolePlatform;
import diarsid.console.impl.io.ExceptionHandler;
import diarsid.support.exceptions.UnsupportedLogicException;

public class ConsolePlatforms implements ConsolePlatform {

    private final ExceptionHandler exceptionHandler;
    private final AtomicBoolean isWorking;
    private final List<ConsolePlatform> platforms;
    private final ScheduledThreadPoolExecutor async;
    private final ScheduledThreadPoolExecutor asyncForAll;
    public final BlockingQueue<Activity> activities;

    public ConsolePlatforms(ExceptionHandler exceptionHandler, List<ConsolePlatform> platforms) {
        this.exceptionHandler = exceptionHandler;
        this.platforms = new ArrayList<>(platforms);

        int platformsQty = platforms.size();
        this.isWorking = new AtomicBoolean(true);
        this.async = new ScheduledThreadPoolExecutor(platformsQty);
        this.asyncForAll = new ScheduledThreadPoolExecutor(platformsQty);

        this.activities = new ArrayBlockingQueue<>(1);

        this.platforms.forEach(platform -> {
            this.async.submit(() -> {
                while ( this.isWorking.get() ) {
                    try {
                        Optional<String> input = platform.readLine();
                        if ( input.isPresent() ) {
                            String line = input.get();
                            Activity activity = new Activity(platform, line);
                            this.activities.put(activity);
                            synchronized ( platform ) {
                                platform.wait();
                            }
                        }
                    }
                    catch (InterruptedException e) {
                        if ( this.isWorking.get() ) {
                            exceptionHandler.accept(e);
                        }
                    }
                }
            });
        });
    }

    @Override
    public Optional<String> readLine() {
        throw new UnsupportedLogicException();
    }

    @Override
    public boolean println(String line) {
        return this.doInParallelForEachPlatform(platform -> platform.println(line));
    }

    public boolean print(String line) {
        return this.doInParallelForEachPlatform(platform -> platform.print(line));
    }

    @Override
    public boolean ln() {
        return this.doInParallelForEachPlatform(ConsolePlatform::ln);
    }

    @Override
    public void beforeLocked() {
        this.doInParallelForEachPlatform(ConsolePlatform::beforeLocked);
    }

    @Override
    public void afterLocked() {
        this.doInParallelForEachPlatform(ConsolePlatform::afterLocked);
    }

    @Override
    public void beforeUnlocked() {
        this.doInParallelForEachPlatform(ConsolePlatform::beforeUnlocked);
    }

    @Override
    public void afterUnlocked() {
        this.doInParallelForEachPlatform(ConsolePlatform::afterUnlocked);
    }

    private boolean doInParallelForEachPlatform(Consumer<ConsolePlatform> platformAction) {
        CountDownLatch sync = new CountDownLatch(this.platforms.size());
        this.platforms.forEach(platform -> {
            this.asyncForAll.submit(() -> {
                try {
                    platformAction.accept(platform);
                }
                catch (Exception e) {
                    this.exceptionHandler.accept(e);
                }
                finally {
                    sync.countDown();
                }
            });
        });

        try {
            sync.await();
            return true;
        }
        catch (InterruptedException e) {
            this.exceptionHandler.accept(e);
            return false;
        }
    }

    public void stop() {
        this.isWorking.set(false);
        this.async.shutdownNow();
        this.asyncForAll.shutdownNow();
    }
}
