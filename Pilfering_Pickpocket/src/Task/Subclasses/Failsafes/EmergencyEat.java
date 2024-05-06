package Task.Subclasses.Failsafes;

import Task.Task;
import UI.ScriptPaint;
import Util.FoodUtil;
import Util.RetryUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.ui.Skill;

import java.util.concurrent.Callable;

public class EmergencyEat extends Task {
    public EmergencyEat(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return skills.getDynamic(Skill.HITPOINTS) <= 5 && inventory.contains(new ActionFilter<>("Eat", "Drink"));
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Emergency Eating");
        log("Emergency Eat!");
        int nextFoodSlot = FoodUtil.getInvSlotContainingFoodWithoutOverheal();
        if (nextFoodSlot == -1) {
            log("Using healing item not in FoodUtil, May overheal");
            nextFoodSlot = inventory.getSlot(new ActionFilter<>("Eat", "Drink"));
            if (nextFoodSlot == -1) {
                stopScriptNow("Unable to find an inventory slot containing food/drink.");
                return;
            }
        }

        final int finalNextFoodSlot = nextFoodSlot;
        final Callable<Boolean> eatItem = () -> inventory.interact(finalNextFoodSlot, "Eat", "Drink");

        if (!RetryUtil.retry(eatItem, 5, 600)) {
            stopScriptNow("Player HP <= 5 & Unable to interact with eatable/drinkable item in inventory");
        }
    }
}
