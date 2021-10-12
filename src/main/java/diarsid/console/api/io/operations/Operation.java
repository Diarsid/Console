package diarsid.console.api.io.operations;

import diarsid.console.api.io.Command;

public interface Operation extends OperationLogic  {

    String name();

    boolean canProcess(Command command);

}
