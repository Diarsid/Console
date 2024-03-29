package diarsid.console.impl;

import java.util.List;

import org.junit.jupiter.api.Test;

import diarsid.console.api.io.Command;
import diarsid.support.objects.references.Either;

import static org.assertj.core.api.Assertions.assertThat;

import static diarsid.console.api.io.Command.Flag.Type.NO_VALUE;
import static diarsid.console.api.io.Command.Flag.Type.OPEN_VALUE;

public class CommandParserTest {

    @Test
    public void test() {
        Command.Flag noValue = new FlagImpl(NO_VALUE, "novalue", "nv", true);
        Command.Flag openValue = new FlagImpl(OPEN_VALUE, "openvalue", "ov", true);
        Command.Flag rValue = new FlagImpl("restrictedvalue", "rv", true, "ONE", "TWO", "THREE");

        DefinedFlags flags = new DefinedFlags(noValue, openValue, rValue);
        CommandParser parser = new CommandParser(flags);

        Either<Command, List<String>> parsing = parser
                .parse("arg0 arg1 --novalue -nv arg2 -ov OPEN -ov OPEN_2 -rv TWO arg3 -rv ONE");
        assertThat(parsing.presence()).isEqualTo(Either.Presence.PRIMARY);

        Command command = parsing.orThrow();

        assertThat(command.has(noValue)).isTrue();
        assertThat(command.has(openValue)).isTrue();
        assertThat(command.has(rValue)).isTrue();
        assertThat(command.valueOf(openValue)).hasValue("OPEN, OPEN_2");
        assertThat(command.valueOf(rValue)).hasValue("TWO, ONE");
        assertThat(command.valuesOf(openValue)).containsExactly("OPEN", "OPEN_2");
        assertThat(command.valuesOf(rValue)).containsExactly("TWO", "ONE");
        assertThat(command.args()).containsExactly("arg0", "arg1", "arg2", "arg3");
        assertThat(command.raw()).isEqualTo("arg0 arg1 --novalue -nv arg2 -ov OPEN -ov OPEN_2 -rv TWO arg3 -rv ONE");

        String s = command.argAt(0);
        String s1 = command.argAt(-1);
        String s2 = command.argAt(20);
    }
}
