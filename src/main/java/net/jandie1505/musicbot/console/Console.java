package net.jandie1505.musicbot.console;

import net.jandie1505.musicbot.MusicBot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Console implements Runnable {
    // NOT STATIC
    private final MusicBot musicBot;
    private final Map<String, CommandExecutor> commands;
    private Thread thread;

    public Console(MusicBot musicBot) {
        this.musicBot = musicBot;
        this.commands = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void run() {

        MusicBot.LINE_READER.printAbove("Console started");

        while(thread == Thread.currentThread() && !thread.isInterrupted() && musicBot.isOperational()) {

            String commandString = MusicBot.LINE_READER.readLine("MusicBot ==> ");

            try {

                String reply = this.runCommand(commandString);

                MusicBot.LOGGER.debug("Issued console command " + commandString + " with response " + reply);
                MusicBot.LINE_READER.printAbove(reply);

            } catch(Exception e) {

                MusicBot.LOGGER.debug("Issued console command " + commandString + " threw an exception", e);
                MusicBot.LINE_READER.printAbove("Error while executing command " + commandString + " [" + e.toString() + "]");

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

    public String runCommand(String commandString) {

        String[] split = commandString.split(" ");
        String command = split[0];
        String[] arguments = new String[split.length - 1];

        for (int i = 1; i < split.length; i++) {
            arguments[i - 1] = split[i];
        }

        CommandExecutor commandExecutor = this.commands.get(command);

        String reply;

        if (commandExecutor != null) {
            return commandExecutor.onCommand(command, arguments);
        } else {
            return  "Unknown command. Type help to see available commands.";
        }
    }

    public void registerCommand(String command, CommandExecutor commandExecutor) {
        this.commands.put(command, commandExecutor);
    }

    public void removeCommand(String command) {
        this.commands.remove(command);
    }

    public Map<String, CommandExecutor> getCommands() {
        return Map.copyOf(this.commands);
    }
}
