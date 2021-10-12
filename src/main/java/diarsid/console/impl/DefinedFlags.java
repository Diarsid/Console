package diarsid.console.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import diarsid.console.api.io.Command;

import static java.util.Objects.isNull;

public class DefinedFlags {

    private final Map<String, Command.Flag> namesOfFlags;
    private final Map<String, Command.Flag> shortNamesOfFlags;

    public DefinedFlags(Command.Flag... flags) {
        this.namesOfFlags = new HashMap<>();
        this.shortNamesOfFlags = new HashMap<>();

        for ( Command.Flag flag : flags) {
            this.namesOfFlags.put(flag.name(), flag);
            this.shortNamesOfFlags.put(flag.shortName(), flag);
        }
    }

    public DefinedFlags(Set<Command.Flag> flags) {
        this.namesOfFlags = new HashMap<>();
        this.shortNamesOfFlags = new HashMap<>();

        for ( Command.Flag flag : flags) {
            this.namesOfFlags.put(flag.name(), flag);
            this.shortNamesOfFlags.put(flag.shortName(), flag);
        }
    }

    public Optional<Command.Flag> ofName(String name) {
        Command.Flag flag = namesOfFlags.get(name);
        if ( isNull(flag) ) {
            flag = shortNamesOfFlags.get(name);
        }
        return Optional.ofNullable(flag);
    }
}
