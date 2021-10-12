package diarsid.console.impl.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import diarsid.console.api.io.Listeners;

import static java.util.UUID.randomUUID;

class ListenersImpl implements Listeners {

    private static class Listener implements Consumer<String> {

        private final UUID uuid;
        private final Consumer<String> listener;

        Listener(Consumer<String> listener) {
            this.uuid = randomUUID();
            this.listener = listener;
        }

        UUID uuid() {
            return this.uuid;
        }

        @Override
        public void accept(String output) {
            this.listener.accept(output);
        }

        void accept(List<String> outputs) {
            outputs.forEach(this::accept);
        }
    }

    private final ExceptionHandler exceptionHandler;
    private final Map<UUID, Listener> outputListeners;

    ListenersImpl(
            ExceptionHandler exceptionHandler,
            List<Consumer<String>> initialListeners) {
        this.exceptionHandler = exceptionHandler;
        this.outputListeners = new HashMap<>();
        initialListeners.forEach(this::add);
    }

    @Override
    public UUID add(Consumer<String> listener) {
        Consumer<String> safeOutputListener = (output) -> {
            try {
                listener.accept(output);
            } catch (Exception e) {
                this.exceptionHandler.accept(e);
            }
        };

        Listener outputListener = new Listener(safeOutputListener);
        this.outputListeners.put(outputListener.uuid(), outputListener);
        return outputListener.uuid();
    }

    @Override
    public Optional<Consumer<String>> remove(UUID uuid) {
        Listener listener = this.outputListeners.remove(uuid);
        return Optional.ofNullable(listener);
    }

    void accept(String output) {
        this.outputListeners.values().forEach(listener -> listener.accept(output));
    }

    void accept(List<String> outputs) {
        this.outputListeners.values().forEach(listener -> listener.accept(outputs));
    }
}
