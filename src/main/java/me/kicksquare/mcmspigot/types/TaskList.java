package me.kicksquare.mcmspigot.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.util.http.HttpUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TaskList {
    private static MCMSpigot plugin = MCMSpigot.getPlugin();

    @JsonProperty("recordSessions")
    public boolean recordSessions;
    @JsonProperty("recordPings")
    public boolean recordPings;
    @JsonProperty("recordPayments")
    public boolean recordPayments;
    @JsonProperty("executeExperiments")
    public boolean executeExperiments;

    public TaskList(@JsonProperty("recordSessions") boolean recordSessions, @JsonProperty("recordPings") boolean recordPings, @JsonProperty("recordPayments") boolean recordPayments, @JsonProperty("executeExperiments") boolean executeExperiments) {
        this.recordSessions = recordSessions;
        this.recordPings = recordPings;
        this.recordPayments = recordPayments;
        this.executeExperiments = executeExperiments;
    }

    public static void fetchTasks() {
        // fetch tasks for this server and save them to memory and config
        HttpUtil.makeAsyncGetRequest("http://localhost:3000/api/server/getServerTasks", HttpUtil.getAuthHeadersFromConfig()).thenAccept(response -> {
            if (response != null) {
                if (response.contains("ERROR_")) {
                    plugin.getLogger().severe("Failed to fetch tasks from server. Response: " + response);
                    return;
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();

                    TaskList tasks = mapper.readValue(response, TaskList.class);
                    plugin.getConfig().set("record-sessions", tasks.recordSessions);
                    plugin.getConfig().set("record-pings", tasks.recordPings);
                    plugin.getConfig().set("record-payments", tasks.recordPayments);
                    plugin.getConfig().set("execute-experiments", tasks.executeExperiments);
                    plugin.saveConfig();
                } catch (JsonProcessingException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

}
