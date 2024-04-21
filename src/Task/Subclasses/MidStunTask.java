package Task.Subclasses;

import Paint.ScriptPaint;
import Task.Task;
import Util.FoodUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import Util.Tuple;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MidStunTask extends Task {

    private enum MidStunActions {
        EAT, NO_OP, EXTENDED_NO_OP, IMPOTENT_PICKPOCKET, PREPARE_MENU_HOVER
    }
    private static final int STUNNED_HEIGHT = 225;
    private final HashMap<MidStunActions, Integer> midStunActionWeightings;
    private final ArrayList<Tuple<MidStunActions, Integer>> validActions = new ArrayList<>();

    public MidStunTask(Bot bot) {
        super(bot);
        this.midStunActionWeightings = new HashMap<>();
        this.midStunActionWeightings.put(MidStunActions.EAT, RngUtil.gaussian(1000, 250, 300, 1700));
        this.midStunActionWeightings.put(MidStunActions.NO_OP, RngUtil.gaussian(500, 100, 300, 700));
        this.midStunActionWeightings.put(MidStunActions.EXTENDED_NO_OP, RngUtil.gaussian(100, 50, 25, 200));
        this.midStunActionWeightings.put(MidStunActions.IMPOTENT_PICKPOCKET, RngUtil.gaussian(750, 75, 450, 1050));
        this.midStunActionWeightings.put(MidStunActions.PREPARE_MENU_HOVER, RngUtil.gaussian(750, 75, 450, 1050));

        StringBuilder builder = new StringBuilder("***Mid Stun Action Weighting***\n");
        for (Map.Entry<MidStunActions, Integer> action: midStunActionWeightings.entrySet()) {
            builder.append(String.format("%s / %d\n",
                    action.getKey().toString(),
                    action.getValue()
                )
            );
        }

        validActions.add(new Tuple<>(MidStunActions.EAT, midStunActionWeightings.get(MidStunActions.EAT)));
        //validActions.add(new Tuple<>(MidStunActions.EXTENDED_NO_OP, midStunActionWeightings.get(MidStunActions.EXTENDED_NO_OP)));
        validActions.add(new Tuple<>(MidStunActions.IMPOTENT_PICKPOCKET, midStunActionWeightings.get(MidStunActions.IMPOTENT_PICKPOCKET)));
        validActions.add(new Tuple<>(MidStunActions.PREPARE_MENU_HOVER, midStunActionWeightings.get(MidStunActions.PREPARE_MENU_HOVER)));
        log(builder);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return myPlayer().getAnimation() == 424 && PickpocketUtil.getPickpocketTarget() != null;
    }

    @Override
    public void runTask() throws InterruptedException {
        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if(pickpocketTarget == null) {
            script.warn("pickpocket target is null even after attempting to re-query");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }

        switch (rollForAction()) {
            case EAT:
                eat();
                pickpocketTarget.hover();
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

        int sleepTime = RngUtil.gaussian(1000, 250, 0, 2400);
        ScriptPaint.setStatus(String.format("MidStun - wait for stun (%dms)", sleepTime));
        sleep(sleepTime);
    }

    private MidStunActions rollForAction() {
        validActions.add(new Tuple<>(MidStunActions.EAT, midStunActionWeightings.get(MidStunActions.EAT)));
        validActions.add(new Tuple<>(MidStunActions.EXTENDED_NO_OP, midStunActionWeightings.get(MidStunActions.EXTENDED_NO_OP)));
        validActions.add(new Tuple<>(MidStunActions.IMPOTENT_PICKPOCKET, midStunActionWeightings.get(MidStunActions.IMPOTENT_PICKPOCKET)));
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
        int nextFoodSlot = FoodUtil.getInvSlotContainingFoodWithoutOverheal();
        if(nextFoodSlot == -1) {
            return;
        }
        ScriptPaint.setStatus("MidStun - Eating");

        if(!inventory.interact("Eat", new ActionFilter<>("Eat"))) {
            log("Error: Unable to interact with eatable item in inventory");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }
        if(FoodUtil.getInvSlotContainingFoodWithoutOverheal() >= 0) {
            inventory.hover(nextFoodSlot);
        }
        ConditionalSleep2.sleep(2000, () -> inventory.getItemInSlot(nextFoodSlot) == null);

        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if(pickpocketTarget == null) {
            script.warn("pickpocket target is null even after attempting to re-query");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }
        PickpocketUtil.getPickpocketTarget().hover();
    }

    private void no_op() {
        ScriptPaint.setStatus("MidStun - no_op");
    }

    private void extendedNo_op() throws InterruptedException {
        int sleepTime = RngUtil.gaussian(10000, 1000, 5000, 15000);
        ScriptPaint.setStatus(String.format("MidStun - Long AFK (%dms)", sleepTime));
        mouse.moveOutsideScreen();
        sleep(sleepTime);
    }

    private void impotentPickpocket() throws InterruptedException {
        PickpocketUtil.pickpocketTarget();
    }

    private void prepareMenuHover() throws InterruptedException {
        ScriptPaint.setStatus("MidStun - hover menu option");
        if(!PickpocketUtil.menuHoverPickpocketOption())
            warn("Pickpocket menu hover failed :(");

    }
}
