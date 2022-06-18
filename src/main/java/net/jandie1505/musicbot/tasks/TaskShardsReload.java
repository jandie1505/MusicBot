package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.MusicBot;

import java.util.concurrent.TimeUnit;

public class TaskShardsReload implements Runnable {

    private final MusicBot musicBot;
    private Thread thread;

    public TaskShardsReload(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public void run() {
        System.out.println("TaskShardsReload started");
        while(thread == Thread.currentThread() && !thread.isInterrupted() && musicBot.isOperational()) {
            try {
                TimeUnit.SECONDS.sleep(300);
                musicBot.reloadShards();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("TaskShardsReload Error");
            }
        }
        System.out.println("TaskShardsReload stopped");
    }

    public void start() {
        if(this.thread == null || !this.thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("MUSICBOT-TASK-SHARDSRELOAD-" + this);
            thread.start();
        }
    }

    public void stop() {
        this.thread.interrupt();
    }
}
