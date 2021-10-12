package diarsid.console.api.format;

public enum ConsoleFormatElement {

    NAME("console"),
    WELCOME(">"),
    SPAN_BETWEEN_LINE_START_AND_NAME(" "),
    SPAN_BETWEEN_NAME_AND_SIGN(" "),
    SPAN_BETWEEN_SIGN_AND_INPUT(" "),
    SPAN_BETWEEN_LINE_START_AND_OUTPUT(" "),
    EXIT_QUESTION("exit? y/n : ");

    private final String defaultValue;

    ConsoleFormatElement(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String defaultValue() {
        return this.defaultValue;
    }
}
