package diarsid.console.api.io;

public interface ConsolePlatform extends ConsoleInteraction {

    default void beforeLocked() {
        // do nothing, intended for @Override
    }

    default void afterLocked() {
        // do nothing, intended for @Override
    }

    default void beforeUnlocked() {
        // do nothing, intended for @Override
    }

    default void afterUnlocked() {
        // do nothing, intended for @Override
    }

}
