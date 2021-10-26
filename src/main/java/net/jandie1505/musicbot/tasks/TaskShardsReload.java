package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.MusicBot;

import java.util.concurrent.TimeUnit;

public class TaskShardsReload implements Runnable {
    private Thread thread;

    public TaskShardsReload() {
        thread = new Thread(this);
    }

    @Override
    public void run() {
        System.out.println("TaskShardsReload started");
        while(!thread.isInterrupted()) {
            try {
                TimeUnit.SECONDS.sleep(300);
                MusicBot.reloadShards();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("TaskShardsReload Error");
            }
        }
        System.out.println("TaskShardsReload stopped");
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
