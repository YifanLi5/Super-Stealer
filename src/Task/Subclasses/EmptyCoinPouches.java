package Task.Subclasses;

import Task.Task;
import org.osbot.rs07.Bot;

public class EmptyCoinPouches extends Task {

    public EmptyCoinPouches(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return false;
    }

    @Override
    public void runTask() throws InterruptedException {

    }
}
