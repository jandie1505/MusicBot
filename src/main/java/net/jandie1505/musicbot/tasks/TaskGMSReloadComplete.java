package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.system.GMS;

import java.util.concurrent.TimeUnit;

public class TaskGMSReloadComplete implements Runnable {
    private Thread thread;

    public TaskGMSReloadComplete() {
        thread = new Thread(this);
    }

    @Override
    public void run() {
        System.out.println("TaskGMSReloadComplete started");
        while(!thread.isInterrupted()) {
            try {
                TimeUnit.SECONDS.sleep(86400);
                GMS.reloadGuilds(true);
            } catch(Exception ignored) {}
        }
        System.out.println("TaskGMSReloadComplete stopped");
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
