package diarsid.console.api.io;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

/*
 * Output means text that is being printed into console from external sources
 * */
public interface Output extends Listenable {

    interface Channels {

        UUID add(BlockingQueue<String> sourceQueue);

        UUID add(Supplier<String> sourceQueue);

        boolean removeBy(UUID uuid);
    }

    void print(String s);

    void println(String s);

    void ln();

    Output.Channels channels();

}
