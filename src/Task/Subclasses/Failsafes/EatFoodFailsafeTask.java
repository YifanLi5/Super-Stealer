package Task.Subclasses.Failsafes;

import Task.Task;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.ui.Skill;

public class EatFoodFailsafeTask extends Task {
    public EatFoodFailsafeTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return myPlayer().getHealthPercentCache() < 25 || skills.getDynamic(Skill.HITPOINTS) < 5;
    }

    @Override
    public void runTask() throws InterruptedException {
        if(!inventory.interact("Eat", new ActionFilter<>("Eat"))) {
            log("Error: Unable to interact with eatable item in inventory");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }
}
