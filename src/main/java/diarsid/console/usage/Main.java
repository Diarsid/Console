package diarsid.console.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.console.api.Console;
import diarsid.console.api.format.ConsoleFormat;
import diarsid.console.api.io.Command;
import diarsid.console.api.io.operations.OperationLogic;
import diarsid.console.impl.FlagImpl;
import diarsid.console.impl.building.ConsoleBuilding;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.runAsync;

import static diarsid.console.api.format.ConsoleFormatElement.NAME;
import static diarsid.console.api.io.Command.Flag.Type.OPEN_VALUE;

public class Main {

    public static void main(String... args) {

        BlockingQueue<String> outputSource = new ArrayBlockingQueue<>(10);
        AtomicInteger counter = new AtomicInteger();
        runAsync(() -> {
            while (true) {
                asleep(10_000);
                try{
                    outputSource.put("out_" + counter.getAndIncrement());
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        OperationLogic operation = (console, command) -> {
            List<String> answers = new ArrayList<>();
            String answer;
            for (int i = 0; i < 3; i++) {
                console.print(" [" + i + "] : ");
                answer = console.readLine().orElse("<no>");
                answers.add(answer);
            }

            answers.add("done " + command.args().size());
            return answers;
        };

        OperationLogic echo = (console, command) -> {
            List<String> answers = new ArrayList<>();
            if ( command.args().isEmpty() ) {
                answers.add("command has no args");
            }
            if ( command.flags().isEmpty() ) {
                answers.add("command has no flags");
            }

            for ( String arg : command.args() ) {
                answers.add("arg: " + arg);
            }

            for ( Command.Flag flag : command.flags() ) {
                answers.add("flag: " + flag.name() + ", value: " + command.valueOf(flag).orElse("<none>"));
            }

            return answers;
        };

        Console console = new ConsoleBuilding()
                .withFormat(ConsoleFormat.building().with(NAME, "name"))
                .withOperation(builder -> builder
                        .named("runner")
                        .matching(command -> command.raw().startsWith("run"))
                        .doing(operation))
                .withOperation(builder -> builder
                        .named("echo")
                        .matching(command -> command.raw().startsWith("echo"))
                        .doing(echo))
//                .withDefaultProcessing()
                .withFlag(new FlagImpl(OPEN_VALUE, "flag", "f", true))
                .withFlag(Command.Flag.withValidatedValues(
                        "uuid",
                        "u",
                        false,
                        (s) -> {
                            try {
                                UUID.fromString(s);
                            }
                            catch (Throwable t) {
                                throw new IllegalArgumentException(format("%s is not an uuid!", s));
                            }
                        }))
                .stopWhenInputIs("exit")
                .enableExitConfirmation("y", "+", "yes")
                .done();

//        Command.Flag flag = console.flags().add();

        console.life().start();
        UUID uuid = console.output().listeners().add(output -> System.out.println("   [listen] : " + output));
        console.output().channels().add(outputSource);
        console.output().listeners().remove(uuid);
//        console.life().stop();

    }

    static void asleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {

        }
    }
}
