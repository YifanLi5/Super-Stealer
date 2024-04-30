package Util;

import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.concurrent.Callable;

import static Task.Task.LOGOUT_ON_SCRIPT_STOP;
import static Util.GlobalMethodProvider.globalMethodProvider;

public class PouchUtil {
    private static final String COIN_POUCH = "Coin pouch";
    public static final String OPEN_ALL = "Open-all";

    public static boolean openPouches() throws InterruptedException {
        return RetryUtil.retry(() -> {
            if(globalMethodProvider.inventory.interact(OPEN_ALL, COIN_POUCH))
                return ConditionalSleep2.sleep(1000, () -> !globalMethodProvider.inventory.contains(COIN_POUCH));
            return false;
        }, 5, 1000);
    }
}
