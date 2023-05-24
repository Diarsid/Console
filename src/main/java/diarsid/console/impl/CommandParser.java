package diarsid.console.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import diarsid.console.api.io.Command;
import diarsid.support.objects.references.Either;
import diarsid.support.strings.StringUtils;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import static diarsid.console.api.io.Command.Flag.Type.NO_VALUE;
import static diarsid.console.api.io.Command.Flag.Type.RESTRICTED_VALUE;

public class CommandParser {

    private final DefinedFlags definedFlags;

    public CommandParser(DefinedFlags definedFlags) {
        this.definedFlags = definedFlags;
    }

    public Either<Command, List<String>> parse(String line) {
        List<String> problems = new ArrayList<>();
        Map<Command.Flag, List<String>> flagValues = new HashMap<>();
        List<String> args = new ArrayList<>();

        String[] parts = StringUtils.splitBySpaces(line);
        String part;
        String flagName;
        Command.Flag flag;
        String flagValue;
        int separator = -1;
        int size = parts.length;
        int last = size - 1;
        boolean valueAsSubstring;
        for ( int i = 0; i < size; i++ ) {
            part = parts[i];
            if ( part.charAt(0) == '-' ) {
                if ( part.charAt(1) == '-' ) {
                    flagName = part.substring(2);
                    part = flagName;
                }
                else {
                    flagName = part.substring(1);
                    part = flagName;
                }

                valueAsSubstring = flagName.contains(":") || flagName.contains("=");
                if ( valueAsSubstring ) {
                    separator = flagName.indexOf(':');
                    if ( separator < 0 ) {
                        separator = flagName.indexOf('=');
                    }
                    flagName = flagName.substring(0, separator);
                }

                Optional<Command.Flag> oFlag = definedFlags.ofName(flagName);

                if ( oFlag.isPresent() ) {
                    flag = oFlag.get();
                }
                else {
                    problems.add(format("No flag '%s' is configured!", flagName));
                    continue;
                }

                if ( flag.type().equalTo(NO_VALUE) ) {
                    flagValues.put(flag, null);
                }
                else {
                    if ( valueAsSubstring ) {
                        flagValue = part.substring(separator + 1);
                        if ( flag.type().equalTo(RESTRICTED_VALUE) ) {
                            if ( ! flag.restrictingValues().contains(flagValue) ) {
                                problems.add(format("Flag '%s' accepts only values: %s",
                                        flag.name(),
                                        String.join(", ", flag.restrictingValues())));
                            }
                        }

                        List<String> values = flagValues.get(flag);
                        if ( isNull(values) ) {
                            values = new ArrayList<>();
                            flagValues.put(flag, values);
                        }
                        else if ( flag.isNotRepeatable() ) {
                            problems.add(format("Flag '%s' requires value!", flag.name()));
                        }
                        values.add(flagValue);
                    }
                    else if ( i < last ) {
                        flagValue = parts[i + 1];
                        if ( flag.type().equalTo(RESTRICTED_VALUE) ) {
                            if ( ! flag.restrictingValues().contains(flagValue) ) {
                                problems.add(format("Flag '%s' accepts only values: %s",
                                        flag.name(),
                                        String.join(", ", flag.restrictingValues())));
                            }
                        }

                        List<String> values = flagValues.get(flag);
                        if ( isNull(values) ) {
                            values = new ArrayList<>();
                            flagValues.put(flag, values);
                        }
                        else if ( flag.isNotRepeatable() ) {
                            problems.add(format("Flag '%s' requires value!", flag.name()));
                        }
                        values.add(flagValue);

                        i++;
                    }
                    else {
                        problems.add(format("Flag '%s' requires value!", flag.name()));
                    }
                }
            }
            else {
                args.add(part);
            }
        }

        if ( ! problems.isEmpty() ) {
            return Either.withSecondary(problems);
        }

        return Either.withPrimary(new CommandImpl(line, args, flagValues));
    }

    private Supplier<IllegalArgumentException> noFlagOfName(String flagName) {
        return () -> new IllegalArgumentException(format("No defined flag of name '%s'", flagName));
    }
}
