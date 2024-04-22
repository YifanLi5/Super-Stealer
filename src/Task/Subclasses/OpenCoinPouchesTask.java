package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.PickpocketUtil;
import org.osbot.rs07.Bot;


public class OpenCoinPouchesTask extends Task {

    static final String COIN_POUCH = "Coin pouch";
    private final int maxCoinPouches;
    private int nextOpenOffset;

    public OpenCoinPouchesTask(Bot bot) {
        super(bot);
        this.maxCoinPouches = PickpocketUtil.getMaxPossibleCoinPouchStack();
        this.nextOpenOffset = random(10);
    }

    @Override
    public boolean shouldRun() {
        return inventory.getAmount(COIN_POUCH) >= maxCoinPouches - nextOpenOffset;
    }

    @Override
    public void runTask() throws InterruptedException {
        nextOpenOffset = random(10);
        log("Opening pouches, next open @ " + (maxCoinPouches - nextOpenOffset));
        ScriptPaint.setStatus("Opening pouches");


        if(!inventory.interact("Open-all", COIN_POUCH)) {
            script.warn("Error: Unable to open-all coin pouches");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }

}
