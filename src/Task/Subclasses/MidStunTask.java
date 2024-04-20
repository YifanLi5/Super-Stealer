package Task.Subclasses;

import Paint.ScriptPaint;
import Task.Task;
import Util.FoodUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import Util.Tuple;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class MidStunTask extends Task {

    private enum MidStunActions {
        EAT, NO_OP, EXTENDED_NO_OP, IMPOTENT_PICKPOCKET, PREPARE_MENU_HOVER
    }
    private static final int STUNNED_HEIGHT = 225;
    private final HashMap<MidStunActions, Integer> midStunActionWeightings;

    public MidStunTask(Bot bot) {
        super(bot);
        this.midStunActionWeightings = new HashMap<>();
        this.midStunActionWeightings.put(MidStunActions.EAT, RngUtil.gaussian(1000, 250, 300, 1700));
        this.midStunActionWeightings.put(MidStunActions.NO_OP, RngUtil.gaussian(500, 100, 300, 700));
        this.midStunActionWeightings.put(MidStunActions.EXTENDED_NO_OP, RngUtil.gaussian(100, 50, 25, 200));
        this.midStunActionWeightings.put(MidStunActions.IMPOTENT_PICKPOCKET, RngUtil.gaussian(750, 75, 450, 1050));
        this.midStunActionWeightings.put(MidStunActions.PREPARE_MENU_HOVER, RngUtil.gaussian(750, 75, 450, 1050));
        int weightSum = midStunActionWeightings.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        StringBuilder builder = new StringBuilder("***Mid Stun Action Weighting***\n");
        for (Map.Entry<MidStunActions, Integer> action: midStunActionWeightings.entrySet()) {
            builder.append(String.format("%s / %d\n",
                    action.getKey().toString(),
                    action.getValue()
                )
            );
        }
        log(builder);
    }

    @Override
    public boolean shouldRun() {
        return myPlayer().getHeight() >= STUNNED_HEIGHT;
    }

    @Override
    public void runTask() throws InterruptedException {
        switch (rollForAction()) {
            case EAT:
                eat();
                break;
            case NO_OP:
                no_op();
                break;
            case EXTENDED_NO_OP:
                extendedNo_op();
                break;
            case IMPOTENT_PICKPOCKET:
                impotentPickpocket();
                break;
            case PREPARE_MENU_HOVER:
                if(PickpocketUtil.getPickpocketTarget().getName().equalsIgnoreCase("Knight of Ardougne"))
                    no_op();
                else
                    prepareMenuHover();
                break;
        }
        ConditionalSleep2.sleep(5000, () -> myPlayer().getHeight() < STUNNED_HEIGHT);
    }

    private MidStunActions rollForAction() {
        ArrayList<Tuple<MidStunActions, Integer>> validActions = new ArrayList<>();

        validActions.add(new Tuple<>(MidStunActions.EAT, midStunActionWeightings.get(MidStunActions.EAT)));
        validActions.add(new Tuple<>(MidStunActions.EXTENDED_NO_OP, midStunActionWeightings.get(MidStunActions.EXTENDED_NO_OP)));
//        validActions.add(new Tuple<>(MidStunActions.IMPOTENT_PICKPOCKET, midStunActionWeightings.get(MidStunActions.IMPOTENT_PICKPOCKET)));
        validActions.add(new Tuple<>(MidStunActions.PREPARE_MENU_HOVER, midStunActionWeightings.get(MidStunActions.PREPARE_MENU_HOVER)));

        MidStunActions selectedAction = null;
        int weightSum = validActions.stream()
                .mapToInt(Tuple::getSecond)
                .sum();
        int roll = random(1, weightSum);
        for (Tuple<MidStunActions, Integer> action: validActions) {
            roll -= action.getSecond();
            if(roll <= 0) {
                selectedAction = action.getFirst();
                break;
            }
        }
        if(selectedAction == null) {
            script.stop(false);
        }
        return selectedAction;
    }

    private void eat() throws InterruptedException {
        while(myPlayer().getHeight() >= STUNNED_HEIGHT) {
            int nextFoodSlot = FoodUtil.getInvSlotContainingFoodWithoutOverheal();
            if(nextFoodSlot == -1) {
                break;
            }
            ScriptPaint.setStatus("MidStun: Eating");

            if(!inventory.interact("Eat", new ActionFilter<>("Eat"))) {
                log("Error: Unable to interact with eatable item in inventory");
                script.stop(LOGOUT_ON_SCRIPT_STOP);
                break;
            }
            if(FoodUtil.getInvSlotContainingFoodWithoutOverheal() >= 0) {
                inventory.hover(nextFoodSlot);
            }
            ConditionalSleep2.sleep(2000, () -> inventory.getItemInSlot(nextFoodSlot) == null);
            sleep(random(500, 1500));
        }
    }

    private void no_op() {
        ScriptPaint.setStatus("MidStun: no_op");
        ConditionalSleep2.sleep(5000, () -> myPlayer().getHeight() < STUNNED_HEIGHT);
    }

    private void extendedNo_op() throws InterruptedException {
        int sleepTime = RngUtil.gaussian(10000, 1000, 5000, 15000);
        ScriptPaint.setStatus(String.format("MidStun: Long AFK (%dms)", sleepTime));
        mouse.moveOutsideScreen();
        sleep(sleepTime);
    }

    private void impotentPickpocket() throws InterruptedException {
        PickpocketUtil.pickpocketTarget();
    }

    private void prepareMenuHover() throws InterruptedException {
        ScriptPaint.setStatus("MidStun: hover menu option");
        if(!PickpocketUtil.menuHoverPickpocketOption())
            warn("Pickpocket menu hover failed :(");

    }
}
