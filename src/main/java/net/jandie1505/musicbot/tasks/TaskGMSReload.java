package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.system.GMS;

import java.util.concurrent.TimeUnit;

public class TaskGMSReload implements Runnable {
    private Thread thread;

    public TaskGMSReload() {
        thread = new Thread(this);
    }

    @Override
    public void run() {
        System.out.println("TaskGMSReload started");
        while(!thread.isInterrupted()) {
            try {
                TimeUnit.SECONDS.sleep(900);
                GMS.reloadGuilds(false);
            } catch(Exception ignored) {}
        }
        System.out.println("TaskGMSReload stopped");
    }

    public void start() {
        if(!thread.isAlive()) {
            thread.start();
        }
    }

    public void stop() {
        thread.interrupt();
    }
}
