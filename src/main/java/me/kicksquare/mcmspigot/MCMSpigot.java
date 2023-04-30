package me.kicksquare.mcmspigot;

import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import io.sentry.Sentry;
import me.kicksquare.mcmspigot.commands.ExperimentCommand;
import me.kicksquare.mcmspigot.commands.MCMCommand;
import me.kicksquare.mcmspigot.commands.MCMetricsTabCompleter;
import me.kicksquare.mcmspigot.commands.PaymentCommand;
import me.kicksquare.mcmspigot.listeners.ExperimentListener;
import me.kicksquare.mcmspigot.listeners.GlobalBansListener;
import me.kicksquare.mcmspigot.listeners.PlayerSessionListener;
import me.kicksquare.mcmspigot.papi.PapiExtension;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.UploadQueue;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("DataFlowIssue")
public final class MCMSpigot extends JavaPlugin {

    private static MCMSpigot plugin; // used in ExperimentUtil
    private Config mainConfig;
    private Config dataConfig;
    private Config bansConfig;

    private SessionQueue sessionQueue;
    private UploadQueue uploadQueue;

    private final ArrayList<Experiment> experiments = new ArrayList<>();

    public static MCMSpigot getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        sessionQueue = new SessionQueue();
        uploadQueue = new UploadQueue(this);

        mainConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "config.yml"))
                .addInputStreamFromResource("config.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        dataConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "data/data.yml"))
                .addInputStreamFromResource("data.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        bansConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "globalbans.yml"))
                .addInputStreamFromResource("globalbans.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();


        // standard command (reload, setup, etc)
        getCommand("mcmetrics").setExecutor(new MCMCommand(this));
        getCommand("mcmetrics").setTabCompleter(new MCMetricsTabCompleter());
        getCommand("/mcmetrics").setExecutor(new MCMCommand(this));

        Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ExperimentListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GlobalBansListener(this), this);
        getCommand("mcmexperiment").setExecutor(new ExperimentCommand(this));
        getCommand("mcmpayment").setExecutor(new PaymentCommand(this));

        MCMCommand.reloadConfigAndFetchData();

        // upload player count every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            uploadPlayerCount();
        }, 0, 20L * 60 * dataConfig.getInt("ping-interval"));

        // enable bstats
        if (mainConfig.getBoolean("enable-bstats")) {
            new Metrics(this, 17450);
        }

        // enable papi
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiExtension(this).register();
        }

        // enable sentry error reporting
        if (mainConfig.getBoolean("enable-sentry")) {
            Sentry.init(options -> {
                options.setDsn("https://b157b0cab7ba42cd92c83a583e57af66@o4504532201046017.ingest.sentry.io/4504540638347264");
                options.setTracesSampleRate(0.1);
                options.setDebug(false);
            });

            // checks for exceptions matching this plugin name and uploads them to sentry
            Thread.setDefaultUncaughtExceptionHandler(new SentryExceptionHandler());
        }
    }

    public void uploadPlayerCount() {
        // need to do this check in case of a config reload without reload
        if (!SetupUtil.shouldRecordPings()) return;
        if (dataConfig.getInt("ping-interval") == 0) return;

        try {
            LoggerUtil.debug("uploading player count");
            final String bodyString = "{\"playercount\": \"" + Bukkit.getOnlinePlayers().size() + "\"}";
            HttpUtil.makeAsyncPostRequest("api/pings/insertPing", bodyString, HttpUtil.getAuthHeadersFromConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SessionQueue getSessionQueue() {
        return sessionQueue;
    }

    public UploadQueue getUploadQueue() {
        return uploadQueue;
    }

    public ArrayList<Experiment> getExperiments() {
        return experiments;
    }

    public Config getMainConfig() {
        return mainConfig;
    }

    public Config getDataConfig() {
        return dataConfig;
    }

    public Config getBansConfig() {
        return bansConfig;
    }

    @Override
    public void onDisable() {
        // upload all sessions in queue on server shutdown
        LoggerUtil.debug("Disabling, uploading all sessions remaining in queue...");

        sessionQueue.endAndUploadAllSessions();
    }
}