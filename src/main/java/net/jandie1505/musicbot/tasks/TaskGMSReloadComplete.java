package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.system.GMS;

import java.util.concurrent.TimeUnit;

public class TaskGMSReloadComplete implements Runnable {

    private final GMS gms;
    private Thread thread;

    public TaskGMSReloadComplete(GMS gms) {
        this.gms = gms;
    }

    @Override
    public void run() {
        System.out.println("TaskGMSReloadComplete started");
        while(thread == Thread.currentThread() && !thread.isInterrupted() && gms.getMusicBot().isOperational()) {
            try {
                TimeUnit.SECONDS.sleep(86400);
                gms.reloadGuilds(true);
            } catch(Exception ignored) {}
        }
        System.out.println("TaskGMSReloadComplete stopped");
    }

    public void start() {
        if(this.thread == null || !thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("MUSICBOT-TASK-GMSRELOADCOMPLETE-" + this);
            thread.start();
        }
    }

    public void stop() {
        this.thread.interrupt();
    }
}
