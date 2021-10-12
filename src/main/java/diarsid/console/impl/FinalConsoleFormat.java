package diarsid.console.impl;

import java.util.Map;

import diarsid.console.api.format.ConsoleFormat;
import diarsid.console.api.format.ConsoleFormatElement;

public class FinalConsoleFormat implements InternalConsoleFormat {

    private final String name;
    private final String welcome;
    private final String consoleNameAndWelcomeSign;
    private final String spanBetweenLineStartAndName;
    private final String spanBetweenLineStartAndOutput;
    private final String spanBetweenNameAndSign;
    private final String spanBetweenSignAndInput;
    private final String exitQuestion;

    public FinalConsoleFormat(ConsoleFormat consoleFormat) {
        this.name = consoleFormat.name();
        this.welcome = consoleFormat.welcome();
        this.spanBetweenLineStartAndName = consoleFormat.spanBetweenLineStartAndName();
        this.spanBetweenNameAndSign = consoleFormat.spanBetweenNameAndSign();
        this.spanBetweenSignAndInput = consoleFormat.spanBetweenSignAndInput();
        this.spanBetweenLineStartAndOutput = consoleFormat.spanBetweenLineStartAndOutput();
        this.exitQuestion = consoleFormat.exitQuestion();

        this.consoleNameAndWelcomeSign = composeConsoleNameAndWelcomeSign();
    }

    public FinalConsoleFormat(Map<ConsoleFormatElement, String> elements) {
        this.name = elements.get(ConsoleFormatElement.NAME);
        this.welcome = elements.get(ConsoleFormatElement.WELCOME);
        this.spanBetweenLineStartAndName = elements.get(ConsoleFormatElement.SPAN_BETWEEN_LINE_START_AND_NAME);
        this.spanBetweenNameAndSign = elements.get(ConsoleFormatElement.SPAN_BETWEEN_NAME_AND_SIGN);
        this.spanBetweenSignAndInput = elements.get(ConsoleFormatElement.SPAN_BETWEEN_SIGN_AND_INPUT);
        this.spanBetweenLineStartAndOutput = elements.get(ConsoleFormatElement.SPAN_BETWEEN_LINE_START_AND_OUTPUT);
        this.exitQuestion = elements.get(ConsoleFormatElement.EXIT_QUESTION);

        this.consoleNameAndWelcomeSign = composeConsoleNameAndWelcomeSign();
    }

    private String composeConsoleNameAndWelcomeSign() {
        return
                this.spanBetweenLineStartAndName +
                this.name +
                this.spanBetweenNameAndSign +
                this.welcome +
                this.spanBetweenSignAndInput;
    }

    @Override
    public String ready() {
        return this.consoleNameAndWelcomeSign;
    }

    @Override
    public String welcome() {
        return this.welcome;
    }

    @Override
    public String exitQuestion() {
        return this.exitQuestion;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String spanBetweenLineStartAndName() {
        return this.spanBetweenLineStartAndName;
    }

    @Override
    public String spanBetweenLineStartAndOutput() {
        return this.spanBetweenLineStartAndOutput;
    }

    @Override
    public String spanBetweenNameAndSign() {
        return this.spanBetweenNameAndSign;
    }

    @Override
    public String spanBetweenSignAndInput() {
        return this.spanBetweenSignAndInput;
    }
}
