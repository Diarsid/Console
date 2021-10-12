package diarsid.console.api.io;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface Listeners {

    UUID add(Consumer<String> listener);

    Optional<Consumer<String>> remove(UUID uuid);
}
