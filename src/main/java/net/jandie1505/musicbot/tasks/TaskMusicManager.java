package net.jandie1505.musicbot.tasks;

import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.system.MusicManager;

import java.util.concurrent.TimeUnit;

public class TaskMusicManager implements Runnable {
    private Thread thread;

    public TaskMusicManager() {
        thread = new Thread(this);
    }

    @Override
    public void run() {
        System.out.println("TaskMusicManager started");
        while(!thread.isInterrupted()) {
            try {
                TimeUnit.SECONDS.sleep(300);
                MusicManager.reloadPlayers();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("TaskMusicManager Error");
            }
        }
        System.out.println("TaskMusicManager stopped");
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
