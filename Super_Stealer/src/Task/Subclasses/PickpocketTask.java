package Task.Subclasses;

import Task.Task;
import UI.ScriptPaint;
import Util.MidStunUtil;
import Util.PickpocketUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.Skill;

public class PickpocketTask extends Task {

    private final int maxCoinPouches;

    public PickpocketTask(Bot bot) {
        super(bot);
        if (PickpocketUtil.userSelections == null || PickpocketUtil.userSelections.isEmpty()) {
            script.warn("TargetNPCDefinition.targetNPCs null or empty");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
        this.maxCoinPouches = PickpocketUtil.getMaxPossibleCoinPouchStack();
    }

    @Override
    public boolean shouldRun() {
        return inventory.getEmptySlots() > 0
                && inventory.getAmount("Coin pouch") < maxCoinPouches
                && !MidStunUtil.isPlayerStunned()
                && skills.getDynamic(Skill.HITPOINTS) > 5;
    }

    @Override
    public void runTask() throws InterruptedException {
        if (inventory.isItemSelected())
            inventory.deselectItem();

        ScriptPaint.setStatus("Pickpocketing");
        boolean foundPickpocketTarget = PickpocketUtil.setPickpocketTarget();
        if (!foundPickpocketTarget) {
            script.warn("Exceeded attempts to find a valid NPC");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }

        boolean interactionSuccessful = PickpocketUtil.pickpocketTarget();
        if (!interactionSuccessful) {
            script.warn("Multiple pickpocket interactions failed.");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }
}
