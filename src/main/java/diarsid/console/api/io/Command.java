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
            ALLOWED_VALUE,
            VALIDATED_VALUE;
        }

        String name();

        String shortName();

        Type type();

        boolean isRepeatable();

        default boolean isNotRepeatable() {
            return ! this.isRepeatable();
        }

        List<String> allowedValues();

        Validator validator();

        static Flag withNoValue(String name, String shortName) {
            return new FlagImpl(NO_VALUE, name, shortName, false);
        }

        static Flag withAnyValues(String name, String shortName, boolean repeatable) {
            return new FlagImpl(OPEN_VALUE, name, shortName, repeatable);
        }

        static Flag withAllowedValues(String name, String shortName, boolean repeatable, String... values) {
            return new FlagImpl(name, shortName, repeatable, values);
        }

        static Flag withValidatedValues(String name, String shortName, boolean repeatable, Validator validator) {
            return new FlagImpl(name, shortName, repeatable, validator);
        }

        interface Validator {

            void validate(String value) throws IllegalArgumentException;
        }

    }

    String raw();

    default String argAt(int i) {
        var args = this.args();

        boolean noValue =
                args.size() <= i ||
                args.isEmpty() ||
                i < 0;

        if ( noValue ) {
            return "";
        }
        else {
            return args.get(i);
        }
    }

    List<String> args();

    Set<Flag> flags();

    boolean has(Flag flag);

    default boolean hasNot(Flag flag) {
        return ! this.has(flag);
    }

    Optional<String> valueOf(Flag flag);

    List<String> valuesOf(Flag flag);

    default boolean firstArgIs(String s) {
        return this.argAt(0).equals(s);
    }

    default boolean hasArgs(int size) {
        return this.args().size() == size;
    }
}
