package diarsid.console.impl.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import diarsid.console.api.io.Output;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;

class OutputChannelsImpl implements Output.Channels {

    private static interface UnsafeStringSupplier {

        String take() throws Exception;
    }

    private static class OutputChannel {

        private final UUID uuid;
        private final UnsafeStringSupplier pollableSource;
        private final BlockingQueue<String> sourcesAggregation;
        private final Future running;
        private boolean isWorking;

        OutputChannel(
                UnsafeStringSupplier pollableSource,
                BlockingQueue<String> sourcesAggregation,
                ExceptionHandler exceptionHandler,
                ExecutorService async) {
            this.uuid = randomUUID();
            this.pollableSource = pollableSource;
            this.sourcesAggregation = sourcesAggregation;

            this.isWorking = true;
            this.running = async.submit(() -> {
                while ( this.isWorking ) {
                    try {
                        String newOutput = this.pollableSource.take();
                        if ( this.isWorking ) {
                            try {
                                this.sourcesAggregation.put(newOutput);
                            } catch (Exception e) {
                                exceptionHandler.accept(e);
                            }
                        }
                    } catch (Exception e) {
                        exceptionHandler.accept(e);
                    }
                }
            });
        }

        UUID uuid() {
            return this.uuid;
        }

        void destroy() {
            this.isWorking = false;
            this.running.cancel(true);
        }
    }

    private final BlockingQueue<String> outputSourcesAggregation;
    private final Map<UUID, OutputChannel> outputSources;
    private final ExecutorService async;
    private final ExceptionHandler exceptionHandler;
    private final Future running;
    private boolean isWorking;

    public OutputChannelsImpl(
            Consumer<String> outputConsumer,
            List<BlockingQueue<String>> givenOutputSources,
            List<Supplier<String>> givenOutputSourcesSuppliers,
            ExceptionHandler exceptionHandler,
            ExecutorService async) {
        this.async = async;
        this.exceptionHandler = exceptionHandler;
        this.outputSourcesAggregation = new ArrayBlockingQueue<>(100);;
        this.outputSources = new HashMap<>();

        givenOutputSources.forEach(this::add);

        this.isWorking = true;
        this.running = this.async.submit(() -> {
            while ( this.isWorking ) {
                try {
                    String output = this.outputSourcesAggregation.take();
                    if ( this.isWorking ) {
                        try {
                            outputConsumer.accept(output);
                        } catch (Exception e) {
                            if ( this.isWorking ) {
                                this.exceptionHandler.accept(e);
                            }
                        }
                    }
                } catch (Exception e) {
                    this.exceptionHandler.accept(e);
                }
            }
        });
    }

    @Override
    public UUID add(BlockingQueue<String> sourceQueue) {
        OutputChannel outputChannel = new OutputChannel(
                sourceQueue::take,
                this.outputSourcesAggregation,
                this.exceptionHandler,
                this.async);
        UUID outputSourceUuid = outputChannel.uuid();
        this.outputSources.put(outputSourceUuid, outputChannel);
        return outputSourceUuid;
    }

    @Override
    public UUID add(Supplier<String> sourceSupplier) {
        OutputChannel outputChannel = new OutputChannel(
                sourceSupplier::get,
                this.outputSourcesAggregation,
                this.exceptionHandler,
                this.async);
        UUID outputSourceUuid = outputChannel.uuid();
        this.outputSources.put(outputSourceUuid, outputChannel);
        return outputSourceUuid;
    }

    @Override
    public boolean removeBy(UUID uuid) {
        OutputChannel removedOutputChannel = this.outputSources.remove(uuid);

        boolean existed = nonNull(removedOutputChannel);

        if ( existed ) {
            removedOutputChannel.destroy();
        }

        return existed;
    }

    void stop() {
        this.isWorking = false;
        this.running.cancel(true);
        this.outputSources.values().forEach(OutputChannel::destroy);
    }
}
