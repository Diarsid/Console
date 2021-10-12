package diarsid.console.api.format;

import java.util.HashMap;
import java.util.Map;

public interface ConsoleFormat {

    static ConsoleFormatBuilding building() {
        return new ConsoleFormatBuilding();
    }

    static ConsoleFormatBuilding buildingFrom(ConsoleFormat source) {
        return new ConsoleFormatBuilding(source);
    }

    String welcome();

    String exitQuestion();

    String name();

    String spanBetweenLineStartAndName();

    String spanBetweenLineStartAndOutput();

    String spanBetweenNameAndSign();

    String spanBetweenSignAndInput();

    default Map<ConsoleFormatElement, String> asElements() {
        HashMap<ConsoleFormatElement, String> elements = new HashMap<>();

        elements.put(ConsoleFormatElement.NAME, name());
        elements.put(ConsoleFormatElement.WELCOME, welcome());
        elements.put(ConsoleFormatElement.SPAN_BETWEEN_LINE_START_AND_NAME, spanBetweenLineStartAndName());
        elements.put(ConsoleFormatElement.SPAN_BETWEEN_LINE_START_AND_OUTPUT, spanBetweenLineStartAndOutput());
        elements.put(ConsoleFormatElement.SPAN_BETWEEN_NAME_AND_SIGN, spanBetweenNameAndSign());
        elements.put(ConsoleFormatElement.SPAN_BETWEEN_SIGN_AND_INPUT, spanBetweenSignAndInput());
        elements.put(ConsoleFormatElement.EXIT_QUESTION, exitQuestion());

        return elements;
    }

}
