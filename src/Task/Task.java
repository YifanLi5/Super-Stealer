package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.ArrayList;

public abstract class Task extends MethodProvider {
    public final static boolean LOGOUT_ON_SCRIPT_STOP = false;
    private static final ArrayList<Task> subclassInstances = new ArrayList<>();
    public static boolean stopScriptNow = false;
    protected final Script script;

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

    public static void cleanupTasks(Bot bot) {
        for (Task task : subclassInstances) {
            if (task instanceof MessageListener) {
                bot.removeMessageListener((MessageListener) task);
            }
        }
        subclassInstances.clear();
    }

    public void stopScriptNow(String errorMsg) {
        warn("Error: " + errorMsg);
        script.stop(LOGOUT_ON_SCRIPT_STOP);
        stopScriptNow = true;
    }

    public abstract boolean shouldRun() throws InterruptedException;

    public abstract void runTask() throws InterruptedException;

}
