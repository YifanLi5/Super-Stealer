package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.PickpocketUtil;
import Util.PouchUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.concurrent.Callable;

import static Util.GlobalMethodProvider.globalMethodProvider;


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

        if(!PouchUtil.openPouches()) {
            stopScriptNow("Unable to open-all coin pouches");
        }
    }

}
