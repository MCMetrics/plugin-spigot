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

        if (args.length == 1) {
            // Complete first argument
            completions.add("setup");
            completions.add("reload");
            completions.add("uploadall");
            completions.add("experiments");
            completions.add("help");
        } else if (args.length == 2) {
            // Complete second argument
            if (args[0].equals("setup")) {
                completions.add("<user id>");
            }
        } else if (args.length == 3) {
            // Complete third argument
            if (args[0].equals("setup")) {
                completions.add("<server id>");
            }
        }

        return completions;
    }
}
