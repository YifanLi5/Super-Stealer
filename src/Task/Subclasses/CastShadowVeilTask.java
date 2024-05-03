package Task.Subclasses;

import Task.Task;
import UI.ScriptPaint;
import Util.RetryUtil;
import Util.ShadowVeilUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.utility.ConditionalSleep2;

public class CastShadowVeilTask extends Task {
    boolean canCast = true;
    public CastShadowVeilTask(Bot bot) throws InterruptedException {
        super(bot);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return canCast && ShadowVeilUtil.svOffCooldown;
    }

    @Override
    public void runTask() throws InterruptedException {
        // Once magic.canCast returns false, never run this task again.
        if(canCast) {
            canCast = ShadowVeilUtil.canCastSV();
            if(!canCast) {
                log("canCast(SHADOW_VEIL) returned false, will no longer cast S.V. this script session.");
                return;
            }
        }
        ScriptPaint.setStatus("Casting shadow veil");
        if(RetryUtil.retry(() -> magic.castSpell(Spells.ArceuusSpells.SHADOW_VEIL), 5, 600)) {
            ConditionalSleep2.sleep(2000, () -> !ShadowVeilUtil.svOffCooldown);
        }
    }
    // Too late, they're dead.
}
