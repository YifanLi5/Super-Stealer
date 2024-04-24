package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.PickpocketUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalSleep2;

public class PickpocketTask extends Task {

    private final int maxCoinPouches;

    public PickpocketTask(Bot bot) {
        super(bot);
        if(PickpocketUtil.userSelections == null || PickpocketUtil.userSelections.isEmpty()) {
            script.warn("TargetNPCDefinition.targetNPCs null or empty");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
        this.maxCoinPouches = PickpocketUtil.getMaxPossibleCoinPouchStack();
    }

    @Override
    public boolean shouldRun() {
        return inventory.getEmptySlots() > 0
                && inventory.getAmount("Coin pouch") < maxCoinPouches
                && myPlayer().getHeight() <= 200;
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Pickpocketing");
        boolean foundPickpocketTarget = PickpocketUtil.setPickpocketTarget();
        if(!foundPickpocketTarget) {
            script.warn("Exceeded attempts to find a valid NPC");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }

        boolean interactionSuccessful = PickpocketUtil.pickpocketTarget();
        if(!interactionSuccessful) {
            script.warn("Multiple pickpocket interactions failed.");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }

//        if(!PickpocketUtil.isPlayerAdjacentToPickpocketNPC()) {
//            PickpocketUtil.menuHoverPickpocketOption();
//        }
        //ConditionalSleep2.sleep(5000, () -> myPlayer().isAnimating());
    }
}
