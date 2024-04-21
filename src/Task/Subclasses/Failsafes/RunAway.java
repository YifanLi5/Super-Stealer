package Task.Subclasses.Failsafes;

import Paint.ScriptPaint;
import Task.Task;
import org.osbot.rs07.Bot;

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
        //Todo: Implement
        ScriptPaint.setStatus("Run away!");
        log("Detected player is under attack!");
    }
}
