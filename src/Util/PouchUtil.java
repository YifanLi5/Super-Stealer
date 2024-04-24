package Util;

import org.osbot.rs07.script.MethodProvider;

import static Task.Task.LOGOUT_ON_SCRIPT_STOP;
import static Util.GlobalMethodProvider.globalMethodProvider;

public class PouchUtil {
    private static final String COIN_POUCH = "Coin pouch";

    // May take multiple attempts to open pouches
    public static boolean openPouches() throws InterruptedException {
        int attempts = 0;
        while(globalMethodProvider.inventory.contains(COIN_POUCH) && attempts < 10) {
            if(globalMethodProvider.inventory.interact("Open-all", COIN_POUCH)) {
                break;
            }
            MethodProvider.sleep(600);
            attempts++;
        }
        return !globalMethodProvider.inventory.contains(COIN_POUCH);
    }
}
