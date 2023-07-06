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
import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.jandie1505.musicbot.config.ConfigManager;
import net.jandie1505.musicbot.console.Console;
import net.jandie1505.musicbot.console.commands.DatabaseCommand;
import net.jandie1505.musicbot.console.commands.GuildCommand;
import net.jandie1505.musicbot.console.commands.ShardsCommand;
import net.jandie1505.musicbot.console.commands.StopCommand;
import net.jandie1505.musicbot.database.DatabaseManager;
import net.jandie1505.musicbot.eventlisteners.EventsBasic;
import net.jandie1505.musicbot.eventlisteners.EventsButtons;
import net.jandie1505.musicbot.eventlisteners.EventsCommands;
import net.jandie1505.musicbot.slashcommands.BotOwnerPermissionRequest;
import net.jandie1505.musicbot.slashcommands.UserPermissionRequest;
import net.jandie1505.musicbot.system.GMS;
import net.jandie1505.musicbot.system.MusicManager;
import net.jandie1505.musicbot.utilities.BotStatus;
import net.jandie1505.musicbot.utilities.Messages;
import net.jandie1505.slashcommandapi.SlashCommandHandler;
import net.jandie1505.slashcommandapi.command.SlashCommandBuilder;
import net.jandie1505.slashcommandapi.utilities.DefaultPermissionRequests;
import net.jandie1505.slashcommandapi.utilities.DefaultSlashCommandExecutors;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicBot {
    //STATIC
    public static final Terminal TERMINAL;
    public static final LineReader LINE_READER;
    public static final Logger LOGGER;
    private static MusicBot instance;

    static {
        try {
            TERMINAL = TerminalBuilder.builder()
                    .system(true)
                    .build();
            LINE_READER = LineReaderBuilder.builder()
                    .terminal(TERMINAL)
                    .build();
            LOGGER = LoggerFactory.getLogger(MusicBot.class);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    // INSTANCE
    private final Console console;
    private final ConfigManager configManager;
    private final ScheduledExecutorService executorService;
    private final DatabaseManager databaseManager;
    private final GMS gms;
    private final MusicManager musicManager;
    private ShardManager shardManager;

    public MusicBot(String token, int shardsCount, boolean disableShardsCheck, boolean ignoreConfigFile, boolean disableAutoStart) throws LoginException, SQLException, IOException, ClassNotFoundException {

        // CONSOLE

        this.console = new Console(this);
        this.console.registerCommand("stop", new StopCommand(this));
        this.console.registerCommand("shards", new ShardsCommand(this));
        this.console.registerCommand("database", new DatabaseCommand(this));
        this.console.registerCommand("guild", new GuildCommand(this));
        this.console.start();
        MusicBot.LOGGER.debug("Console initialization completed");

        // CONFIG

        this.configManager = new ConfigManager(this);
        this.configManager.getConfig().setDisableShardsCheck(disableShardsCheck);

        if(!ignoreConfigFile) {
            try {
                File configFile = new File(System.getProperty("user.dir"), "config.json");

                if(!configFile.exists()) {
                    configFile.createNewFile();
                    this.configManager.saveConfig(configFile);
                }

                this.configManager.loadConfig(configFile);
            } catch (IOException | JSONException e) {

            }
        }
        MusicBot.LOGGER.debug("ConfigManager initialization completed");

        // OVERRIDE CONFIG VALUES

        if (token != null && !token.equalsIgnoreCase("")) {
            this.configManager.getConfig().setToken(token);
            MusicBot.LOGGER.info("Config value token overridden by start argument");
        }

        if (shardsCount > 0) {
            this.configManager.getConfig().setShardsCount(shardsCount);
            MusicBot.LOGGER.info("Config value shardsCount overridden by start argument");
        }

        // EXECUTOR SERVICE

        this.executorService = new ScheduledThreadPoolExecutor(1);

        // DATABASE

        this.databaseManager = new DatabaseManager(this);

        this.executorService.schedule(() -> {

            this.databaseManager.cleanupGuilds();
            this.databaseManager.cleanupMusicBlacklist();

        }, 1, TimeUnit.MINUTES);

        MusicBot.LOGGER.debug("DatabaseManager initialization completed");

        // BOT

        if (!disableAutoStart) {
            this.startShardManager();
        }

        this.executorService.schedule(() -> {

            if (this.shardManager == null) {
                return;
            }

            if (this.shardManager.getShardsRunning() >= this.shardManager.getShardsTotal()) {
                return;
            }

            if (disableAutoStart) {
                MusicBot.LOGGER.warn("Not all shards are online. Auto-restarting is disabled.");
                return;
            }

            this.startShards();
            MusicBot.LOGGER.warn("Not all shards are online. Restarting...");

        }, 1, TimeUnit.MINUTES);

        // GMS

        this.gms = new GMS(this);

        // MUSIC MANAGER

        this.musicManager = new MusicManager(this);

        this.executorService.schedule(this.musicManager::reload, 1, TimeUnit.MINUTES);

    }

    public void shutdown() {

        MusicBot.LOGGER.info("Shutting down...");

        try {

            if (this.shardManager != null) {
                this.shardManager.shutdown();
            }

        } catch (Exception e) {
            MusicBot.LOGGER.error("Exception while shutting down", e);
        }

        this.executorService.shutdownNow();

        this.console.stop();

        Thread shutdownThread = new Thread(() -> {
            try {
                int timer = 10;
                while (timer > 0) {
                    TimeUnit.SECONDS.sleep(1);
                    timer--;
                }
                MusicBot.LOGGER.warn("Enforcing JVM exit");
                System.exit(0);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        });
        shutdownThread.setName("MUSICBOT-SHUTDOWN-THREAD");
        shutdownThread.setDaemon(true);
        shutdownThread.start();

    }

    // BOT MANAGER

    public BotStatus getBotStatus() {

        if (this.shardManager == null) {
            return BotStatus.NOT_AVAILABLE;
        }

        try {
            Field shutdownField = DefaultShardManager.class.getDeclaredField("shutdown");
            shutdownField.setAccessible(true);
            AtomicBoolean shutdown = (AtomicBoolean) shutdownField.get(this.shardManager);

            Field executorField = DefaultShardManager.class.getDeclaredField("executor");
            executorField.setAccessible(true);
            ScheduledExecutorService executor = (ScheduledExecutorService) executorField.get(this.shardManager);

            if (!shutdown.get() && !executor.isTerminated()) {
                return BotStatus.ACTIVE;
            } else if (shutdown.get() && !executor.isTerminated()) {
                return BotStatus.SHUTDOWN_REQUESTED;
            } else {
                return BotStatus.SHUTDOWN;
            }

        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            e.printStackTrace();
            return BotStatus.ERROR;
        }
    }

    public void shutdownShardManager() {

        if (this.shardManager == null) {
            return;
        }

        this.shardManager.shutdown();
        MusicBot.LOGGER.info("Shut down ShardManager");
    }

    public void startShardManager(int shardsTotal) {

        if (this.shardManager != null) {
            return;
        }

        if (shardsTotal < 1) {
            return;
        }

        this.shardManager = DefaultShardManagerBuilder.createDefault(this.configManager.getConfig().getToken())
                .setShardsTotal(shardsTotal)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .build();
        this.shardManager.setPresence(OnlineStatus.IDLE, Activity.playing("Starting up..."));
        this.shardManager.addEventListener(new EventsBasic(this));
        this.shardManager.addEventListener(new EventsCommands(this));
        this.shardManager.addEventListener(new EventsButtons(this));

        MusicBot.LOGGER.info("Started ShardManager");
    }

    public void startShardManager() {
        this.startShardManager(this.configManager.getConfig().getShardsCount());
    }

    // SHARD MANAGER

    public void startShard(int shardId) {

        if (this.getBotStatus() != BotStatus.ACTIVE) {
            return;
        }

        if (this.shardManager.getShardById(shardId) != null) {
            return;
        }

        this.shardManager.start(shardId);
        this.shardManagerInfo("Started shard " + shardId);
    }

    public void stopShard(int shardId) {

        if (this.getBotStatus() != BotStatus.ACTIVE) {
            return;
        }

        if (this.shardManager.getShardById(shardId) == null) {
            return;
        }

        this.shardManager.shutdown(shardId);
        this.shardManagerInfo("Stopped shard " + shardId);
    }

    public void restartShard(int shardId) {

        if (this.getBotStatus() != BotStatus.ACTIVE) {
            return;
        }

        if (this.shardManager.getShardById(shardId) == null) {
            return;
        }

        this.shardManager.restart(shardId);
        this.shardManagerInfo("Restarted shard " + shardId);
    }

    public void startShards() {

        if (this.getBotStatus() != BotStatus.ACTIVE) {
            return;
        }

        for(int i = 0; i < this.shardManager.getShardsTotal(); i++) {
            this.startShard(i);
        }
    }

    public void stopShards() {

        if (this.getBotStatus() != BotStatus.ACTIVE) {
            return;
        }

        for(int i = 0; i < this.shardManager.getShardsTotal(); i++) {
            this.stopShard(i);
        }
    }

    public void restartShards() {

        if (this.getBotStatus() != BotStatus.ACTIVE) {
            return;
        }

        for(int i = 0; i < this.shardManager.getShardsTotal(); i++) {
            this.restartShard(i);
        }
    }

    public void reloadShards() {
        new Thread(() -> {
            if(shardManager.getShardsRunning() < shardManager.getShardsTotal()) {
                if(!this.getConfigManager().getConfig().isDisableShardsCheck()) {
                    this.shardManagerInfo("Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online. Auto restarting...");
                    startShards();
                } else {
                    this.shardManagerWarning("Only " + shardManager.getShardsRunning() + " of " + shardManager.getShardsTotal() + " are online");
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

        if (this.getBotStatus() != BotStatus.ACTIVE) {
            return false;
        }

        boolean status = true;
        for(JDA jda : shardManager.getStatuses().keySet()) {
            if(jda.getStatus() != JDA.Status.CONNECTED) {
                status = false;
            }
        }
        return (shardManager.getShardsRunning() == shardManager.getShardsTotal()) && status;
    }

    public SlashCommandHandler upsertCommands() {
        SlashCommandHandler slashCommandHandler = new SlashCommandHandler();

        slashCommandHandler.registerSlashCommand(
                "cmd",
                new SlashCommandBuilder()
                        .executes(interaction -> {
                            interaction.deferReply().queue();
                            interaction.getHook().sendMessage(this.console.runCommand(interaction.getOption("cmd").getAsString()));
                            // Logger is planned
                        })
                        .executesMissingOptions(DefaultSlashCommandExecutors.missingOptionsExecutor())
                        .withPermissionRequest(new BotOwnerPermissionRequest(this))
                        .requireOption("cmd", OptionType.STRING)
                        .build()
        );

        slashCommandHandler.registerSlashCommand(
                "help",
                new SlashCommandBuilder()
                        .executes(interaction -> {
                            interaction.reply(Messages.getHelpMessage().build()).queue();
                        })
                        .withPermissionRequest(DefaultPermissionRequests.publicCommand())
                        .build()
        );

        slashCommandHandler.registerSlashCommand(
                "play",
                new SlashCommandBuilder()
                        .executes(interaction -> {

                        })
                        .executesNoPermission(DefaultSlashCommandExecutors.noPermissionExecutor())
                        .withPermissionRequest(new UserPermissionRequest(this))
                        .requireGuild(true)
                        .build()
        );

        return slashCommandHandler;
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
                MusicBot.LOGGER.info("Registered command cmd");
            }
            if(!cmdNameList.contains("invite") || reloadall) {
                CommandData inviteCommand = new CommandDataImpl("invite", "Invite MusicBot to your server");
                jda.upsertCommand(inviteCommand).queue();
                MusicBot.LOGGER.info("Registered command invite");
            }
            if(!cmdNameList.contains("help") || reloadall) {
                CommandData commandData = new CommandDataImpl("help", "Get help and information about MusicBot");
                jda.upsertCommand(commandData).queue();
                MusicBot.LOGGER.info("Registered command help");
            }
            if(!cmdNameList.contains("nowplaying") || reloadall) {
                CommandData nowplayingCommand = new CommandDataImpl("nowplaying", "Shows information about the song which is currently playing");
                jda.upsertCommand(nowplayingCommand).queue();
                MusicBot.LOGGER.info("Registered command nowplaying");
            }
            if(!cmdNameList.contains("queue") || reloadall) {
                CommandData queueCommand = new CommandDataImpl("queue", "Shows the current queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "index", "Search for queue indexes"));
                jda.upsertCommand(queueCommand).queue();
                MusicBot.LOGGER.info("Registered command queue");
            }
            if(!cmdNameList.contains("play") || reloadall) {
                CommandData playCommand = new CommandDataImpl("play", "Play a song")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song link / song name / playlist link you want to play"));
                jda.upsertCommand(playCommand).queue();
                MusicBot.LOGGER.info("Registered command play");
            }
            if(!cmdNameList.contains("add") || reloadall) {
                CommandData addCommand = new CommandDataImpl("add", "Add a song to queue without starting to play")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song link / song name / playlist link you want to add").setRequired(true));
                jda.upsertCommand(addCommand).queue();
                MusicBot.LOGGER.info("Registered command add");
            }
            if(!cmdNameList.contains("pause") || reloadall) {
                CommandData pauseCommand = new CommandDataImpl("pause", "Stop playing music");
                jda.upsertCommand(pauseCommand).queue();
                MusicBot.LOGGER.info("Registered command pause");
            }
            if(!cmdNameList.contains("remove") || reloadall) {
                CommandData removeCommand = new CommandDataImpl("remove", "Remove a specific song from the queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "index", "The index of the song you want to remove").setRequired(true));
                jda.upsertCommand(removeCommand).queue();
                MusicBot.LOGGER.info("Registered command remove");
            }
            if(!cmdNameList.contains("clear") || reloadall) {
                CommandData clearCommand = new CommandDataImpl("clear", "Clear the queue");
                jda.upsertCommand(clearCommand).queue();
                MusicBot.LOGGER.info("Registered command clear");
            }
            if(!cmdNameList.contains("search") || reloadall) {
                CommandData searchCommand = new CommandDataImpl("search", "YTSearchHandler youtube")
                        .addOptions(new OptionData(OptionType.STRING, "query", "The text you want to search for").setRequired(true));
                jda.upsertCommand(searchCommand).queue();
                MusicBot.LOGGER.info("Registered command search");
            }
            if(!cmdNameList.contains("shuffle") || reloadall) {
                CommandData shuffleCommand = new CommandDataImpl("shuffle", "Shuffle the queue");
                jda.upsertCommand(shuffleCommand).queue();
                MusicBot.LOGGER.info("Registered command shuffle");
            }
            if(!cmdNameList.contains("skip") || reloadall) {
                CommandData skipCommand = new CommandDataImpl("skip", "Skip a song")
                        .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                jda.upsertCommand(skipCommand).queue();
                MusicBot.LOGGER.info("Registered command skip");
            }
            if(!cmdNameList.contains("removeuser") || reloadall) {
                CommandData removeUserCommand = new CommandDataImpl("removeuser", "Removes all songs by a specific member")
                        .addOptions(new OptionData(OptionType.USER, "member", "The member you want to remove the music from").setRequired(true));
                jda.upsertCommand(removeUserCommand).queue();
                MusicBot.LOGGER.info("Registered command removeuser");
            }
            if(!cmdNameList.contains("forceskip") || reloadall) {
                CommandData forceskipCommand = new CommandDataImpl("forceskip", "Force skip a song")
                        .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                jda.upsertCommand(forceskipCommand).queue();
                MusicBot.LOGGER.info("Registered command forceskip");
            }
            if(!cmdNameList.contains("movetrack") || reloadall) {
                CommandData movetrackCommand = new CommandDataImpl("movetrack", "Move a specific track in queue")
                        .addOptions(new OptionData(OptionType.INTEGER, "from", "The track you want to move").setRequired(true))
                        .addOptions(new OptionData(OptionType.INTEGER, "to", "The queue position you want to move the track to").setRequired(true));
                jda.upsertCommand(movetrackCommand).queue();
                MusicBot.LOGGER.info("Registered command movetrack");
            }
            if(!cmdNameList.contains("playnow") || reloadall) {
                CommandData playnowCommand = new CommandDataImpl("playnow", "Stop the current song and play the specified song immediately")
                        .addOptions(new OptionData(OptionType.STRING, "song", "The song you want to play").setRequired(true));
                jda.upsertCommand(playnowCommand).queue();
                MusicBot.LOGGER.info("Registered command playnow");
            }
            if(!cmdNameList.contains("stop") || reloadall) {
                CommandData stopCommand = new CommandDataImpl("stop", "Stop playing music");
                jda.upsertCommand(stopCommand).queue();
                MusicBot.LOGGER.info("Registered command stop");
            }
            if(!cmdNameList.contains("volume") || reloadall) {
                CommandData volumeCommand = new CommandDataImpl("volume", "Change the volume")
                        .addOptions(new OptionData(OptionType.INTEGER, "volume", "Change the volume to this value"));
                jda.upsertCommand(volumeCommand).queue();
                MusicBot.LOGGER.info("Registered command volume");
            }
            if(!cmdNameList.contains("leave") || reloadall) {
                CommandData leaveCommand = new CommandDataImpl("leave", "Leave the voice channel");
                jda.upsertCommand(leaveCommand).queue();
                MusicBot.LOGGER.info("Registered command leave");
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
                MusicBot.LOGGER.info("Registered command mbsettings");
            }

            MusicBot.LOGGER.info("Command setup completed (reloadall=" + reloadall + ")");
        });
    }

    // GETTER METHODS
    public Console getConsole() {
        return this.console;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public GMS getGMS() {
        return this.gms;
    }

    public MusicManager getMusicManager() {
        return this.musicManager;
    }

    // OPERATIONAL STATUS

    public boolean isOperational() {
        return true;
    }

    // LOGS

    private void shardManagerInfo(String message) {
        MusicBot.LOGGER.info(message);
    }

    private void shardManagerWarning(String message) {
        MusicBot.LOGGER.warn(message);
    }

    // STATIC

    public static void main(String[] args) {
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

        boolean showLauncherStackTrace = false;
        showLauncherStackTrace = Boolean.parseBoolean(startArguments.get("showLauncherStackTrace"));
        if(showLauncherStackTrace) {
            System.out.println("showLauncherStackTrace option enabled");
        }

        boolean defaultConfigValues = false;
        defaultConfigValues = Boolean.parseBoolean(startArguments.get("defaultConfigValues"));
        if (defaultConfigValues) {
            System.out.println("defaultConfigValues option enabled");
        }

        String overrideToken = null;
        overrideToken = startArguments.get("overrideToken");
        if(overrideToken != null) {
            System.out.println("Bot token specified via overrideToken option");
        }

        int overrideShardsCount = -1;
        try {
            overrideShardsCount = Integer.parseInt(startArguments.get("overrideShardsCount"));
        } catch (IllegalArgumentException ignored) {
            // NOT REQUIRED
        }

        boolean disableShardsCheck = false;
        disableShardsCheck = Boolean.parseBoolean(startArguments.get("disableShardsCheck"));
        if(disableShardsCheck) {
            System.out.println("" +
                    "-------------------- WARNING --------------------\n" +
                    "Start option disableShardsCheck is enabled.\n" +
                    "This prevents the bot from automatically restarting stopped shards.\n" +
                    "If not all shards are online, important checks like the guild cleanup are disabled.\n" +
                    "Don't use the disableShardCheck option for a long time.\n" +
                    "-------------------------------------------------");

            waitTime = 10;
        }

        boolean disableAutostart = Boolean.parseBoolean(startArguments.get("disableAutostart"));

        System.out.println("Starting bot in " + waitTime + " seconds...");
        try {
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (Exception ignored) {}

        try {
            new MusicBot(overrideToken, overrideShardsCount, disableShardsCheck, defaultConfigValues, disableAutostart);
        } catch (LoginException e) {
            System.err.println("Failed to start the bot: Please check your token (LoginException)");
            if(showLauncherStackTrace) {
                e.printStackTrace();
            }
            System.exit(-1);
        } catch (SQLException e) {
            System.err.println("Failed to start the bot: Database error (SQLException)");
            if(showLauncherStackTrace) {
                e.printStackTrace();
            }
            System.exit(-2);
        } catch (IOException e) {
            System.err.println("Failed to start the bot: Config/Database file error (IOException)");
            if(showLauncherStackTrace) {
                e.printStackTrace();
            }
            System.exit(-3);
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to start the bot: ClassNotFoundException (This error can occur if the database driver was not found)");
            if(showLauncherStackTrace) {
                e.printStackTrace();
            }
            System.exit(-4);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-5);
        }
    }
}
