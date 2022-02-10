package net.jandie1505.musicbot.console;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Console implements Runnable {
    // STATIC
    private static boolean GMSLogging;
    private static boolean DBMLogging;

    // NOT STATIC
    private Thread thread;

    public Console() {
        thread = new Thread(this);
    }

    @Override
    public void run() {
        System.out.println("Console started");
        while(!thread.isInterrupted()) {
            try {
                Scanner scan = new Scanner(System.in);
                System.out.println(Commands.command(scan.nextLine()));
            } catch(Exception ignored) {}
        }
        System.out.println("Console stopped");
    }

    public void start() {
        if(!thread.isAlive()) {
            thread.start();
        }
    }

    /*
    public void stop() {
        thread.interrupt();
    }
     */



    // STATIC
    public static void messageShardManager(String msg) {
        timestampMessage("[SHARDS] " + msg);
    }

    public static void messageGMS(String msg) {
        if(GMSLogging) {
            timestampMessage("[GMS] " + msg);
        }
    }
    public static void messageGMS(String msg, boolean important) {
        if(GMSLogging || important) {
            timestampMessage("[GMS] " + msg);
        }
    }

    public static void messageDB(String msg) {
        if(DBMLogging) {
            timestampMessage("[DB] " + msg);
        }
    }
    public static void messageDB(String msg, boolean important) {
        if(DBMLogging || important) {
            timestampMessage("[DB] " + msg);
        }
    }

    public static void defaultMessage(String msg) {
        System.out.println(msg);
    }

    public static void timestampMessage(String msg) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        System.out.println("[" + dateTimeFormatter.format(localDateTime) + "] " + msg);
    }

    public static void setGMSLogging(boolean state) {
        GMSLogging = state;
    }

    public static void setDBMLogging(boolean state) {
        DBMLogging = state;
    }

    public static boolean isGMSLogging() {
        return GMSLogging;
    }

    public static boolean isDBMLogging() {
        return DBMLogging;
    }
}
