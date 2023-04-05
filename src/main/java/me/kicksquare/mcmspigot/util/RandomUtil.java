package me.kicksquare.mcmspigot.util;

import java.util.ArrayList;
import java.util.Random;

public class RandomUtil {

    private static final Random random = new Random();


    /**
     * @param probabilities - array of probabilities
     * @return a random element from an array weighted by the probability of each element
     */
    public static int randomWeightedElement(ArrayList<Double> probabilities) {
        double randomValue = random.nextDouble();
        double total = 0;
        for (int i = 0; i < probabilities.size(); i++) {
            total += probabilities.get(i);
            if (randomValue <= total) {
                return i;
            }
        }
        throw new IllegalArgumentException("Probabilities do not add up to 1.");
    }

    private static final ArrayList<String> combinations = new ArrayList<>(generateCombinations());


    /**
     * Generates a random number between 0.0 and 1.0 inclusive based on the given UUID.
     * The output is random, but consistent for the same UUID.
     *
     * @param uuid The UUID of the player
     * @return A random double between 0.0 and 1.0 inclusive based on the provided UUID
     */
    public static double getProbabilityFromUuid(String uuid) {
        String firstTwoChars = uuid.substring(0, 2);
        // the line above is problematic because bedrock player uuids always start with 0s
        // if firstTwoChars equals 00, we'll use the last two characters instead
        // we're not using the last two strings for everyone for compatibility with existing experiments
        if (firstTwoChars.equals("00")) {
            firstTwoChars = uuid.substring(uuid.length() - 2);
        }

        return ((double) combinations.indexOf(firstTwoChars) % 100) / 100;
    }

    private static ArrayList<String> generateCombinations() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        ArrayList<String> combinations = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                String combination = alphabet.charAt(i) + "" + alphabet.charAt(j);
                combinations.add(combination);
            }
        }
        return combinations;
    }

}
