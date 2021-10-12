package diarsid.console.impl;

import java.util.function.Predicate;

import diarsid.console.api.io.Command;
import diarsid.console.api.io.operations.Operation;
import diarsid.console.api.io.operations.OperationBuilder;
import diarsid.console.api.io.operations.OperationLogic;
import diarsid.console.impl.io.ExceptionHandler;

import static java.util.Objects.requireNonNull;

public class OperationBuilderImpl implements OperationBuilder {

    private String name;
    private OperationLogic processing;
    private Predicate<Command> inputMatching;

    public OperationBuilderImpl() {
    }

    @Override
    public OperationBuilder named(String name) {
        this.name = name;
        return this;
    }

    @Override
    public OperationBuilder doing(OperationLogic processing) {
        this.processing = processing;
        return this;
    }

    @Override
    public OperationBuilder matching(Predicate<Command> match) {
        this.inputMatching = match;
        return this;
    }

    public Operation build(ExceptionHandler exceptionHandler) {
        checkState();
        return new OperationImpl(this.name, this.processing, this.inputMatching, exceptionHandler);
    }

    private void checkState() {
        requireNonNull(this.name);
        requireNonNull(this.processing);
        requireNonNull(this.inputMatching);
    }
}
