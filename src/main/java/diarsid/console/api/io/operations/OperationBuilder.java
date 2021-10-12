package diarsid.console.api.io.operations;

import java.util.function.Predicate;

import diarsid.console.api.io.Command;

public interface OperationBuilder {

    OperationBuilder named(String name);

    OperationBuilder doing(OperationLogic processing);

    OperationBuilder matching(Predicate<Command> match);

}
