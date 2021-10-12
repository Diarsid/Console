package diarsid.console.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import diarsid.console.api.io.Command;
import diarsid.console.api.io.ConsoleInteraction;
import diarsid.console.api.io.operations.Operation;
import diarsid.console.api.io.operations.OperationLogic;
import diarsid.console.impl.io.ExceptionHandler;

import static java.util.Arrays.stream;

public class OperationImpl implements Operation {

    private final String name;
    private final OperationLogic operation;
    private final Predicate<Command> inputMatching;
    private final ExceptionHandler exceptionHandler;

    public OperationImpl(
            String name,
            OperationLogic processing,
            Predicate<Command> inputMatching,
            ExceptionHandler exceptionHandler) {
        this.name = name;
        this.operation = processing;
        this.inputMatching = inputMatching;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean canProcess(Command command) {
        try {
            return this.inputMatching.test(command);
        } catch (Exception e) {
            this.exceptionHandler.accept(e);
            return false;
        }
    }

    public static List<String> toLines(Exception e) {
        List<String> lines = new ArrayList<>();
        lines.add(e.getMessage());
        stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .forEach(lines::add);
        return lines;
    }

    @Override
    public List<String> execute(ConsoleInteraction interaction, Command command) {
        try {
            return this.operation.execute(interaction, command);
        } catch (Exception e) {
            this.exceptionHandler.accept(e);
            return toLines(e);
        }
    }
}
