package diarsid.console.impl;

import java.util.List;

import diarsid.console.api.io.Command;
import diarsid.console.api.io.ConsoleInteraction;
import diarsid.console.api.io.operations.Operation;
import diarsid.console.impl.io.ExceptionHandler;

import static diarsid.console.impl.OperationImpl.toLines;

public class SafeOperationWrapper implements Operation {

    private final Operation operation;
    private final ExceptionHandler exceptionHandler;

    public SafeOperationWrapper(Operation operation, ExceptionHandler exceptionHandler) {
        this.operation = operation;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public String name() {
        return operation.name();
    }

    @Override
    public boolean canProcess(Command command) {
        try {
            return operation.canProcess(command);
        }
        catch (Exception e) {
            exceptionHandler.accept(e);
            return false;
        }
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
