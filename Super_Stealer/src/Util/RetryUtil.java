package Util;

import org.osbot.rs07.script.MethodProvider;

import java.util.concurrent.Callable;

public class RetryUtil {
    public static boolean retry(Callable<Boolean> fx, int maxAttempts, int retryWaitTime) throws InterruptedException {
        int attempts = 0;
        boolean isSuccess = false;
        while (attempts < maxAttempts && !isSuccess) {
            try {
                isSuccess = fx.call();
            } catch (Exception ignored) {
            }
            attempts++;
            MethodProvider.sleep(1200);
        }
        return isSuccess;
    }
}
