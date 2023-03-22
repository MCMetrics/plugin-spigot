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
    public int pingInterval;
    @JsonProperty("recordPayments")
    public boolean recordPayments;
    @JsonProperty("executeExperiments")
    public boolean executeExperiments;
    @JsonProperty("bulkSessionThreshold")
    public int bulkSessionThreshold;

    public TaskList(@JsonProperty("recordSessions") boolean recordSessions, @JsonProperty("recordPings") boolean recordPings, @JsonProperty("pingInterval") int pingInterval, @JsonProperty("recordPayments") boolean recordPayments, @JsonProperty("executeExperiments") boolean executeExperiments, @JsonProperty("bulkSessionThreshold") int bulkSessionThreshold) {
        this.recordSessions = recordSessions;
        this.recordPings = recordPings;
        this.pingInterval = pingInterval;
        this.recordPayments = recordPayments;
        this.executeExperiments = executeExperiments;
        this.bulkSessionThreshold = bulkSessionThreshold;
    }

    public static void fetchTasks() {
        // fetch tasks for this server and save them to memory and config
        HttpUtil.makeAsyncGetRequest("api/server/getServerTasks", HttpUtil.getAuthHeadersFromConfig()).thenAccept(response -> {
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
                    // if the message contains "Invalid user or server id", don't spam the console and just send one custom error
                    if (response.contains("Invalid user or server id")) {
                        System.out.println("MCMetrics: Error occurred while fetching task list: Invalid user or server id");
                        return;
                    }
                    exception.printStackTrace();
                }
            }
        });
    }

}
