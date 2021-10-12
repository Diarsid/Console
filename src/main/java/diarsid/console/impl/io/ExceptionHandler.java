package diarsid.console.impl.io;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExceptionHandler {

    static final Consumer<Exception> defaultExceptionListener = Exception::printStackTrace;
    private final List<Consumer<Exception>> listeners;

    public ExceptionHandler() {
        this.listeners = new ArrayList<>();
    }

    public ExceptionHandler(Consumer<Exception> listener) {
        this.listeners = new ArrayList<>();
        this.listeners.add(listener);
    }

    public ExceptionHandler(List<Consumer<Exception>> listeners) {
        this.listeners = listeners;
    }

    public void accept(Exception e) {
        if (this.listeners.isEmpty()) {
            return;
        }

        this.listeners.forEach(listener -> listener.accept(e));
    }

    public void add(Consumer<Exception> listener) {
        this.listeners.add(listener);
    }

    public void setDefaultIfEmpty() {
        if ( this.listeners.isEmpty() ) {
            this.listeners.add(defaultExceptionListener);
        }
    }
}
