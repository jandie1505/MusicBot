package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.system.MusicManager;

import java.util.concurrent.TimeUnit;

public class TaskMusicManager implements Runnable {

    private final MusicManager musicManager;
    private Thread thread;

    public TaskMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    @Override
    public void run() {
        System.out.println("TaskMusicManager started");
        while(thread == Thread.currentThread() && !thread.isInterrupted() && musicManager.getMusicBot().isOperational()) {
            try {
                TimeUnit.SECONDS.sleep(300);
                musicManager.reloadPlayers();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("TaskMusicManager Error");
            }
        }
        System.out.println("TaskMusicManager stopped");
    }

    public void start() {
        if(this.thread == null || !this.thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("MUSICBOT-TASK-MUSICMANAGER-" + this);
            thread.start();
        }
    }

    public void stop() {
        this.thread.interrupt();
    }
}
