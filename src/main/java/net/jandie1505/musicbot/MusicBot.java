package net.jandie1505.musicbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
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
        upsertCommands();

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

    // UPSERT COMMANDS
    public static void upsertCommands() {
        shardManager.retrieveApplicationInfo().getJDA().retrieveCommands().queue(commands -> {
            JDA jda = shardManager.retrieveApplicationInfo().getJDA();

            List<String> cmdNameList = new ArrayList<>();

            for(Command cmd : commands) {
                cmdNameList.add(cmd.getName());
            }

            if(!cmdNameList.contains("cmd")) {
                CommandData cmdCommand = new CommandData("cmd", "cmd")
                        .addOptions(new OptionData(OptionType.STRING, "cmd", "cmd").setRequired(true));
                jda.upsertCommand(cmdCommand).queue();
            }
            if(!cmdNameList.contains("nowplaying")) {
                CommandData nowplayingCommand = new CommandData("nowplaying", "Shows information about the song which is currently playing");
                jda.upsertCommand(nowplayingCommand).queue();
            }
            if(!cmdNameList.contains("queue")) {
                CommandData queueCommand = new CommandData("queue", "Shows the current queue");
                jda.upsertCommand(queueCommand).queue();
            }
            if(!cmdNameList.contains("play")) {
                CommandData playCommand = new CommandData("play", "Play a song")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song link / song name / playlist link you want to play"));
                jda.upsertCommand(playCommand).queue();
            }
            if(!cmdNameList.contains("remove")) {
                CommandData removeCommand = new CommandData("remove", "Remove a specific song from the queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "index", "The index of the song you want to remove").setRequired(true));
                jda.upsertCommand(removeCommand).queue();
            }
            if(!cmdNameList.contains("search")) {
                CommandData searchCommand = new CommandData("search", "YTSearchHandler youtube")
                        .addOptions(new OptionData(OptionType.STRING, "query", "The text you want to search for").setRequired(true));
                jda.upsertCommand(searchCommand).queue();
            }
            if(!cmdNameList.contains("shuffle")) {
                CommandData shuffleCommand = new CommandData("shuffle", "Shuffle the queue");
                jda.upsertCommand(shuffleCommand).queue();
            }
            if(!cmdNameList.contains("skip")) {
                CommandData skipCommand = new CommandData("skip", "Skip a song")
                        .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                jda.upsertCommand(skipCommand).queue();
            }
            if(!cmdNameList.contains("removeuser")) {
                CommandData removeUserCommand = new CommandData("removeuser", "Removes all songs by a specific member")
                        .addOptions(new OptionData(OptionType.USER, "member", "The member you want to remove the music from").setRequired(true));
                jda.upsertCommand(removeUserCommand).queue();
            }
            if(!cmdNameList.contains("forceskip")) {
                CommandData forceskipCommand = new CommandData("forceskip", "Force skip a song")
                        .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                jda.upsertCommand(forceskipCommand).queue();
            }
            if(!cmdNameList.contains("movetrack")) {
                CommandData movetrackCommand = new CommandData("movetrack", "Move a specific track in queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "from", "The track you want to move").setRequired(true))
                        .addOptions(new OptionData(OptionType.INTEGER, "to", "The queue position you want to move the track to").setRequired(true));
                jda.upsertCommand(movetrackCommand).queue();
            }
            if(!cmdNameList.contains("playnow")) {
                CommandData playnowCommand = new CommandData("playnow", "Stop the current song and play the specified song immediately")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song you want to play").setRequired(true));
                jda.upsertCommand(playnowCommand).queue();
            }
            if(!cmdNameList.contains("stop")) {
                CommandData stopCommand = new CommandData("stop", "Stop playing music");
                jda.upsertCommand(stopCommand).queue();
            }
            if(!cmdNameList.contains("volume")) {
                CommandData volumeCommand = new CommandData("volume", "Change the volume")
                        .addOptions(new OptionData(OptionType.INTEGER, "volume", "Change the volume to this value"));
                jda.upsertCommand(volumeCommand).queue();
            }
            if(!cmdNameList.contains("leave")) {
                CommandData leaveCommand = new CommandData("leave", "Leave the voice channel");
                jda.upsertCommand(leaveCommand).queue();
            }
            if(!cmdNameList.contains("mbsettings")) {
                SubcommandData mbsettingsInfoCommand = new SubcommandData("info", "See an overview of all settings");
                SubcommandData mbsettingsDJRoleCommand = new SubcommandData("djrole", "Add/remove/clear dj roles")
                        .addOptions(new OptionData(OptionType.STRING, "action", "add/remove").setRequired(true).addChoice("add", "add").addChoice("remove", "remove").addChoice("clear", "clear"))
                        .addOptions(new OptionData(OptionType.ROLE, "role", "Only required if you have chosen add/remove"));
                SubcommandData mbsettingsEphemeralCommand = new SubcommandData("ephemeral", "Enable/disable private (ephemeral) reply")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "state", "Set to true to enable private (ephemeral) replies").setRequired(true));
                CommandData mbsettingsCommand = new CommandData("mbsettings", "Music bot settings command for administrators")
                        .addSubcommands(mbsettingsInfoCommand)
                        .addSubcommands(mbsettingsDJRoleCommand)
                        .addSubcommands(mbsettingsEphemeralCommand);
                jda.upsertCommand(mbsettingsCommand).queue();
            }

            Console.messageShardManager("Upserted commands");
        });
    }

    // GETTER METHODS
    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static boolean getPublicMode() {
        return publicMode;
    }
}
