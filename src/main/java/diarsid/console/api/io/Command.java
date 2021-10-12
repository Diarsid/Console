package diarsid.console.api.io;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.console.impl.FlagImpl;
import diarsid.support.objects.CommonEnum;

import static diarsid.console.api.io.Command.Flag.Type.NO_VALUE;
import static diarsid.console.api.io.Command.Flag.Type.OPEN_VALUE;

public interface Command {

    interface Flag {

        enum Type implements CommonEnum<Type> {
            NO_VALUE,
            OPEN_VALUE,
            RESTRICTED_VALUE
        }

        String name();

        String shortName();

        Type type();

        boolean isRepeatable();

        default boolean isNotRepeatable() {
            return ! this.isRepeatable();
        }

        List<String> restrictingValues();

        static Flag noValue(String name, String shortName) {
            return new FlagImpl(NO_VALUE, name, shortName, false);
        }

        static Flag openValue(String name, String shortName, boolean repeatable) {
            return new FlagImpl(OPEN_VALUE, name, shortName, repeatable);
        }

        static Flag restrictingValues(String name, String shortName, boolean repeatable, String... values) {
            return new FlagImpl(name, shortName, repeatable, values);
        }

    }

    String raw();

    String argAt(int i);

    List<String> args();

    Set<Flag> flags();

    boolean has(Flag flag);

    default boolean hasNot(Flag flag) {
        return ! this.has(flag);
    }

    Optional<String> valueOf(Flag flag);

    List<String> valuesOf(Flag flag);
}
