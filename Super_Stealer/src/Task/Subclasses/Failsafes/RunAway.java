package Task.Subclasses.Failsafes;

import Task.Task;
import UI.ScriptPaint;
import Util.BankAreaUtil;
import Util.RetryUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;

public class RunAway extends Task {
    public RunAway(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return myPlayer().isUnderAttack();
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Run away!");
        log("Player is under attack!. Walking to nearest bank. Then will attempt to hop worlds.");
        Position returnPosition = myPosition();
        if(!walking.webWalk(BankAreaUtil.getAccessibleBanks(bot.getMethods()))) {
            stopScriptNow("When running away, Unable to walk to nearest bank.");
        }
        boolean isStillUnderAttack = RetryUtil.retry(() -> myPlayer().isUnderAttack(), 5, 1000);

        if(isStillUnderAttack) {
            warn("Still under attack even after running to bank.");
            walking.webWalk(Banks.LUMBRIDGE_UPPER);
            stopScriptNow("Force Stop, attempted to run to Lumbridge.");
            return;
        }

        if(!RetryUtil.retry(() -> worlds.hopToP2PWorld(), 3, 3000)) {
            stopScriptNow("Unable to hop worlds.");
            return;
        }
        if(!walking.webWalk(returnPosition)) {
            stopScriptNow("Unable to walk back to return position: " + returnPosition);
        }
    }
}
