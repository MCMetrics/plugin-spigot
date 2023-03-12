package me.kicksquare.mcmspigot.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.util.http.HttpUtil;

public class TaskList {
    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    @JsonProperty("recordSessions")
    public boolean recordSessions;
    @JsonProperty("recordPings")
    public boolean recordPings;
    @JsonProperty("pingInterval")
    public boolean pingInterval;
    @JsonProperty("recordPayments")
    public boolean recordPayments;
    @JsonProperty("executeExperiments")
    public boolean executeExperiments;
    @JsonProperty("bulkSessionThreshold")
    public boolean bulkSessionThreshold;

    public TaskList(@JsonProperty("recordSessions") boolean recordSessions, @JsonProperty("recordPings") boolean recordPings, @JsonProperty("pingInterval") boolean pingInterval, @JsonProperty("recordPayments") boolean recordPayments, @JsonProperty("executeExperiments") boolean executeExperiments, @JsonProperty("bulkSessionThreshold") boolean bulkSessionThreshold) {
        this.recordSessions = recordSessions;
        this.recordPings = recordPings;
        this.pingInterval = pingInterval;
        this.recordPayments = recordPayments;
        this.executeExperiments = executeExperiments;
        this.bulkSessionThreshold = bulkSessionThreshold;
    }

    public static void fetchTasks() {
        // fetch tasks for this server and save them to memory and config
        HttpUtil.makeAsyncGetRequest("https://dashboard.mcmetrics.net/api/server/getServerTasks", HttpUtil.getAuthHeadersFromConfig()).thenAccept(response -> {
            if (response != null) {
                if (response.contains("ERROR_")) {
                    plugin.getLogger().severe("Failed to fetch tasks from server. Response: " + response);
                    return;
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();

                    TaskList tasks = mapper.readValue(response, TaskList.class);
                    plugin.getDataConfig().set("record-sessions", tasks.recordSessions);
                    plugin.getDataConfig().set("record-pings", tasks.recordPings);
                    plugin.getDataConfig().set("ping-interval", tasks.pingInterval);
                    plugin.getDataConfig().set("record-payments", tasks.recordPayments);
                    plugin.getDataConfig().set("execute-experiments", tasks.executeExperiments);
                    plugin.getDataConfig().set("bulk-session-threshold", tasks.bulkSessionThreshold);
                } catch (JsonProcessingException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

}
