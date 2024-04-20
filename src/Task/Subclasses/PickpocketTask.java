package Task.Subclasses;

import Task.Task;
import Util.PickpocketUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalSleep2;

import static Util.PickpocketUtil.isPlayerAdjacentToPickpocketNPC;

public class PickpocketTask extends Task {

    public PickpocketTask(Bot bot) {
        super(bot);
        if(PickpocketUtil.userSelections == null || PickpocketUtil.userSelections.isEmpty()) {
            script.warn("TargetNPCDefinition.targetNPCs null or empty");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }

    @Override
    public boolean shouldRun() {
        //Todo: update Coin pouch to 56 if correct ardy achievement diary is done
        return inventory.getEmptySlots() > 0 && inventory.getAmount("Coin pouch") < 28 && myPlayer().getHeight() <= 200;
    }

    @Override
    public void runTask() throws InterruptedException {
        boolean foundPickpocketTarget = PickpocketUtil.setPickpocketTarget();
        if(!foundPickpocketTarget) {
            script.warn("Exceeded attempts to find a valid NPC");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }

        boolean interactionSuccessful = PickpocketUtil.pickpocketTarget();
        if(!interactionSuccessful) {
            script.warn("Multiple pickpocket interactions failed.");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }

        if(!PickpocketUtil.isPlayerAdjacentToPickpocketNPC()) {
            PickpocketUtil.menuHoverPickpocketOption();
        }
        ConditionalSleep2.sleep(5000, () -> myPlayer().isAnimating());
    }
}
