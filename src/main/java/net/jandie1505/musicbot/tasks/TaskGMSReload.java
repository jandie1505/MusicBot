package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.system.GMS;

import java.util.concurrent.TimeUnit;

public class TaskGMSReload implements Runnable {

    private final GMS gms;
    private Thread thread;

    public TaskGMSReload(GMS gms) {
        this.gms = gms;
    }

    @Override
    public void run() {
        System.out.println("TaskGMSReload started");
        while(thread == Thread.currentThread() && Thread.currentThread().isInterrupted() && gms.getMusicBot().isOperational()) {
            try {
                TimeUnit.SECONDS.sleep(900);
                gms.reloadGuilds(false);
            } catch(Exception ignored) {}
        }
        System.out.println("TaskGMSReload stopped");
    }

    public void start() {
        if(this.thread == null || !this.thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("MUSICBOT-TASK-GMSRELOAD-" + this);
            thread.start();
        }
    }

    public void stop() {
        this.thread.interrupt();
    }
}
