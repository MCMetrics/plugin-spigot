package me.kicksquare.mcmspigot.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringFormatterUtil {
    private static String formatNumberWithCommas(String input) {
        String[] parts = input.split("\\.");
        parts[0] = parts[0].replaceAll("(?<=\\d)(?=(\\d{3})+$)", ",");
        return String.join(".", parts);
    }

    public static String formatLargeNumber(Number input) {
        if (input == null) {
            return "N/A";
        }

        double num = input.doubleValue();

        if (num % 1 == 0) {
            return formatNumberWithCommas(Integer.toString((int) num));
        } else {
            return formatNumberWithCommas(String.format("%.2f", num));
        }
    }

    public static String formatMoney(Number value) {
        if (value == null) {
            return "N/A";
        }

        double num = value.doubleValue();
        String toString = String.format("$%.2f", num);
        return formatNumberWithCommas(toString);
    }

    public static String formatPercentage(Number value) {
        if (value == null) {
            return "N/A";
        }

        double num = value.doubleValue();

        if (Double.isNaN(num)) {
            return "0%";
        }

        return String.format("%.2f%%", num * 100);
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        return dateFormat.format(date);
    }
}
