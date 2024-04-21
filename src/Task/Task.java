package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.ArrayList;

public abstract class Task extends MethodProvider {
     protected final static boolean LOGOUT_ON_SCRIPT_STOP = false;
     protected final Script script;
     public static boolean stopScriptNow = false;

    private static final ArrayList<Task> subclassInstances = new ArrayList<>();

    public Task(Bot bot) {
        exchangeContext(bot);
        subclassInstances.add(this);
        this.script = bot.getScriptExecutor().getCurrent();
        log("Initialized task instance of type: " + this.getClass().getCanonicalName());
    }

    public static Task nextTask() throws InterruptedException {
        Task nextTask = null;
        for (Task task : Task.subclassInstances) {
            if (task.shouldRun()) {
                nextTask = task;
                break;
            }
        }
        return nextTask;
    }

    public static void clearSubclassInstances() {
        subclassInstances.clear();
    }

    public abstract boolean shouldRun() throws InterruptedException;

    public abstract void runTask() throws InterruptedException;

}
