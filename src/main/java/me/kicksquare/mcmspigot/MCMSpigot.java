package me.kicksquare.mcmspigot;

import io.sentry.Sentry;
import me.kicksquare.mcmspigot.commands.ExperimentCommand;
import me.kicksquare.mcmspigot.commands.MCMCommand;
import me.kicksquare.mcmspigot.commands.MCMetricsTabCompleter;
import me.kicksquare.mcmspigot.commands.PaymentCommand;
import me.kicksquare.mcmspigot.listeners.ExperimentListener;
import me.kicksquare.mcmspigot.listeners.PlayerSessionListener;
import me.kicksquare.mcmspigot.papi.PapiExtension;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

@SuppressWarnings("DataFlowIssue")
public final class MCMSpigot extends JavaPlugin {

    private static MCMSpigot plugin; // used in ExperimentUtil
    private SessionQueue sessionQueue = new SessionQueue();
    private ArrayList<Experiment> experiments = new ArrayList<>();

    public static MCMSpigot getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // standard command (reload, setup, etc)
        getCommand("mcmetrics").setExecutor(new MCMCommand(this));
        getCommand("mcmetrics").setTabCompleter(new MCMetricsTabCompleter());
        getCommand("/mcmetrics").setExecutor(new MCMCommand(this));

        Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ExperimentListener(this), this);
        getCommand("mcmexperiment").setExecutor(new ExperimentCommand(this));
        getCommand("mcmpayment").setExecutor(new PaymentCommand(this));

        MCMCommand.reloadConfigAndFetchData();

        // upload player count every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            // need to do this check in case of a config reload without reload
            if (!SetupUtil.shouldRecordPings()) return;

            try {
                System.out.println("uploading player count");
                final String bodyString = "{\"playercount\": \"" + Bukkit.getOnlinePlayers().size() + "\"}";
                HttpUtil.makeAsyncPostRequest("https://dashboard.mcmetrics.net/api/pings/insertPing", bodyString, HttpUtil.getAuthHeadersFromConfig());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 20 * 60 * 5);

        // enable bstats
        if (getConfig().getBoolean("enable-bstats")) {
            new Metrics(this, 17450);
        }

        // enable papi
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiExtension(this).register();
        }

        // enable sentry error reporting
        if (getConfig().getBoolean("enable-sentry")) {
            Sentry.init(options -> {
                options.setDsn("https://b157b0cab7ba42cd92c83a583e57af66@o4504532201046017.ingest.sentry.io/4504540638347264");
                //todo We recommend adjusting this value in production.
                options.setTracesSampleRate(1.0);
                options.setDebug(false);
            });

            // checks for exceptions matching this plugin name and uploads them to sentry
            Thread.setDefaultUncaughtExceptionHandler(new SentryExceptionHandler());
        }
    }

    public SessionQueue getSessionQueue() {
        return sessionQueue;
    }

    public ArrayList<Experiment> getExperiments() {
        return experiments;
    }
}