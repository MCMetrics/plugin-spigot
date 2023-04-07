package me.kicksquare.mcmspigot.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.PlayerPayment;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentTrigger;
import me.kicksquare.mcmspigot.util.ExperimentUtil;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PaymentCommand implements CommandExecutor {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final MCMSpigot plugin;

    public PaymentCommand(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage("This command can only be run from the console.");
            return true;
        }

        // mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>
        if (args.length != 6) {
            sender.sendMessage("Usage: /mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>");
            return true;
        }

        if (!SetupUtil.shouldRecordPayments()) {
            sender.sendMessage("Payments are disabled or not configured.");
            return true;
        }

        final String platform = args[0];
        final String player_uuid = args[1];
        final String transaction_id = args[2];
        String amount = args[3];
        final String currency = args[4];
        final String package_id = args[5];

        // transaction fee option from config
        double amountDouble = Double.parseDouble(amount);
        final double paymentFeeOption = plugin.getMainConfig().getDouble("payment-fee");
        if (paymentFeeOption > 0) {
            amountDouble = amountDouble * (1 - paymentFeeOption);
            amount = String.valueOf(amountDouble);
        }

        if (!platform.equalsIgnoreCase("tebex") && !platform.equalsIgnoreCase("craftingstore")) {
            sender.sendMessage("Invalid platform. Must be either 'tebex' or 'craftingstore'.");
            return true;
        }

        PlayerPayment playerPayment = new PlayerPayment(plugin, platform, player_uuid, transaction_id, amount, currency, package_id);

        // get the payment as a json string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(playerPayment);
        } catch (JsonProcessingException ex) {
            LoggerUtil.severe("Error converting incoming payment to json string.");
            throw new RuntimeException(ex);
        }

        LoggerUtil.debug("Uploading payment session now... " + jsonString);

        HttpUtil.makeAsyncPostRequest("api/payments/insertPayment", jsonString, HttpUtil.getAuthHeadersFromConfig());

        // payments trigger for ab tests
        Player p = Bukkit.getPlayer(player_uuid);
        if (p != null) {
            ArrayList<Experiment> experiments = plugin.getExperiments();
            for (Experiment experiment : experiments) {
                if (experiment.trigger == ExperimentTrigger.PURCHASE) {
                    ExperimentUtil.executeActions(p, experiment);
                }
            }
        }

        return true;
    }
}