package net.jandie1505.musicbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.jandie1505.musicbot.console.Console;
import net.jandie1505.musicbot.system.*;
import net.jandie1505.musicbot.tasks.TaskGMSReload;
import net.jandie1505.musicbot.tasks.TaskGMSReloadComplete;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicBot {
    private static ShardManager shardManager;
    private static int shardsTotal = 1;
    private static boolean publicMode;
    private static boolean shardAutoMode = true;
    private static TaskGMSReload taskGMSReload;
    private static TaskGMSReloadComplete taskGMSReloadComplete;

    public static void main(String[] args) throws Exception {
        Console console = new Console();
        console.start();

        try {
            DatabaseManager.init();

            if(args.length >= 2) {
                shardsTotal = Integer.parseInt(args[1]);
            } else {
                System.out.println("No shards count specified. Starting with 1 shard.");
            }
            if(args.length >= 3) {
                if(args[2].equalsIgnoreCase("true")) {
                    publicMode = true;
                } else {
                    publicMode = false;
                }
            } else {
                System.out.println("Starting in private mode because the mode was not specified.");
            }

            System.out.println("*****************************\n" +
                    "* Starting bot in 3 seconds *\n" +
                    "*****************************");
            TimeUnit.SECONDS.sleep(3);

            if(shardsTotal > 0) {
                shardManager = DefaultShardManagerBuilder
                        .createDefault(args[0], GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_EMOJIS)
                        .setShardsTotal(shardsTotal)
                        .build();
                shardManager.addEventListener(new EventsBasic());
                shardManager.addEventListener(new EventsCommands());
            } else {
                System.out.println("Please enter a valid shards count");
            }
        } catch(LoginException e) {
            System.out.println("Check your bot token");
            TimeUnit.SECONDS.sleep(3);
            System.exit(-2);
        } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Check your start arguments");
            TimeUnit.SECONDS.sleep(3);
            System.exit(-1);
        }

        GMS.init();

        System.out.println(
                "*****************************************\n"
                        + "Application ID: " + shardManager.retrieveApplicationInfo().getJDA().getSelfUser().getApplicationId() + "\n"
                        + "Username: " + shardManager.retrieveApplicationInfo().getJDA().getSelfUser().getName() + "#" + shardManager.retrieveApplicationInfo().getJDA().getSelfUser().getDiscriminator() + "\n"
                        + "Public mode: " + getPublicMode() + "\n"
                        + "Shards: " + shardManager.getShardsRunning() + " + " + shardManager.getShardsQueued() + " = " + shardManager.getShardsTotal() + "\n"
                        + "*****************************************");

        shardManager.retrieveApplicationInfo().getJDA().awaitReady();

        onReady();

        MusicManager.init();
    }

    // READY AND SHUTDOWN
    private static void onReady() {
        taskGMSReload = new TaskGMSReload();
        taskGMSReloadComplete = new TaskGMSReloadComplete();

        GMS.reloadGuilds(true);

        taskGMSReload.start();
        taskGMSReloadComplete.start();

        System.out.println("*********\n" +
                "* READY *\n" +
                "*********\n");
    }

    public static void shutdown() {
        System.out.println("*****************\n" +
                "* SHUTTING DOWN *\n" +
                "*****************\n");
        shardManager.shutdown();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    // SHARD MANAGER
    public static void startShard(int shardId) {
        if(shardId >= 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    shardManager.start(shardId);
                    Console.messageShardManager("Started new shard with id " + shardId);
                }
            }).start();
        }
    }

    public static void stopShard(int shardId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(JDA jda : shardManager.getShards()) {
                    if(jda.getShardInfo().getShardId() == shardId) {
                        shardManager.shutdown(shardId);
                        Console.messageShardManager("Stopped and removed shard with id " + shardId);
                    }
                }
            }
        }).start();
    }

    public static void restartShard(int shardId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(JDA jda : shardManager.getShards()) {
                    if(jda.getShardInfo().getShardId() == shardId) {
                        shardManager.restart(shardId);
                        Console.messageShardManager("Restarted shard with id " + shardId);
                    }
                }
            }
        }).start();
    }

    public static void startShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Integer> activeIds = new ArrayList<>();
                for(JDA jda : shardManager.getShards()) {
                    activeIds.add(jda.getShardInfo().getShardId());
                }
                Collections.sort(activeIds);
                for(int i = 0; i < shardManager.getShardsTotal(); i++) {
                    if(!activeIds.contains(i)) {
                        shardManager.start(i);
                    }
                }
                Console.messageShardManager("Started all shards");
            }
        }).start();
    }

    public static void stopShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(JDA jda : MusicBot.shardManager.getShards()) {
                    shardManager.shutdown(jda.getShardInfo().getShardId());
                }
                Console.messageShardManager("Stopped and remove all shards");
            }
        }).start();
    }

    public static void restartShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startShards();
                Console.messageShardManager("Restarted all shards");
            }
        }).start();
    }

    public static void setShardAutoMode(boolean mode) {
        shardAutoMode = mode;
    }

    public static boolean getShardAutoMode() {
        return shardAutoMode;
    }

    public static void reloadShards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(shardManager.getShardsRunning() < shardManager.getShardsTotal()) {
                    if(getShardAutoMode()) {
                        Console.messageShardManager("Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online. Auto restarting...");
                        startShards();
                    } else {
                        Console.messageShardManager("[WARN] Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online");
                    }
                }
            }
        }).start();
    }

    public static boolean completeOnline() {
        return (shardManager.getShardsRunning() == shardManager.getShardsTotal());
    }

    // GETTER METHODS
    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static boolean getPublicMode() {
        return publicMode;
    }
}
