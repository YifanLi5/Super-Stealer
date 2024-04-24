package Util;

import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.concurrent.Callable;

import static Task.Task.LOGOUT_ON_SCRIPT_STOP;
import static Util.GlobalMethodProvider.globalMethodProvider;

public class PouchUtil {
    private static final String COIN_POUCH = "Coin pouch";

    public static boolean openPouches() throws InterruptedException {
        int attempts = 0;
        while(globalMethodProvider.inventory.contains(COIN_POUCH) && attempts < 10) {
            globalMethodProvider.inventory.interact("Open-all", COIN_POUCH);
            MethodProvider.sleep(600);
            attempts++;
        }
        return !globalMethodProvider.inventory.contains(COIN_POUCH);
    }
}
