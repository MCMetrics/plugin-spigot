package me.kicksquare.mcmspigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MCMetricsTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // todo better solution instead of nested ifs
        if (args.length == 1) {
            // Complete first argument
            completions.add("setup");
            completions.add("bans");
            completions.add("reload");
            completions.add("uploadall");
            completions.add("experiments");
            completions.add("testexperiment");
            completions.add("help");
        } else if (args.length == 2) {
            // Complete second argument
            switch (args[0]) {
                case "setup":
                    completions.add("<user id>");
                    break;
                case "bans":
                    completions.add("add");
                    completions.add("lookup");
                    break;
                case "testexperiment":
                    completions.add("<player name>");
                    break;
            }
        } else if (args.length == 3) {
            // Complete third argument
            switch (args[0]) {
                case "setup":
                    completions.add("<server id>");
                    break;
                case "bans":
                    if (args[1].equals("add")) {
                        completions.add("<player name/uuid>");
                    } else if (args[1].equals("lookup")) {
                        completions.add("<player name/uuid>");
                    }
                    break;
                case "testexperiment":
                    completions.add("<experiment name>");
                    break;
            }
        } else if (args.length == 4) {
            if (args[0].equals("bans") && args[1].equals("add")) {
                completions.add("<reason>");
            } else if (args[0].equals("testexperiment")) {
                completions.add("<variant>");
            }
        } else if (args.length == 5) {
            if (args[0].equals("bans") && args[1].equals("add")) {
                completions.add("<evidence>");
            }
        }

        return completions;
    }
}
