package diarsid.console.impl.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import diarsid.console.api.Console;
import diarsid.console.api.format.ConsoleFormat;
import diarsid.console.api.format.ConsoleFormatBuilding;
import diarsid.console.api.format.ConsoleFormatElement;
import diarsid.console.api.io.Command;
import diarsid.console.api.io.ConsolePlatform;
import diarsid.console.api.io.operations.Operation;
import diarsid.console.api.io.operations.OperationBuilder;
import diarsid.console.api.io.operations.OperationLogic;
import diarsid.console.impl.CommandParser;
import diarsid.console.impl.ConsoleImpl;
import diarsid.console.impl.ConsolePlatforms;
import diarsid.console.impl.DefaultConsoleFormat;
import diarsid.console.impl.DefinedFlags;
import diarsid.console.impl.FinalConsoleFormat;
import diarsid.console.impl.OperationBuilderImpl;
import diarsid.console.impl.SafeOperationWrapper;
import diarsid.console.impl.io.ExceptionHandler;
import diarsid.console.impl.platforms.SystemConsolePlatform;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import static diarsid.console.impl.OperationsChain.DEFAULT_OPERATION_NAME;

public class ConsoleBuilding {

    private static final boolean DEFAULT_EXIT_CONFIRMATION_ENABLED = false;

    private final List<ConsolePlatform> consolePlatformsList;
    private final List<OperationBuilderImpl> operationBuilders;
    private final List<Operation> operations;
    private final List<Consumer<String>> inputListeners;
    private final List<Consumer<String>> outputListeners;
    private final List<Consumer<Exception>> exceptionListenersLists;
    private final List<BlockingQueue<String>> outputSourcesQueues;
    private final List<Supplier<String>> outputSourcesSuppliers;
    private final List<Predicate<String>> exitPredicates;
    private final List<Predicate<String>> exitConfirmationPredicates;
    private final Set<Command.Flag> flags;
    private final Map<ConsoleFormatElement, String> formatBuildingMap;

    private ConsolePlatforms consolePlatforms;
    private ExceptionHandler exceptionHandler;
    private List<Operation> allOperations;
    private CommandParser parser;

    private ConsoleFormat format;

    public ConsoleBuilding() {
        this.consolePlatformsList = new ArrayList<>();
        this.operationBuilders = new ArrayList<>();
        this.operations = new ArrayList<>();
        this.inputListeners = new ArrayList<>();
        this.outputListeners = new ArrayList<>();
        this.exceptionListenersLists = new ArrayList<>();
        this.outputSourcesQueues = new ArrayList<>();
        this.outputSourcesSuppliers = new ArrayList<>();
        this.exitPredicates = new ArrayList<>();
        this.exitConfirmationPredicates = new ArrayList<>();
        this.flags = new HashSet<>();
        this.formatBuildingMap = new HashMap<>();
    }

    public ConsoleBuilding withPlatform(ConsolePlatform consolePlatform) {
        this.consolePlatformsList.add(consolePlatform);
        return this;
    }

    public ConsoleBuilding withOperation(Consumer<OperationBuilder> builderLogic) {
        OperationBuilderImpl builder = new OperationBuilderImpl();
        builderLogic.accept(builder);
        this.operationBuilders.add(builder);
        return this;
    }

    public ConsoleBuilding withDefaultOperation(OperationLogic defaultOperationLogic) {
        OperationBuilderImpl builder = new OperationBuilderImpl();
        builder
                .named(DEFAULT_OPERATION_NAME)
                .doing(defaultOperationLogic)
                .matching(command -> true);
        this.operationBuilders.add(builder);
        return this;
    }

    public ConsoleBuilding withOperation(Operation operation) {
        this.operations.add(operation);
        return this;
    }

    public ConsoleBuilding withInputListener(Consumer<String> inputConsumer) {
        this.inputListeners.add(inputConsumer);
        return this;
    }

    public ConsoleBuilding withOutputListener(Consumer<String> inputConsumer) {
        this.outputListeners.add(inputConsumer);
        return this;
    }

    public ConsoleBuilding withPollableOutputSource(BlockingQueue<String> queue) {
        requireNonNull(queue);

        this.outputSourcesQueues.add(queue);
        return this;
    }

    public ConsoleBuilding withPollableOutputSource(Supplier<String> queue) {
        requireNonNull(queue);

        this.outputSourcesSuppliers.add(queue);
        return this;
    }

    public ConsoleBuilding stopWhenInputIsAny(String... exits) {
        this.exitPredicates.add((input) -> {
            String refinedInput = input.trim().toLowerCase();

            boolean inputMatchExit = stream(exits)
                    .map(exit -> exit.trim().toLowerCase())
                    .anyMatch(exit -> exit.equalsIgnoreCase(refinedInput));

            return inputMatchExit;
        });
        return this;
    }

    public ConsoleBuilding stopWhenInputMatches(Predicate<String> ifExit) {
        this.exitPredicates.add(ifExit);
        return this;
    }

    public ConsoleBuilding stopWhenInputIs(String exit) {
        this.exitPredicates.add((input) -> {
            return input.trim().toLowerCase().equals(exit.toLowerCase().trim());
        });
        return this;
    }

    public ConsoleBuilding enableExitConfirmation(String... confirmations) {
        stream(confirmations)
                .map(confirmation -> {
                    Predicate<String> confirmationPredicate = (String input) -> {
                        return input.trim().toLowerCase().equals(confirmation.toLowerCase().trim());
                    };
                    return confirmationPredicate;
                })
                .forEach(this.exitConfirmationPredicates::add);

        return this;
    }

    public ConsoleBuilding withFormat(ConsoleFormat format) {
        this.format = format;
        return this;
    }

    public ConsoleBuilding withFormat(ConsoleFormatBuilding formatBuilding) {
        this.formatBuildingMap.putAll(formatBuilding.elements());
        return this;
    }

    public ConsoleBuilding withExceptionListener(Consumer<Exception> listener) {
        this.exceptionListenersLists.add(listener);
        return this;
    }

    public ConsoleBuilding withFlag(Command.Flag flag) {
        this.flags.add(flag);
        return this;
    }

    public ConsoleBuilding withFlags(Command.Flag... flags) {
        this.flags.addAll(asList(flags));
        return this;
    }

    public Console done() {
        setupDefaultValues();
        return new ConsoleImpl(this);
    }

    private void setupDefaultValues() {
        if (isNull(this.format)) {
            ConsoleFormat defaultFormat = new DefaultConsoleFormat();
            for (Map.Entry<ConsoleFormatElement, String> entry : defaultFormat.asElements().entrySet()) {
                if ( ! this.formatBuildingMap.containsKey(entry.getKey()) ) {
                    this.formatBuildingMap.put(entry.getKey(), entry.getValue());
                }
            }

            this.format = new FinalConsoleFormat(this.formatBuildingMap);
        }
    }

    public void prepare() {
        this.exceptionHandler = new ExceptionHandler(this.exceptionListenersLists);
//        this.exceptionListeners.setDefaultIfEmpty();

        DefinedFlags definedFlags = new DefinedFlags(this.flags);
        this.parser = new CommandParser(definedFlags);

        if ( this.consolePlatformsList.isEmpty() ) {
            this.consolePlatformsList.add(new SystemConsolePlatform(this.exceptionHandler));
        }
        this.consolePlatforms = new ConsolePlatforms(this.exceptionHandler, this.consolePlatformsList);

        this.allOperations = new ArrayList<>();
        this.allOperations.addAll(this.operations
                .stream()
                .map(operation -> new SafeOperationWrapper(operation, this.exceptionHandler))
                .collect(Collectors.toList()));

        this.allOperations.addAll(this.operationBuilders
                .stream()
                .map(builder -> builder.build(this.exceptionHandler))
                .collect(Collectors.toList()));
    }

    public ConsolePlatforms getConsolePlatforms() {
        return this.consolePlatforms;
    }

    public List<Operation> getOperations() {
        return this.allOperations;
    }

    public List<Consumer<String>> getOutputListeners() {
        return this.outputListeners;
    }

    public List<Consumer<String>> getInputListeners() {
        return this.inputListeners;
    }

    public ExceptionHandler getExceptionListeners() {
        return this.exceptionHandler;
    }

    public List<BlockingQueue<String>> getOutputSourcesQueues() {
        return this.outputSourcesQueues;
    }

    public List<Predicate<String>> getExitPredicates() {
        return this.exitPredicates;
    }

    public FinalConsoleFormat getFormat() {
        return new FinalConsoleFormat(this.format);
    }

    public CommandParser getParser() {
        return this.parser;
    }

    public List<Predicate<String>> getExitConfirmationPredicates() {
        return this.exitConfirmationPredicates;
    }

    public List<Supplier<String>> getOutputSourcesSuppliers() {
        return this.outputSourcesSuppliers;
    }
}
