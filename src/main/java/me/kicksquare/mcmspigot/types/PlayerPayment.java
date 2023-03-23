package me.kicksquare.mcmspigot.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.util.LoggerUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerPayment {

    public String platform;
    public String player_uuid;
    public String transaction_id;
    public String amount;
    public String currency;
    public String package_id;
    public String dateTime;

    public String uid;
    public String server_id;

    @JsonCreator
    public PlayerPayment(MCMSpigot plugin, String platform, String player_uuid, String transaction_id, String amount, String currency, String package_id) {

        this.platform = platform;
        this.player_uuid = player_uuid;
        this.transaction_id = transaction_id;
        this.amount = amount;
        this.currency = currency;
        this.package_id = package_id;
        this.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.uid = plugin.getMainConfig().getString("uid");
        this.server_id = plugin.getMainConfig().getString("server_id");

        LoggerUtil.debug("Server Id: " + server_id + " UID: " + uid);
    }
}
