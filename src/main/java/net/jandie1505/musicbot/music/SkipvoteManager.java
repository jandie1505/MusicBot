package net.jandie1505.musicbot.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.system.MusicManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SkipvoteManager implements Runnable {
    private static final List<SkipvoteManager> objectList = new ArrayList<>();
    private final MusicPlayer musicPlayer;
    private final List<Member> memberList;
    private int timer;
    private double membercount;
    private double votecount;
    private double requiredvotes;
    Thread thread;

    public SkipvoteManager(MusicPlayer musicPlayer, int timer) {
        objectList.add(this);
        this.musicPlayer = musicPlayer;
        memberList = new LinkedList<>();
        thread = new Thread(this);
        thread.start();
        this.timer = timer;
    }

    @Override
    public void run() {
        Guild g = musicPlayer.getMusicManager().getMusicBot().getShardManager().getGuildById(musicPlayer.getMusicManager().getGuildIdFromMusicPlayer(musicPlayer));
        while(!thread.isInterrupted()) {
            try {
                if(g != null && musicPlayer != null && g.getSelfMember().getVoiceState().inAudioChannel()) {
                    this.membercount = g.getSelfMember().getVoiceState().getChannel().getMembers().size() - 1;
                    this.votecount = 0;
                    if(!memberList.isEmpty()) {
                        for(Member m : memberList) {
                            if(m != null && m.getVoiceState().inAudioChannel() && m.getVoiceState().getChannel() == g.getSelfMember().getVoiceState().getChannel()) {
                                votecount++;
                            } else {
                                memberList.remove(m);
                            }
                        }
                    }
                    this.requiredvotes = Math.ceil(membercount / 2);
                    if(votecount >= requiredvotes && votecount != 0 && requiredvotes != 0) {
                        this.skip();
                    }
                } else {
                    Thread.sleep(100);
                    thread.interrupt();
                    this.destroy();
                }
                if(timer > 0) {
                    timer--;
                } else {
                    this.expired();
                }
                TimeUnit.SECONDS.sleep(1);
            } catch(Exception e) {
                e.printStackTrace();
                this.destroy();
            }
        }
    }

    /**
     * This will be executed if the skipvote was successful
     */
    private void skip() {
        if(musicPlayer != null) {
            musicPlayer.nextTrack();
        }
        this.destroy();
    }

    /**
     * This will be executed if the skipvote wasn't successful and expired
     */
    private void expired() {
        this.destroy();
    }

    /**
     * Add a skipvote
     * @param m Member
     */
    public void addSkipvote(Member m) {
        if(!memberList.contains(m)) {
            memberList.add(m);
        }
    }

    /**
     * Remove a skipvote
     * @param m Member
     */
    public void removeSkipvote(Member m) {
        if(memberList.contains(m)) {
            memberList.remove(m);
        }
    }

    /**
     * Delete all skipvotes
     */
    public void clearSkipvotes() {
        memberList.clear();
    }

    /**
     * Clear all skipvotes of null members
     */
    public void clearNullSkipvotes() {
        memberList.removeIf(Objects::isNull);
    }

    /**
     * Get the list of all members that have skipvoted
     * @return List of members
     */
    public List<Member> getSkipvotes() {
        return memberList;
    }

    /**
     * Get the count of skipvotes
     * @return count of skipvotes
     */
    public int getVoteCount() {
        return (int) this.votecount;
    }

    /**
     * Get the count of required votes to skip
     * @return required votes
     */
    public int getRequiredVotes() {
        return (int) this.requiredvotes;
    }

    /**
     * Destroy this skipvote manager
     */
    public void destroy() {
        /*try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */
        if(!thread.isInterrupted()) {
            thread.interrupt();
        }
        if(musicPlayer != null) {
            musicPlayer.destroySkipvoteManager();
        }
        System.out.println("skipvotemanager destroyed");
    }

    // STATIC

    /**
     * Destroy all instances of skipvote managers
     */
    public static void destroyAll() {
        for(SkipvoteManager skipvoteManager : objectList) {
            skipvoteManager.destroy();
            objectList.remove(skipvoteManager);
        }
    }
}
