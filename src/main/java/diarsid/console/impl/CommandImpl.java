package diarsid.console.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.console.api.io.Command;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import static diarsid.console.api.io.Command.Flag.Type.NO_VALUE;

public class CommandImpl implements Command {

    private final String raw;
    private final List<String> args;
    private final Map<Flag, List<String>> flagValues;

    public CommandImpl(String raw, List<String> args, Map<Flag, List<String>> flagValues) {
        this.raw = raw;
        this.args = args;
        this.flagValues = flagValues;
    }

    @Override
    public String raw() {
        return raw;
    }

    @Override
    public List<String> args() {
        return args;
    }

    @Override
    public Set<Flag> flags() {
        return flagValues.keySet();
    }

    @Override
    public boolean has(Flag flag) {
        return flagValues.containsKey(flag);
    }

    @Override
    public Optional<String> valueOf(Flag flag) {
        if ( flag.type().equalTo(NO_VALUE) ) {
            return Optional.empty();
        }

        List<String> values = valuesOf(flag);
        if ( values.isEmpty() ) {
            return Optional.empty();
        }
        else if ( values.size() == 1 ) {
            return Optional.of(values.get(0));
        }
        else {
            return Optional.of(String.join(", ", values));
        }

    }

    @Override
    public List<String> valuesOf(Flag flag) {
        if ( flag.type().equalTo(NO_VALUE) ) {
            return emptyList();
        }

        List<String> values = flagValues.get(flag);

        if ( isNull(values) ) {
            values = emptyList();
        }

        return values;
    }
}
