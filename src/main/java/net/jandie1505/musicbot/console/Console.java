package net.jandie1505.musicbot.console;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Console implements Runnable {
    private Thread thread;

    public Console() {
        thread = new Thread(this);
    }

    @Override
    public void run() {
        System.out.println("Console started");
        while(!thread.isInterrupted()) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println(Commands.command(scanner.nextLine()));
            } catch(Exception ignored) {}
        }
        System.out.println("Console stopped");
    }

    public void start() {
        if(!thread.isAlive()) {
            thread.start();
        }
    }

    public void stop() {
        thread.interrupt();
    }



    // MESSAGES
    public static void messageShardManager(String msg) {
        timestampMessage("[SHARDS] " + msg);
    }

    public static void messageGMS(String msg) {
        timestampMessage("[GMS] " + msg);
    }

    public static void messageDB(String msg) {
        timestampMessage("[DB] " + msg);
    }

    public static void messageCHM(String msg) {
        timestampMessage("[CHM] " + msg);
    }

    public static void messageGMS(String msg, boolean important) {
        timestampMessage("[GMS] " + msg);
    }

    public static void messageDB(String msg, boolean important) {
        timestampMessage("[DB] " + msg);
    }

    public static void messageCHM(String msg, boolean important) {
        timestampMessage("[CHM] " + msg);
    }

    public static void timestampMessage(String msg) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        System.out.println("[" + dateTimeFormatter.format(localDateTime) + "] " + msg);
    }
}
