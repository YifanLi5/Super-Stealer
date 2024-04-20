import Paint.NPCSelectionPainter;
import Paint.ScriptPaint;
import Task.Subclasses.BankTask;
import Task.Subclasses.Failsafes.EatFoodFailsafeTask;
import Task.Subclasses.MidStunTask;
import Task.Subclasses.OpenCoinPouches;
import Task.Subclasses.PickpocketTask;
import Task.Task;
import Util.GlobalMethodProvider;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Util.PickpocketUtil.PICKPOCKET;

@ScriptManifest(author = "yfoo", name = "[DEV9] Mark & Pickpocket", info = "Mark target NPC to have this bot to pickpocket them!", version = 0.1, logo = "")
public class MainScript extends Script {

    ScriptPaint scriptPaint;
    @Override
    public void onStart() throws InterruptedException {
        super.onStart();
        GlobalMethodProvider.methodProvider = this.bot.getMethods();

        NPCSelectionPainter selectionPainter = new NPCSelectionPainter(this,
                npc -> npc.hasAction(PICKPOCKET)
        );
        PickpocketUtil.userSelections = selectionPainter.awaitSelectedNPCDefinitions();


        // high -> low priority, if multiple task's shouldRun() return true
        new EatFoodFailsafeTask(this.bot);
        new OpenCoinPouches(this.bot);
        new BankTask(this.bot);
        new PickpocketTask(this.bot);
        new MidStunTask(this.bot);

        scriptPaint = new ScriptPaint(this);

        tabs.open(Tab.INVENTORY);
        settings.setRunning(true);
        camera.movePitch(67);
    }

    @Override
    public int onLoop() throws InterruptedException {
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
