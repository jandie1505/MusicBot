package net.jandie1505.musicbot.music;

import net.jandie1505.musicbot.system.MusicManager;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkipvoteManager implements Runnable {
    private static List<SkipvoteManager> objectList = new ArrayList<>();
    private MusicPlayer musicPlayer;
    private List<Member> memberList;
    Thread thread;

    public SkipvoteManager(MusicPlayer musicPlayer) {
        objectList.add(this);
        this.musicPlayer = musicPlayer;
        memberList = new ArrayList<>();
        thread = new Thread(this);
    }

    @Override
    public void run() {
        System.out.println("Skipvotemanager started: " + this);
        while(!thread.isInterrupted()) {
            if(MusicManager.getGuildIdFromMusicPlayer(musicPlayer) != null && musicPlayer != null) {

            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                thread.interrupt();
                this.destroy();
            }
        }
        System.out.println("Skipvotemanager stopped: " + this);
    }

    public void addSkipvote(Member m) {
        if(!memberList.contains(m)) {
            memberList.add(m);
        }
    }
    public void removeSkipvote(Member m) {
        if(memberList.contains(m)) {
            memberList.remove(m);
        }
    }
    public void clearSkipvotes() {
        memberList.clear();
    }
    public void clearNullSkipvotes() {
        memberList.removeIf(Objects::isNull);
    }

    public void destroy() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    // STATIC
    public static void destroyAll() {
        for(SkipvoteManager skipvoteManager : objectList) {
            skipvoteManager.destroy();
            objectList.remove(skipvoteManager);
        }
    }
}
