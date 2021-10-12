package diarsid.console.impl.io;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import diarsid.console.api.io.Output;
import diarsid.console.api.io.Listeners;

import static java.util.Objects.requireNonNull;

public class OutputImpl implements Output {

    private final BlockingQueue<String> outputQueue;
    private final OutputChannelsImpl consoleOutputSources;
    private final ListenersImpl outputListeners;
    private final ExceptionHandler exceptionHandler;

    public OutputImpl(
            ExecutorService async,
            ExceptionHandler exceptionHandler,
            List<Consumer<String>> initialOutputListeners,
            List<BlockingQueue<String>> initialOutputSourcesQueues,
            List<Supplier<String>> initialOutputSourcesSuppliers) {
        this.exceptionHandler = exceptionHandler;
        this.outputQueue = new ArrayBlockingQueue<>(100);
        this.outputListeners = new ListenersImpl(exceptionHandler, initialOutputListeners);
        this.consoleOutputSources = new OutputChannelsImpl(
                this::dispatchOutput,
                initialOutputSourcesQueues,
                initialOutputSourcesSuppliers,
                this.exceptionHandler, async);
    }

    @Override
    public void print(String s) {
        requireNonNull(s);
        this.dispatchOutput(s);
    }

    @Override
    public void println(String s) {
        requireNonNull(s);
        if ( ! s.endsWith("\n") ) {
            s = s + "\n";
        }
        this.dispatchOutput(s);
    }

    @Override
    public void ln() {
        this.dispatchOutput("\n");
    }

    @Override
    public Listeners listeners() {
        return this.outputListeners;
    }

    @Override
    public Output.Channels channels() {
        return this.consoleOutputSources;
    }

    public String blockingTake() throws InterruptedException {
        return this.outputQueue.take();
    }

    public void stop() {
        this.consoleOutputSources.stop();
    }

    public void acceptProcessingAnswer(List<String> answer) {
        try {
            this.outputListeners.accept(answer);
        } catch (Exception e) {
            this.exceptionHandler.accept(e);
        }
    }

    private void dispatchOutput(String s) {
        try {
            this.outputQueue.put(s);
            this.outputListeners.accept(s);
        } catch (Exception e) {
            this.exceptionHandler.accept(e);
        }
    }
}
