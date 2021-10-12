package diarsid.console.impl;

import java.util.ArrayList;
import java.util.List;

import diarsid.console.api.io.Command;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static diarsid.console.api.io.Command.Flag.Type.OPEN_VALUE;
import static diarsid.console.api.io.Command.Flag.Type.RESTRICTED_VALUE;

public class FlagImpl implements Command.Flag {

    private final String name;
    private final String shortName;
    private final Type type;
    private final boolean repeatable;
    private final List<String> restrictingValues;

    public FlagImpl(Type type, String name, String shortName, boolean repeatable) {
        if ( type.equalTo(RESTRICTED_VALUE) ) {
            throw new IllegalArgumentException(format("Flag with type '%s' must have values!", type));
        }
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.repeatable = repeatable;
        this.restrictingValues = emptyList();
    }

    public FlagImpl(String name, String shortName, boolean repeatable, String... values) {
        this.name = name;
        this.shortName = shortName;
        this.repeatable = repeatable;

        if ( values.length > 0 ) {
            this.type = RESTRICTED_VALUE;
            this.restrictingValues = asList(values);
        }
        else {
            this.type = OPEN_VALUE;
            this.restrictingValues = emptyList();
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String shortName() {
        return shortName;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public List<String> restrictingValues() {
        return restrictingValues;
    }
}
