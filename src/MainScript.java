import UI.NPCSelectionPainter;
import UI.ScriptPaint;
import Task.Subclasses.*;
import Task.Subclasses.Failsafes.EmergencyEat;
import Task.Task;
import Util.GlobalMethodProvider;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Util.PickpocketUtil.PICKPOCKET;

@ScriptManifest(author = "yfoo", name = "[DEV14] Mark & Pickpocket", info = "Mark target NPC to have this bot to pickpocket them!", version = 0.1, logo = "")
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
        }

        GlobalMethodProvider.globalMethodProvider = this.bot.getMethods();

        NPCSelectionPainter selectionPainter = new NPCSelectionPainter(this,
                npc -> npc.hasAction(PICKPOCKET)
        );
        PickpocketUtil.userSelections = selectionPainter.awaitSelectedNPCDefinitions();


        // high -> low priority, if multiple task's shouldRun() would return true
        new EmergencyEat(this.bot);
        new OpenCoinPouches(this.bot);
        new BankTask(this.bot);
        new PickpocketTask(this.bot);
        new MidStunTask(this.bot);
        new EquipDodgyNecklaceTask(this.bot);

        scriptPaint = new ScriptPaint(this);

        tabs.open(Tab.INVENTORY);
        settings.setRunning(true);
        camera.movePitch(67);
    }

    @Override
    public int onLoop() throws InterruptedException {
        if(Task.stopScriptNow) {
            stop(false);
            return 250;
        }

        Task task = Task.nextTask();
        if(task != null) {
            task.runTask();
        }
        return RngUtil.gaussian(500, 150, 250, 900);
    }

    @Override
    public void onStop() throws InterruptedException {
        super.onStop();
        Task.clearSubclassInstances();
        scriptPaint.onStopCleanup();
    }
}
