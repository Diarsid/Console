package diarsid.console.api.io.operations;

import java.util.List;

import diarsid.console.api.io.Command;
import diarsid.console.api.io.ConsoleInteraction;

public interface OperationLogic {

    List<String> execute(ConsoleInteraction interaction, Command command);
}
