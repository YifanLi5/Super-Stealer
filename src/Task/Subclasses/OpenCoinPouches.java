package Task.Subclasses;

import Paint.ScriptPaint;
import Task.Task;
import Util.PickpocketUtil;
import org.osbot.rs07.Bot;


public class OpenCoinPouches extends Task {

    private static final String COIN_POUCH = "Coin pouch";
    private final int maxCoinPouches;

    public OpenCoinPouches(Bot bot) {
        super(bot);
        this.maxCoinPouches = PickpocketUtil.getMaxPossibleCoinPouchStack();
    }

    @Override
    public boolean shouldRun() {
        return inventory.getAmount(COIN_POUCH) >= maxCoinPouches - random(5);
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Opening pouches");
        if(!inventory.interact("Open-all", COIN_POUCH)) {
            script.warn("Error: Unable to open-all coin pouches");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }

}
