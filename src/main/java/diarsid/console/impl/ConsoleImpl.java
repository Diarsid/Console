package diarsid.console.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.console.api.Console;
import diarsid.console.api.Life;
import diarsid.console.api.io.Command;
import diarsid.console.api.io.ConsolePlatform;
import diarsid.console.api.io.Output;
import diarsid.console.impl.building.ConsoleBuilding;
import diarsid.console.impl.io.ExceptionHandler;
import diarsid.console.impl.io.OutputImpl;
import diarsid.support.objects.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MINUTES;

import static diarsid.support.objects.Either.Side.LEFT;

public class ConsoleImpl implements Console, Life {

    private static final Logger log = LoggerFactory.getLogger(ConsoleImpl.class);

    private final Object consolePlatformLock;
    private final ExitBehavior exitBehavior;
    private final OperationsChain operationsChain;
    private final OutputImpl output;
    private final ExceptionHandler exceptionHandler;
    private final CommandParser parser;

    private final ScheduledThreadPoolExecutor async;

    private boolean isWorking;
    private ConsolePlatforms consolePlatforms;
    private FinalConsoleFormat format;

    private Future asyncProcessingLoop;
//    private Future asyncInputListening;
//    private Future asyncPlatformListening;
    private Future asyncOutputListening;

    public ConsoleImpl(ConsoleBuilding builder) {
        builder.prepare();
        this.consolePlatformLock = new Object();
        this.async = new ScheduledThreadPoolExecutor(10);
        this.isWorking = false;
        this.consolePlatforms = builder.getConsolePlatforms();
        this.format = builder.getFormat();
        this.exceptionHandler = builder.getExceptionListeners();
        this.parser = builder.getParser();

        this.output = new OutputImpl(
                this.async,
                this.exceptionHandler,
                builder.getOutputListeners(),
                builder.getOutputSourcesQueues(),
                builder.getOutputSourcesSuppliers());

        this.exitBehavior = new ExitBehavior(
                builder.getExitPredicates(),
                builder.getExitConfirmationPredicates());

        this.operationsChain = new OperationsChain(builder.getOperations());
    }

    @Override
    public Life life() {
        return this;
    }

    @Override
    public Output output() {
        return this.output;
    }

    @Override
    public void start() {
        if ( this.isWorking) {
            return;
        }

        this.isWorking = true;

        this.asyncProcessingLoop = this.async.submit(() -> {
            String input;
            Command command;
            this.consolePlatforms.print(this.format.ready());
            while ( this.isWorking ) {
                try {
                    Activity activity = this.consolePlatforms.activities.take();
                    input = activity.input;
                    ConsolePlatform consolePlatform = activity.platform;
                    consolePlatform.beforeLocked();
                    synchronized ( this.consolePlatformLock ) {
                        consolePlatform.afterLocked();

                        if ( this.exitBehavior.isExitPattern(input) ) {
                            if ( this.exitBehavior.isExitConfirmationEnabled() ) {
                                consolePlatform.print(this.format.exitQuestion());
                                Optional<String> exitAnswer = consolePlatform.readLine();
                                if ( this.exitBehavior.isExitConfirmationPattern(exitAnswer.orElse("")) ) {
                                    Thread t = new Thread(this::stop);
                                    t.setName("-stopper");
                                    t.start();
                                    return;
                                }
                            }
                        }

                        Either<Command, List<String>> parsing = this.parser.parse(input);
                        if ( parsing.side.equalTo(LEFT) ) {
                            command = parsing.left;
                            List<String> answer = this.operationsChain.process(consolePlatform, command);
                            this.output.acceptProcessingAnswer(answer);
                            for (String line : answer) {
                                consolePlatform.println(this.format.spanBetweenLineStartAndOutput() + line);
                            }
                        }
                        else {
                            consolePlatform.println(this.format.spanBetweenLineStartAndOutput() + "Command is not valid: ");
                            for ( String line : parsing.right ) {
                                consolePlatform.println(this.format.spanBetweenLineStartAndOutput() + line);
                            }
                        }

                        consolePlatform.print(this.format.ready());
                        consolePlatform.beforeUnlocked();
                    }
                    consolePlatform.afterUnlocked();
                    synchronized ( consolePlatform ) {
                        consolePlatform.notify();
                    }
                }
                catch (Exception e) {
                    this.exceptionHandler.accept(e);
                }
            }
        });

        this.asyncOutputListening = this.async.submit(() -> {
            String output;
            while ( this.isWorking ) {
                try {
                    output = this.output.blockingTake();
                    this.consolePlatforms.beforeLocked();
                    synchronized ( this.consolePlatformLock ) {
                        this.consolePlatforms.afterLocked();
                        this.consolePlatforms.ln();
                        this.consolePlatforms.println(output);
                        this.consolePlatforms.print(this.format.ready());
                        this.consolePlatforms.beforeUnlocked();
                    }
                    this.consolePlatforms.afterUnlocked();
                } catch (InterruptedException e) {
                    this.exceptionHandler.accept(e);
                }
            }
        });

//        this.asyncInputListening = this.async.submit(() -> {
//            String input;
//            while ( this.isWorking ) {
//                try {
//                    input = this.input.();
//                    this.inputQueue.put(input);
//                }
//                catch (InterruptedException | IOException e) {
//                    this.exceptionListeners.accept(e);
//                }
//            }
//        });


//        this.asyncPlatformListening = this.async.submit(() -> {
//            String input;
//            while ( this.isWorking ) {
//                try {
//                    input = this.consolePlatform.readLine();
//                    this.inputQueue.put(input);
//                }
//                catch (InterruptedException | IOException e) {
//                    this.exceptionListeners.accept(e);
//                }
//            }
//        });
    }

    @Override
    public void stop() {
        if ( ! this.isWorking) {
            return;
        }

        this.isWorking = false;
//        this.asyncInputListening.cancel(true);
        this.output.stop();
        this.consolePlatforms.stop();
//        this.asyncOutputListening.cancel(true);
        this.async.shutdownNow();
        try { this.async.awaitTermination(10, MINUTES); } catch (Exception e) {}
    }

//    @Override
//    public Optional<String> readLine() {
//        try {
////            String line = consolePlatform.readLine();
////            return Optional.of(line);
//            return Optional.of("line");
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return Optional.empty();
//        }
//    }
//
//    @Override
//    public boolean println(String line) {
//        try {
////            this.consolePlatform.println(this.format.spanBetweenLineStartAndOutput() + line);
//            return true;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    @Override
//    public boolean print(String line) {
//        try {
////            this.consolePlatform.print(line);
//            return true;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    @Override
//    public boolean ln() {
//        try {
////            this.consolePlatform.ln();
////            this.consolePlatform.print(this.format.spanBetweenLineStartAndOutput());
//            return true;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
