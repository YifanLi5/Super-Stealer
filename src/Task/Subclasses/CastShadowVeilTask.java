package Task.Subclasses;

import Task.Task;
import UI.ScriptPaint;
import Util.RetryUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.utility.ConditionalSleep2;

public class CastShadowVeilTask extends Task implements MessageListener {
    boolean canCastSV = true;
    public CastShadowVeilTask(Bot bot) {
        super(bot);
        this.bot.addMessageListener(this);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return canCastSV;
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Casting shadow veil");
        if(RetryUtil.retry(() -> magic.castSpell(Spells.ArceuusSpells.SHADOW_VEIL), 5, 600)) {
            ConditionalSleep2.sleep(2000, () -> !canCastSV);
        }
    }
    // Too late, they're dead.
    @Override
    public void onMessage(Message message) throws InterruptedException {
        if(message.getType() != Message.MessageType.GAME)
            return;
        String msg = message.getMessage();
        if(msg.contains("Your Shadow Veil")) {
            log("shadow veil is ready.");
            canCastSV = true;
        } else if (msg.contains("Your thieving abilities")) {
            canCastSV = false;
        }
            
    }
}
