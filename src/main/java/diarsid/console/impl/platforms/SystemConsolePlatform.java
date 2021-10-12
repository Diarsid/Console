package diarsid.console.impl.platforms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Optional;

import diarsid.console.api.io.ConsolePlatform;
import diarsid.console.impl.io.ExceptionHandler;

public class SystemConsolePlatform implements ConsolePlatform {

    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final ExceptionHandler exceptionHandler;

    public SystemConsolePlatform(ExceptionHandler exceptionHandler) {
        this.reader = reader();
        this.writer = writer();
        this.exceptionHandler = exceptionHandler;
    }

    private static BufferedWriter writer() {
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(System.console().writer());
        } catch (NullPointerException e) {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        return bufferedWriter;
    }

    private static BufferedReader reader() {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(System.console().reader());
        } catch (NullPointerException e) {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        }
        return bufferedReader;
    }

    @Override
    public Optional<String> readLine() {
        try {
            return Optional.ofNullable(this.reader.readLine().trim());
        }
        catch (IOException e) {
            this.exceptionHandler.accept(e);
            return Optional.empty();
        }
    }

    @Override
    public boolean println(String s) {
        try {
            this.writer.write(s);
            this.writer.newLine();
            this.writer.flush();
            return true;
        }
        catch (IOException e) {
            this.exceptionHandler.accept(e);
            return false;
        }
    }

    @Override
    public boolean print(String s) {
        try {
            this.writer.write(s);
            this.writer.flush();
            return true;
        }
        catch (IOException e) {
            this.exceptionHandler.accept(e);
            return false;
        }
    }

    @Override
    public boolean ln() {
        try {
            this.writer.newLine();
            this.writer.flush();
            return true;
        }
        catch (IOException e) {
            this.exceptionHandler.accept(e);
            return false;
        }
    }
}
