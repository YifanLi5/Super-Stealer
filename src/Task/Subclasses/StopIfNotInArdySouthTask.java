package Task.Subclasses;

import Task.Task;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.map.constants.Banks;

public class StopIfNotInArdySouthTask extends Task {

    public StopIfNotInArdySouthTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return !Banks.ARDOUGNE_SOUTH.contains(myPosition());
    }

    @Override
    public void runTask() throws InterruptedException {
        stopScriptNow("Played exited Ardy South bank.");
    }
}
