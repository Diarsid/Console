package diarsid.console.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import diarsid.console.api.io.Command;
import diarsid.support.objects.Either;
import org.junit.jupiter.api.Test;

import static diarsid.console.api.io.Command.Flag.Type.NO_VALUE;
import static diarsid.console.api.io.Command.Flag.Type.OPEN_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(parsing.side).isEqualTo(Either.Side.LEFT);

        Command command = parsing.left;

        assertThat(command.has(noValue)).isTrue();
        assertThat(command.has(openValue)).isTrue();
        assertThat(command.has(rValue)).isTrue();
        assertThat(command.valueOf(openValue)).hasValue("OPEN, OPEN_2");
        assertThat(command.valueOf(rValue)).hasValue("TWO, ONE");
        assertThat(command.valuesOf(openValue)).containsExactly("OPEN", "OPEN_2");
        assertThat(command.valuesOf(rValue)).containsExactly("TWO", "ONE");
        assertThat(command.args()).containsExactly("arg0", "arg1", "arg2", "arg3");
        assertThat(command.raw()).isEqualTo("arg0 arg1 --novalue -nv arg2 -ov OPEN -ov OPEN_2 -rv TWO arg3 -rv ONE");
    }
}
