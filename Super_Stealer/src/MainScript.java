import Task.Subclasses.*;
import Task.Subclasses.Failsafes.EmergencyEat;
import Task.Task;
import UI.NPCSelectionPainter;
import UI.ScriptPaint;
import Util.*;
import org.osbot.rs07.api.Settings;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Util.PickpocketUtil.PICKPOCKET;

@ScriptManifest(author = "yfoo", name = "Super Stealer", info = "Mark target NPC to have this bot to pickpocket them!", version = 1.1, logo = "https://github.com/YifanLi5/Super-Stealer/blob/master/readme_imgs/super_stealer_logo.jpg?raw=true")
public class MainScript extends Script {

    ScriptPaint scriptPaint;
    NPCSelectionPainter selectionPainter;
    MessageListener svMessageListener;

    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        int emptySlotsNeeded = 2;
        if (inventory.contains("Coins"))
            emptySlotsNeeded--;
        if (inventory.contains("Coin pouch"))
            emptySlotsNeeded--;
        if (inventory.getEmptySlots() < emptySlotsNeeded) {
            warn("You need room for coin pouch + coins." +
                    "\nMake some space then restart.");
            stop(false);
            return;
        }



        boolean isNPCAtkHidden = configs.isSet(1306, 3);
        log("isNPCAtkHidden: " + isNPCAtkHidden);
        if(!isNPCAtkHidden) {
            log("Attempting to set NPC attack options to hidden...");
            isNPCAtkHidden = RetryUtil.retry(
                    () -> settings.setSetting(Settings.AllSettingsTab.CONTROLS, "NPC Attack options", "Hidden"),
                    5, 2500
            ) && widgets.closeOpenInterface();

            if(!isNPCAtkHidden) {
                warn("Error attempting to set npc atk options to hidden.");
                stop(false);
            }
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

        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if (Banks.ARDOUGNE_SOUTH.contains(myPosition())) {
            assert pickpocketTarget != null;
            if (pickpocketTarget.getName().equals("Knight of Ardougne")) {
                log("Player is at mass ardy knights (in Ardy S. Bank). Will stop if they exit the bank.");
                new StopIfNotInArdySouthTask(this.bot);
            }
        }
        new OpenCoinPouchesTask(this.bot);
        new EquipDodgyNecklaceTask(this.bot);
        if (CastShadowVeilTask.canCastSV()) {
            new CastShadowVeilTask(this.bot);
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
        if (Task.stopScriptNow) {
            stop(false);
            return 5000;
        }

        Task task = Task.nextTask();
        if (task != null) {
            task.runTask();
        }
        return RngUtil.gaussian(250, 50, 0, 350);
    }

    @Override
    public void onStop() throws InterruptedException {
        super.onStop();
        Task.cleanupTasks(bot);
        if (scriptPaint != null)
            scriptPaint.onStopCleanup();
        if (selectionPainter != null)
            selectionPainter.onStopCleanup();
        if (svMessageListener != null)
            this.bot.removeMessageListener(svMessageListener);
    }
}
