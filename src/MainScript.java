import UI.NPCSelectionPainter;
import UI.ScriptPaint;
import Task.Subclasses.*;
import Task.Subclasses.Failsafes.EmergencyEat;
import Task.Task;
import Util.GlobalMethodProvider;
import Util.PickpocketUtil;
import Util.RngUtil;
import Util.StartingEquipmentUtil;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Util.PickpocketUtil.PICKPOCKET;

@ScriptManifest(author = "yfoo", name = "[2] Mark & Steal", info = "Mark target NPC to have this bot to pickpocket them!", version = 0.9, logo = "")
public class MainScript extends Script {

    ScriptPaint scriptPaint;
    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        if(myPlayer().getHeight() >= 240) {
            warn("This script uses player height to determine if they are stunned (>=240). " +
                    "\nAs such your starting height cannot be >= 240. " +
                    "\nYour height is likely too high due to your weapon or headgear. Please remove them then restart.");
            stop(false);
        } else if (inventory.isFull()) {
            warn("Do not start this script with a full inventory; inventory.isFull() is used as a banking condition." +
                    "\nMake some space then restart.");
            stop(false);
        }

        // Share MethodProvider for UtilClasses
        GlobalMethodProvider.globalMethodProvider = this.bot.getMethods();

        // Await user selection for pickpocket NPC
        NPCSelectionPainter selectionPainter = new NPCSelectionPainter(this,
                npc -> npc.hasAction(PICKPOCKET)
        );
        PickpocketUtil.userSelections = selectionPainter.awaitSelectedNPCDefinitions();

        // Store starting inventory so bankTask can restock
        StartingEquipmentUtil.setStartingInventory();

        // high -> low priority
        // Call order of subclass's shouldRun()
        new EmergencyEat(this.bot);
        new OpenCoinPouchesTask(this.bot);
        new EquipDodgyNecklaceTask(this.bot);
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
        scriptPaint.onStopCleanup();
    }
}
