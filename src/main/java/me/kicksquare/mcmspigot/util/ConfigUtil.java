package me.kicksquare.mcmspigot.util;

import de.leonhard.storage.Config;

public class ConfigUtil {
    public static void setConfigDefaults(Config mainConfig, Config dataConfig, Config bansConfig) {
        mainConfig.setDefault("server_id", "");
        mainConfig.setDefault("uid", "");
        mainConfig.setDefault("enable-bstats", true);
        mainConfig.setDefault("bedrock-prefix", "*");
        mainConfig.setDefault("exempt-players", new String[]{
                "00000000-0000-0000-0000-000000000000",
                "ExamplePlayerUserName"
        });
        mainConfig.setDefault("payment-fee", 0.00);
        mainConfig.setDefault("debug", false);

        dataConfig.setDefault("setup-complete", false);
        dataConfig.setDefault("record-sessions", true);
        dataConfig.setDefault("record-pings", true);
        dataConfig.setDefault("ping-interval", 5);
        dataConfig.setDefault("record-payments", true);
        dataConfig.setDefault("execute-experiments", true);
        dataConfig.setDefault("bulk-session-threshold", 1);
        dataConfig.setDefault("global-bans", false);
        dataConfig.setDefault("dev-mode", false);

        bansConfig.setDefault("enabled", false);
        bansConfig.setDefault("dupe", true);
        bansConfig.setDefault("lag", true);
        bansConfig.setDefault("discrimination", true);
        bansConfig.setDefault("botting", true);
        bansConfig.setDefault("commands", new String[]{
                "ban ${player} You are globally banned for ${reason}!"
        });
        bansConfig.setDefault("discord-webhook-enabled", false);
        bansConfig.setDefault("discord-webhook-url", "");
        bansConfig.setDefault("discord-webhook-title", "Global Bans - Login Blocked");
        bansConfig.setDefault("discord-webhook-description", "${player} was blocked from logging in due to a global ban.");
    }
}
