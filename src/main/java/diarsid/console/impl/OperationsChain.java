package diarsid.console.impl;

import java.util.List;
import java.util.stream.Collectors;

import diarsid.console.api.io.Command;
import diarsid.console.api.io.ConsoleInteraction;
import diarsid.console.api.io.operations.Operation;

import static java.util.Collections.emptyList;

class OperationsChain {

    private final List<Operation> operations;
    private boolean failOnMultipleOperationsMatching;

    OperationsChain(List<Operation> operations) {
        this.operations = operations;
    }

    List<String> process(ConsoleInteraction interaction, Command command) {
        List<Operation> acceptableOperations = this.operations
                .stream()
                .filter(unit -> unit.canProcess(command))
                .collect(Collectors.toList());

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
