package me.kicksquare.mcmspigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MCMetricsTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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
            if (args[0].equals("setup")) {
                completions.add("<user id>");
            } else if (args[0].equals("bans")) {
                completions.add("add");
                completions.add("lookup");
            } else if (args[0].equals("testexperiment")) {
                completions.add("<player name>");
            }
        } else if (args.length == 3) {
            // Complete third argument
            if (args[0].equals("setup")) {
                completions.add("<server id>");
            } else if (args[0].equals("bans")) {
                if (args[1].equals("add")) {
                    completions.add("<player name/uuid>");
                } else if (args[1].equals("lookup")) {
                    completions.add("<player name/uuid>");
                }
            } else if (args[0].equals("testexperiment")) {
                completions.add("<experiment name>");
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
