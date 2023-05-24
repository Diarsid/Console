package diarsid.console.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import diarsid.console.api.io.Command;
import diarsid.console.api.io.ConsoleInteraction;
import diarsid.console.api.io.operations.Operation;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class OperationsChain {

    public static final String DEFAULT_OPERATION_NAME = "default-operation";

    private final List<Operation> operations;
    private boolean failOnMultipleOperationsMatching;

    OperationsChain(List<Operation> operations) {
        List<Operation> defaultOperations = operations
                .stream()
                .filter(operation -> operation.name().equals(DEFAULT_OPERATION_NAME))
                .collect(toList());

        if ( defaultOperations.size() > 1 ) {
            throw new IllegalArgumentException();
        }

        if ( defaultOperations.isEmpty() ) {
            this.operations = operations;
        }
        else {
            Operation defaultOperation = defaultOperations.get(0);
            this.operations = new ArrayList<>(operations);
            this.operations.remove(defaultOperation);
            this.operations.add(defaultOperation);
        }
    }

    List<String> process(ConsoleInteraction interaction, Command command) {
        List<Operation> acceptableOperations = this.operations
                .stream()
                .filter(unit -> unit.canProcess(command))
                .collect(toList());

        if ( acceptableOperations.isEmpty() ) {
            return emptyList();
        }

        if ( acceptableOperations.size() > 1 ) {
            if ( failOnMultipleOperationsMatching ) {

            }
        }

        return acceptableOperations.get(0).execute(interaction, command);
    }
}
