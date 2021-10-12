package diarsid.console.api.format;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ConsoleFormatBuilding {

    private final Map<ConsoleFormatElement, String> elements;

    ConsoleFormatBuilding() {
        this.elements = new HashMap<>();
    }

    ConsoleFormatBuilding(ConsoleFormat source) {
        requireNonNull(source);
        this.elements = source.asElements();
    }

    public ConsoleFormatBuilding with(ConsoleFormatElement element, String value) {
        this.elements.put(element, value);
        return this;
    }

    public Map<ConsoleFormatElement, String> elements() {
        return this.elements;
    }
}
