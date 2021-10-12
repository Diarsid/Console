package diarsid.console.impl;

import java.util.List;
import java.util.function.Predicate;

class ExitBehavior {

    private final boolean exitConfirmationEnabled;
    private final List<Predicate<String>> exitPredicates;
    private final List<Predicate<String>> exitConfirmationPredicates;

    ExitBehavior(
            List<Predicate<String>> exitPredicates,
            List<Predicate<String>> exitConfirmationPredicates) {
        this.exitPredicates = exitPredicates;
        this.exitConfirmationEnabled = ! exitConfirmationPredicates.isEmpty();
        this.exitConfirmationPredicates = exitConfirmationPredicates;
    }

    void add(String exitPattern) {
        this.exitPredicates.add((input) -> input.equalsIgnoreCase(exitPattern));
    }

    boolean isExitPattern(String input) {
        return this.exitPredicates.stream().anyMatch(exitPredicate -> exitPredicate.test(input));
    }

    boolean isExitConfirmationEnabled() {
        return this.exitConfirmationEnabled;
    }

    boolean isExitConfirmationPattern(String input) {
        return
                this.exitConfirmationEnabled &&
                this.exitConfirmationPredicates
                        .stream()
                        .anyMatch(exitConfirmationPredicate -> exitConfirmationPredicate.test(input));
    }
}
