package me.kicksquare.mcmspigot;

import io.sentry.Sentry;
import me.kicksquare.mcmspigot.util.LoggerUtil;

public class SentryExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // loop through the stack trace and print it out
        boolean isFromThisPlugin = false;
        for (StackTraceElement element : e.getStackTrace()) {
            //todo change this package name
            if (element.getClassName().contains("me.kicksquare.mcmspigot")) {
                isFromThisPlugin = true;
                break;
            }
        }
        if (isFromThisPlugin) {
            LoggerUtil.debug("Detected an MCMetrics exception. Uploading to sentry.");
            Sentry.captureException(e);
        }
    }
}
