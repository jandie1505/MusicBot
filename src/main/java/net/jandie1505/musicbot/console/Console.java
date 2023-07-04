package net.jandie1505.musicbot.console;

import net.jandie1505.musicbot.MusicBot;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Console implements Runnable {
    // NOT STATIC
    private final MusicBot musicBot;
    private Thread thread;

    public Console(MusicBot musicBot) throws IOException {
        this.musicBot = musicBot;
    }

    @Override
    public void run() {

        MusicBot.LINE_READER.printAbove("Console started");

        while(thread == Thread.currentThread() && !thread.isInterrupted() && musicBot.isOperational()) {

            try {

                MusicBot.LINE_READER.printAbove(Commands.command(this.musicBot, MusicBot.LINE_READER.readLine("MusicBot ==> ")));

            } catch(Exception e) {

            }

        }

        MusicBot.LINE_READER.printAbove("Console stopped");
    }

    public void start() {
        if(this.thread == null || !thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("MUSICBOT-CONSOLE-" + this);
            thread.start();
        }
    }

    public void stop() {
        this.thread.interrupt();
    }
}
