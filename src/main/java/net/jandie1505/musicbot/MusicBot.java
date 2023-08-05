package net.jandie1505.musicbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.jandie1505.musicbot.config.ConfigManager;
import net.jandie1505.musicbot.console.Console;
import net.jandie1505.musicbot.console.commands.*;
import net.jandie1505.musicbot.database.DatabaseManager;
import net.jandie1505.musicbot.eventlisteners.EventsBasic;
import net.jandie1505.musicbot.eventlisteners.EventsButtons;
import net.jandie1505.musicbot.eventlisteners.EventsCommands;
import net.jandie1505.musicbot.eventlisteners.EventsCommandsOld;
import net.jandie1505.musicbot.music.MusicManager;
import net.jandie1505.musicbot.system.GMS;
import net.jandie1505.musicbot.utilities.BotStatus;
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
        this.console.registerCommand("commands", new CommandsCommand(this));
        this.console.registerCommand("player", new PlayerCommand(this));
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

        this.executorService.schedule(this.gms::setupGuilds, 30, TimeUnit.MINUTES);

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
        this.shardManager.addEventListener(new EventsCommandsOld(this));
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

    // GLOBAL SLASH COMMANDS

    public void deleteCommands() {

        this.shardManager.retrieveApplicationInfo().getJDA().retrieveCommands().queue(commands -> {

            for (Command command : commands) {
                command.delete().queue();
            }

            this.shardManagerInfo("Deleted global bot commands");
        });

    }

    public void upsertCommands() {

        if (this.getBotStatus() != BotStatus.ACTIVE || this.shardManager.retrieveApplicationInfo().getJDA() == null || this.shardManager.retrieveApplicationInfo().getJDA().getStatus() != JDA.Status.CONNECTED) {
            this.shardManagerWarning("Cannot run upsert commands because bot is not online");
            return;
        }

        this.shardManager.retrieveApplicationInfo().getJDA().retrieveCommands().queue(commands -> {
            JDA jda = this.shardManager.retrieveApplicationInfo().getJDA();

            List<String> cmdNameList = new ArrayList<>();

            for(Command cmd : commands) {
                cmdNameList.add(cmd.getName());
            }

            if(!cmdNameList.contains("cmd")) {
                CommandData cmdCommand = new CommandDataImpl("cmd", "cmd")
                        .addOptions(new OptionData(OptionType.STRING, "cmd", "cmd").setRequired(true));
                jda.upsertCommand(cmdCommand).queue();
            }

            if(!cmdNameList.contains("help")) {
                CommandData commandData = new CommandDataImpl("help", "Get help and information about MusicBot");
                jda.upsertCommand(commandData).queue();
            }

            MusicBot.LOGGER.info("Command setup completed");
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
