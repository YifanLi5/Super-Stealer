import UI.NPCSelectionPainter;
import UI.ScriptPaint;
import Task.Subclasses.*;
import Task.Subclasses.Failsafes.EmergencyEat;
import Task.Task;
import Util.*;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Util.PickpocketUtil.PICKPOCKET;

@ScriptManifest(author = "yfoo", name = "[1] Pilfering Pickpocket", info = "Mark target NPC to have this bot to pickpocket them!", version = 1.0, logo = "")
public class MainScript extends Script {

    ScriptPaint scriptPaint;
    NPCSelectionPainter selectionPainter;
    MessageListener svMessageListener;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        if (inventory.getEmptySlots() <= 2) {
            warn("Do not start this script with <= 2 empty slots; you  need room for coin pouch + coins." +
                    "\nMake some space then restart.");
            stop(false);
        }

        // Share MethodProvider for UtilClasses
        GlobalMethodProvider.globalMethodProvider = this.bot.getMethods();

        // Await user selection for pickpocket NPC
        selectionPainter = new NPCSelectionPainter(this,
                npc -> npc.hasAction(PICKPOCKET)
        );
        PickpocketUtil.userSelections = selectionPainter.awaitSelectedNPCDefinitions();

        // Store starting inventory so bankTask can restock
        StartingEquipmentUtil.setStartingInventory();

        // When player is stunned their vertices count goes up by ~42.
        // Get baseline vertices when idle to determine when stunned.
        MidStunUtil.approxVerticesCountStunned = myPlayer().getModel().getVerticesCount() + 42;

        // high -> low priority of tasks
        new EmergencyEat(this.bot);
        // If Player starts in Ardy South bank, Stop the script if they exit.
        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if(Banks.ARDOUGNE_SOUTH.contains(myPosition())) {
            assert pickpocketTarget != null;
            if (pickpocketTarget.getName().equals("Knight of Ardougne")) {
                log("Player is at mass ardy knights (in Ardy S. Bank). Will stop if they exit the bank.");
                new StopIfNotInArdySouthTask(this.bot);
            }
        }
        new OpenCoinPouchesTask(this.bot);
        new EquipDodgyNecklaceTask(this.bot);
        if(ShadowVeilUtil.canCastSV()) {
            new CastShadowVeilTask(this.bot);
            svMessageListener = ShadowVeilUtil.initMessageListener(this.bot);
        }

        new BankTask(this.bot);
        new MidStunTask(this.bot);
        new PickpocketTask(this.bot);

        // init script paint
        scriptPaint = new ScriptPaint(this);

        // other setup
        tabs.open(Tab.INVENTORY);
        settings.setRunning(true);
        camera.movePitch(67);
    }

    @Override
    public int onLoop() throws InterruptedException {
        if(Task.stopScriptNow) {
            stop(false);
            return 5000;
        }

        Task task = Task.nextTask();
        if(task != null) {
            task.runTask();
        }
        return RngUtil.gaussian(250, 50, 0, 350);
    }

    @Override
    public void onStop() throws InterruptedException {
        super.onStop();
        Task.clearSubclassInstances();
        if(scriptPaint != null)
            scriptPaint.onStopCleanup();
        if(selectionPainter != null)
            selectionPainter.onStopCleanup();
        if(svMessageListener != null)
            this.bot.removeMessageListener(svMessageListener);
    }
}
