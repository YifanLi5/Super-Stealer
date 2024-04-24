package Task.Subclasses.Failsafes;

import UI.ScriptPaint;
import Task.Task;
import Util.FoodUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.ui.Skill;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class EmergencyEat extends Task {
    public EmergencyEat(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return skills.getDynamic(Skill.HITPOINTS) < 5 && inventory.contains(FoodUtil.getAllFoodNames());
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Emergency Eating");

        int nextFoodSlot = FoodUtil.getInvSlotContainingFoodWithoutOverheal();
        if(nextFoodSlot == -1) {
            log("Using healing item not in FoodUtil, May overheal");
            nextFoodSlot = inventory.getSlot(new ActionFilter<>("Eat", "Drink"));
            if(nextFoodSlot == -1) {
                stopScriptNow("Unable to find an inventory slot containing food/drink.");
                return;
            }
        }

        if(!inventory.interact(nextFoodSlot, "Eat", "Drink")) {
            stopScriptNow("Error: Unable to interact with eatable/drinkable item in inventory");
        }
    }
}
