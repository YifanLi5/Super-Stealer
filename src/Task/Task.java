package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.ArrayList;

public abstract class Task extends MethodProvider {
    protected static boolean LOGOUT_ON_SCRIPT_STOP = false;
    static NPC knightInstance;
    protected Script script;
    private static final ArrayList<Task> subclassInstances = new ArrayList<>();

    public Task(Bot bot) {
        exchangeContext(bot);
        subclassInstances.add(this);
        this.script = bot.getScriptExecutor().getCurrent();
        log("Initialized task instance of type: " + this.getClass().getCanonicalName());
    }

    public static Task nextTask() {
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

    public abstract boolean shouldRun();

    public abstract void runTask() throws InterruptedException;

}
