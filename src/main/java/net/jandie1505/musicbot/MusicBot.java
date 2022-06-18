package net.jandie1505.musicbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.jandie1505.musicbot.console.Console;
import net.jandie1505.musicbot.search.SpotifySearchHandler;
import net.jandie1505.musicbot.system.*;
import net.jandie1505.musicbot.tasks.TaskGMSReload;
import net.jandie1505.musicbot.tasks.TaskGMSReloadComplete;
import net.jandie1505.musicbot.tasks.TaskMusicManager;
import net.jandie1505.musicbot.tasks.TaskShardsReload;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MusicBot {
    //STATIC
    private static MusicBot instance;

    // INSTANCE
    private Console console;
    private ShardManager shardManager;
    private final DatabaseManager databaseManager;
    private final GMS gms;
    private final MusicManager musicManager;
    private final int shardsTotal;
    private boolean publicMode;
    private boolean shardAutoMode = true;
    private String bowOwner = "";
    private TaskShardsReload taskShardsReload;

    public MusicBot(String token, int shardsCount) throws LoginException, SQLException, IOException, ClassNotFoundException {
        this.console = new Console();
        this.console.start();

        this.databaseManager = new DatabaseManager(this);

        if(shardsCount > 1) {
            this.shardsTotal = shardsCount;
        } else {
            this.shardsTotal = 1;
        }

        this.shardManager = DefaultShardManagerBuilder
                .createDefault(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_EMOJIS)
                .setShardsTotal(shardsTotal)
                .build();
        this.shardManager.setPresence(OnlineStatus.IDLE, Activity.playing("Starting up..."));
        this.shardManager.addEventListener(new EventsBasic());
        this.shardManager.addEventListener(new EventsCommands());
        this.shardManager.addEventListener(new EventsButtons());

        this.reloadShards();

        this.taskShardsReload = new TaskShardsReload(this);
        this.taskShardsReload.start();

        this.gms = new GMS(this);
        this.gms.reloadGuilds(true);

        this.musicManager = new MusicManager(this);
        this.musicManager.reloadPlayers();

        Console.defaultMessage(
                "*****************************************\n"
                        + "Application ID: " + this.shardManager.retrieveApplicationInfo().getJDA().getSelfUser().getApplicationId() + "\n"
                        + "Username: " + this.shardManager.retrieveApplicationInfo().getJDA().getSelfUser().getName() + "#" + shardManager.retrieveApplicationInfo().getJDA().getSelfUser().getDiscriminator() + "\n"
                        + "Public mode: " + this.getPublicMode() + "\n"
                        + "Shards: " + this.shardManager.getShardsRunning() + " + " + shardManager.getShardsQueued() + " = " + shardManager.getShardsTotal() + "\n"
                        + "*****************************************");
    }

    public void shutdown() {
        System.out.println("*****************\n" +
                "* SHUTTING DOWN *\n" +
                "*****************\n");
        try {
            this.shardManager.shutdown();
            TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e) {
            e.printStackTrace();
        } catch(Exception ignored) {}
        System.exit(1);
    }

    // SHARD MANAGER
    public void startShard(int shardId) {
        if(shardId >= 0) {
            new Thread(() -> {
                shardManager.start(shardId);
                Console.messageShardManager("Started new shard with id " + shardId);
            }).start();
        }
    }

    public void stopShard(int shardId) {
        new Thread(() -> {
            for(JDA jda : shardManager.getShards()) {
                if(jda.getShardInfo().getShardId() == shardId) {
                    shardManager.shutdown(shardId);
                    Console.messageShardManager("Stopped and removed shard with id " + shardId);
                }
            }
        }).start();
    }

    public void restartShard(int shardId) {
        new Thread(() -> {
            for(JDA jda : shardManager.getShards()) {
                if(jda.getShardInfo().getShardId() == shardId) {
                    shardManager.restart(shardId);
                    Console.messageShardManager("Restarted shard with id " + shardId);
                }
            }
        }).start();
    }

    public void startShards() {
        new Thread(() -> {
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
        }).start();
    }

    public void stopShards() {
        new Thread(() -> {
            for(JDA jda : shardManager.getShards()) {
                shardManager.shutdown(jda.getShardInfo().getShardId());
            }
            Console.messageShardManager("Stopped and remove all shards");
        }).start();
    }

    public void restartShards() {
        new Thread(() -> {
            startShards();
            Console.messageShardManager("Restarted all shards");
        }).start();
    }

    public void setShardAutoMode(boolean mode) {
        this.shardAutoMode = mode;
    }

    public boolean getShardAutoMode() {
        return this.shardAutoMode;
    }

    public void reloadShards() {
        new Thread(() -> {
            if(shardManager.getShardsRunning() < shardManager.getShardsTotal()) {
                if(getShardAutoMode()) {
                    Console.messageShardManager("Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online. Auto restarting...");
                    startShards();
                } else {
                    Console.messageShardManager("[WARN] Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online");
                }
            }

            if(completeOnline()) {
                shardManager.setPresence(OnlineStatus.ONLINE, Activity.playing("/play, /help"));
            } else {
                shardManager.setPresence(OnlineStatus.IDLE, Activity.playing("Limited functionality"));
            }
        }).start();
    }

    public boolean completeOnline() {
        boolean status = true;
        for(JDA jda : shardManager.getStatuses().keySet()) {
            if(jda.getStatus() != JDA.Status.CONNECTED) {
                status = false;
            }
        }
        return (shardManager.getShardsRunning() == shardManager.getShardsTotal()) && status;
    }

    // UPSERT COMMANDS
    public void upsertCommands(boolean reloadall) {
        shardManager.retrieveApplicationInfo().getJDA().retrieveCommands().queue(commands -> {
            JDA jda = shardManager.retrieveApplicationInfo().getJDA();

            List<String> cmdNameList = new ArrayList<>();

            for(Command cmd : commands) {
                cmdNameList.add(cmd.getName());
            }

            if(!cmdNameList.contains("cmd") || reloadall) {
                CommandData cmdCommand = new CommandDataImpl("cmd", "cmd")
                        .addOptions(new OptionData(OptionType.STRING, "cmd", "cmd").setRequired(true));
                jda.upsertCommand(cmdCommand).queue();
                Console.timestampMessage("Registered command cmd");
            }
            if(!cmdNameList.contains("invite") || reloadall) {
                CommandData inviteCommand = new CommandDataImpl("invite", "Invite MusicBot to your server");
                jda.upsertCommand(inviteCommand).queue();
                Console.timestampMessage("Registered command invite");
            }
            if(!cmdNameList.contains("help") || reloadall) {
                CommandData commandData = new CommandDataImpl("help", "Get help and information about MusicBot");
                jda.upsertCommand(commandData).queue();
                Console.timestampMessage("Registered command help");
            }
            if(!cmdNameList.contains("nowplaying") || reloadall) {
                CommandData nowplayingCommand = new CommandDataImpl("nowplaying", "Shows information about the song which is currently playing");
                jda.upsertCommand(nowplayingCommand).queue();
                Console.timestampMessage("Registered command nowplaying");
            }
            if(!cmdNameList.contains("queue") || reloadall) {
                CommandData queueCommand = new CommandDataImpl("queue", "Shows the current queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "index", "Search for queue indexes"));
                jda.upsertCommand(queueCommand).queue();
                Console.timestampMessage("Registered command queue");
            }
            if(!cmdNameList.contains("play") || reloadall) {
                CommandData playCommand = new CommandDataImpl("play", "Play a song")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song link / song name / playlist link you want to play"));
                jda.upsertCommand(playCommand).queue();
                Console.timestampMessage("Registered command play");
            }
            if(!cmdNameList.contains("add") || reloadall) {
                CommandData addCommand = new CommandDataImpl("add", "Add a song to queue without starting to play")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song link / song name / playlist link you want to add").setRequired(true));
                jda.upsertCommand(addCommand).queue();
                Console.timestampMessage("Registered command add");
            }
            if(!cmdNameList.contains("pause") || reloadall) {
                CommandData pauseCommand = new CommandDataImpl("pause", "Stop playing music");
                jda.upsertCommand(pauseCommand).queue();
                Console.timestampMessage("Registered command pause");
            }
            if(!cmdNameList.contains("remove") || reloadall) {
                CommandData removeCommand = new CommandDataImpl("remove", "Remove a specific song from the queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "index", "The index of the song you want to remove").setRequired(true));
                jda.upsertCommand(removeCommand).queue();
                Console.timestampMessage("Registered command remove");
            }
            if(!cmdNameList.contains("clear") || reloadall) {
                CommandData clearCommand = new CommandDataImpl("clear", "Clear the queue");
                jda.upsertCommand(clearCommand).queue();
                Console.timestampMessage("Registered command clear");
            }
            if(!cmdNameList.contains("search") || reloadall) {
                CommandData searchCommand = new CommandDataImpl("search", "YTSearchHandler youtube")
                        .addOptions(new OptionData(OptionType.STRING, "query", "The text you want to search for").setRequired(true));
                jda.upsertCommand(searchCommand).queue();
                Console.timestampMessage("Registered command search");
            }
            if(!cmdNameList.contains("shuffle") || reloadall) {
                CommandData shuffleCommand = new CommandDataImpl("shuffle", "Shuffle the queue");
                jda.upsertCommand(shuffleCommand).queue();
                Console.timestampMessage("Registered command shuffle");
            }
            if(!cmdNameList.contains("skip") || reloadall) {
                CommandData skipCommand = new CommandDataImpl("skip", "Skip a song")
                        .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                jda.upsertCommand(skipCommand).queue();
                Console.timestampMessage("Registered command skip");
            }
            if(!cmdNameList.contains("removeuser") || reloadall) {
                CommandData removeUserCommand = new CommandDataImpl("removeuser", "Removes all songs by a specific member")
                        .addOptions(new OptionData(OptionType.USER, "member", "The member you want to remove the music from").setRequired(true));
                jda.upsertCommand(removeUserCommand).queue();
                Console.timestampMessage("Registered command removeuser");
            }
            if(!cmdNameList.contains("forceskip") || reloadall) {
                CommandData forceskipCommand = new CommandDataImpl("forceskip", "Force skip a song")
                        .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                jda.upsertCommand(forceskipCommand).queue();
                Console.timestampMessage("Registered command forceskip");
            }
            if(!cmdNameList.contains("movetrack") || reloadall) {
                CommandData movetrackCommand = new CommandDataImpl("movetrack", "Move a specific track in queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "from", "The track you want to move").setRequired(true))
                        .addOptions(new OptionData(OptionType.INTEGER, "to", "The queue position you want to move the track to").setRequired(true));
                jda.upsertCommand(movetrackCommand).queue();
                Console.timestampMessage("Registered command movetrack");
            }
            if(!cmdNameList.contains("playnow") || reloadall) {
                CommandData playnowCommand = new CommandDataImpl("playnow", "Stop the current song and play the specified song immediately")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song you want to play").setRequired(true));
                jda.upsertCommand(playnowCommand).queue();
                Console.timestampMessage("Registered command playnow");
            }
            if(!cmdNameList.contains("stop") || reloadall) {
                CommandData stopCommand = new CommandDataImpl("stop", "Stop playing music");
                jda.upsertCommand(stopCommand).queue();
                Console.timestampMessage("Registered command stop");
            }
            if(!cmdNameList.contains("volume") || reloadall) {
                CommandData volumeCommand = new CommandDataImpl("volume", "Change the volume")
                        .addOptions(new OptionData(OptionType.INTEGER, "volume", "Change the volume to this value"));
                jda.upsertCommand(volumeCommand).queue();
                Console.timestampMessage("Registered command volume");
            }
            if(!cmdNameList.contains("leave") || reloadall) {
                CommandData leaveCommand = new CommandDataImpl("leave", "Leave the voice channel");
                jda.upsertCommand(leaveCommand).queue();
                Console.timestampMessage("Registered command leave");
            }
            if(!cmdNameList.contains("mbsettings") || reloadall) {
                SubcommandData mbsettingsInfoCommand = new SubcommandData("info", "See an overview of all settings");
                SubcommandData mbsettingsDJRoleCommand = new SubcommandData("djrole", "Add/remove/clear dj roles")
                        .addOptions(new OptionData(OptionType.STRING, "action", "add/remove").setRequired(true).addChoice("add", "add").addChoice("remove", "remove").addChoice("clear", "clear"))
                        .addOptions(new OptionData(OptionType.ROLE, "role", "Only required if you have chosen add/remove"));
                SubcommandData mbsettingsEphemeralCommand = new SubcommandData("ephemeral", "Enable/disable private (ephemeral) reply")
                        .addOptions(new OptionData(OptionType.BOOLEAN, "state", "Set to true to enable private (ephemeral) replies").setRequired(true));
                SubcommandData mbsettingsBlacklistCommand = new SubcommandData("blacklist", "Manage the blacklist")
                        .addOptions(new OptionData(OptionType.STRING, "action", "Add/remove/clear/list").setRequired(true).addChoice("add", "add").addChoice("remove", "remove").addChoice("clear", "clear").addChoice("list", "list"))
                        .addOptions(new OptionData(OptionType.STRING, "link", "The source you want to blacklist"));
                SubcommandData mbsettingsKeywordBlacklistCommand = new SubcommandData("keywordblacklist", "Manage the keyword blacklist")
                        .addOptions(new OptionData(OptionType.STRING, "action", "keyword").setRequired(true).addChoice("add", "add").addChoice("remove", "remove").addChoice("clear", "clear").addChoice("list", "list"))
                        .addOptions(new OptionData(OptionType.STRING, "keyword", "The keyword you want to blacklist"));
                SubcommandData mbsettingsArtistBlacklistCommand = new SubcommandData("artistblacklist", "Manage the artist blacklist")
                        .addOptions(new OptionData(OptionType.STRING, "action", "artist").setRequired(true).addChoice("add", "add").addChoice("remove", "remove").addChoice("clear", "clear").addChoice("list", "list"))
                        .addOptions(new OptionData(OptionType.STRING, "keyword", "The artist name you want to blacklist"));
                CommandData mbsettingsCommand = new CommandDataImpl("mbsettings", "Music bot settings command for administrators")
                        .addSubcommands(mbsettingsInfoCommand)
                        .addSubcommands(mbsettingsDJRoleCommand)
                        .addSubcommands(mbsettingsEphemeralCommand)
                        .addSubcommands(mbsettingsBlacklistCommand)
                        .addSubcommands(mbsettingsKeywordBlacklistCommand)
                        .addSubcommands(mbsettingsArtistBlacklistCommand);
                jda.upsertCommand(mbsettingsCommand).queue();
                Console.timestampMessage("Registered command mbsettings");
            }

            Console.messageShardManager("Command setup (Mode: " + reloadall + ")");
        });
    }

    // GETTER METHODS
    public ShardManager getShardManager() {
        return shardManager;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public GMS getGMS() {
        return this.gms;
    }

    public boolean getPublicMode() {
        return publicMode;
    }

    public String getBowOwner() {
        return bowOwner;
    }

    public Console getConsole() {
        return console;
    }

    // OPERATIONAL STATUS

    public boolean isOperational() {
        return true;
    }

    // STATIC

    public static void main(String[] args) throws Exception {
        System.out.println("MusicBot by jandie1505 (https://github.com/jandie1505/MusicBot)");

        int waitTime = 3;
        Map<String, String> startArguments = new HashMap<>();
        try {
            for (String arg : args) {
                if (arg.startsWith("-")) {
                    arg = arg.replace("-", "");
                    try {
                        String[] argument = arg.split("=");
                        startArguments.put(argument[0], argument[1]);
                    } catch (Exception e) {
                        System.out.println("Incorrect start argument: " + arg);
                        waitTime = 10;
                    }
                } else {
                    System.out.println("Wrong start argument format: " + arg);
                    waitTime = 10;
                }
            }
        } catch (Exception e) {
            System.out.println("Error with start arguments. Starting with default arguments...");
            waitTime = 30;
        }

        boolean defaultConfigValues = false;
        defaultConfigValues = Boolean.parseBoolean(startArguments.get("defaultConfigValues"));
        if (defaultConfigValues) {
            System.out.println("defaultConfigValues option enabled");
        }

        System.out.println("Starting server in " + waitTime + " seconds...");
        try {
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (Exception ignored) {}

        new MusicBot();
    }
}
