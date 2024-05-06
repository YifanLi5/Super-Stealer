package Task.Subclasses;

import Task.Task;
import UI.ScriptPaint;
import Util.RetryUtil;
import Util.RngUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.*;
import org.osbot.rs07.listener.MessageListener;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class CastShadowVeilTask extends Task implements MessageListener {
    boolean canCast = true;
    boolean isSvActive = false;
    long nextSVTimestamp = 0;

    public CastShadowVeilTask(Bot bot) {
        super(bot);
        bot.addMessageListener(this);
    }

    public static boolean canCastSV() {
        if (!globalMethodProvider.magic.open()) {
            globalMethodProvider.log("Unable to open magic tab");
            return false;
        }
        RS2Widget shadowVeilSpellWidget = globalMethodProvider.widgets.singleFilter(218, rs2Widget -> rs2Widget.getSpellName().contains("Shadow Veil"));
        if (shadowVeilSpellWidget.getSpriteIndex1() == 1334 || globalMethodProvider.skills.getStatic(Skill.MAGIC) < 47) {
            globalMethodProvider.log("Unable to cast Shadow veil, Widget is using blacked out || is on cooldown || < 47 magic, ");
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return canCast && !isSvActive && System.currentTimeMillis() > nextSVTimestamp;
    }

    @Override
    public void runTask() throws InterruptedException {
        // Once magic.canCast returns false, never run this task again.
        if (canCast) {
            canCast = canCastSV();
            if (!canCast) {
                log("canCast(SHADOW_VEIL) returned false, will no longer cast S.V. this script session.");
                return;
            }
        }
        ScriptPaint.setStatus("Casting shadow veil");
        if (!RetryUtil.retry(() -> magic.castSpell(Spells.ArceuusSpells.SHADOW_VEIL), 5, 600)) {
            warn("Unable to cast shadow veil spell.");
        }
        tabs.open(Tab.INVENTORY);
    }

    @Override
    public void onMessage(Message message) {
        if (message.getType() != Message.MessageType.GAME)
            return;
        String msg = message.getMessage();
        if (msg.contains("Your Shadow Veil")) {
            this.isSvActive = false;
            int delay = RngUtil.gaussian(20000, 3000, 0, 40000);
            this.nextSVTimestamp = System.currentTimeMillis() + delay;
            log(String.format("Shadow veil has faded; Next SV cast in %dms", delay));
        } else if (msg.contains("Your thieving abilities")) {
            this.isSvActive = true;
        }
    }
    // Too late, they're dead.
}
