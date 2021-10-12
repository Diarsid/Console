package diarsid.console.api.io;

import java.util.Optional;

public interface ConsoleInteraction {

    Optional<String> readLine();

    boolean println(String s);

    boolean print(String s);

    boolean ln();
}
