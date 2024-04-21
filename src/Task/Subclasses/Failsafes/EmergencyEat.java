package Task.Subclasses.Failsafes;

import UI.ScriptPaint;
import Task.Task;
import Util.FoodUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.ui.Skill;

public class EmergencyEat extends Task {
    public EmergencyEat(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return myPlayer().getHealthPercentCache() < 25 || skills.getDynamic(Skill.HITPOINTS) < 5 && inventory.contains(FoodUtil.getAllFoodNames());
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Emergency Eating");
        if(!inventory.interact("Eat", new ActionFilter<>("Eat"))) {
            log("Error: Unable to interact with eatable item in inventory");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }
}
