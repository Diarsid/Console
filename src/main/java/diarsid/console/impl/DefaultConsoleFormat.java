package diarsid.console.impl;

import diarsid.console.api.format.ConsoleFormat;
import diarsid.console.api.format.ConsoleFormatElement;

public class DefaultConsoleFormat implements ConsoleFormat {

    @Override
    public String welcome() {
        return ConsoleFormatElement.WELCOME.defaultValue();
    }

    @Override
    public String exitQuestion() {
        return ConsoleFormatElement.EXIT_QUESTION.defaultValue();
    }

    @Override
    public String name() {
        return ConsoleFormatElement.NAME.defaultValue();
    }

    @Override
    public String spanBetweenLineStartAndName() {
        return ConsoleFormatElement.SPAN_BETWEEN_LINE_START_AND_NAME.defaultValue();
    }

    @Override
    public String spanBetweenLineStartAndOutput() {
        return ConsoleFormatElement.SPAN_BETWEEN_LINE_START_AND_OUTPUT.defaultValue();
    }

    @Override
    public String spanBetweenNameAndSign() {
        return ConsoleFormatElement.SPAN_BETWEEN_NAME_AND_SIGN.defaultValue();
    }

    @Override
    public String spanBetweenSignAndInput() {
        return ConsoleFormatElement.SPAN_BETWEEN_SIGN_AND_INPUT.defaultValue();
    }
}
