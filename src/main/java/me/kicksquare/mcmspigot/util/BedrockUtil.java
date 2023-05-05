package me.kicksquare.mcmspigot.util;

public class BedrockUtil {
    public static boolean isBedrockUuid(String uuid) {
        // all bedrock uuids start with 00000000-0000-0000
        return uuid.startsWith("00000000-0000-0000");
    }
}
