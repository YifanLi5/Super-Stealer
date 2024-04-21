package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.FoodUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.HashSet;

public class MidStunTask extends Task {

    private final static int stunnedPlayerHeightThreshold = 240;

    private enum MidStunActions {
        EAT(RngUtil.gaussian(1000, 250, 300, 1700)),
        NO_OP(RngUtil.gaussian(500, 100, 300, 700)),
        EXTENDED_NO_OP(RngUtil.gaussian(100, 50, 25, 200)),
        SPAM_PICKPOCKET(RngUtil.gaussian(750, 75, 450, 1050)),
        PREPARE_MENU_HOVER(RngUtil.gaussian(750, 75, 450, 1050)),
        DROP_JUNK(RngUtil.gaussian(1000, 250, 300, 1700));

        final int executionWeight;
        MidStunActions(int executionWeight) {
            this.executionWeight = executionWeight;
        }
    }
    private final HashSet<MidStunActions> validActions = new HashSet<>();

    public MidStunTask(Bot bot) {
        super(bot);

        StringBuilder builder = new StringBuilder("***Mid Stun Action Weighting***\n");
        for (MidStunActions action: MidStunActions.values()) {
            builder.append(String.format("%s / %d\n",
                    action.name(),
                    action.executionWeight
                )
            );
        }
        log(builder);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return myPlayer().getHeight() >= stunnedPlayerHeightThreshold && PickpocketUtil.getPickpocketTarget() != null;
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
                if(myPlayer().getHealthPercentCache() < 75)
                    eat();
                pickpocketTarget.hover();
                break;
            case NO_OP:
                no_op();
                break;
            case EXTENDED_NO_OP:
                extendedNo_op();
                break;
            case SPAM_PICKPOCKET:
                spamPickpocket();
                break;
            case PREPARE_MENU_HOVER:
                if(PickpocketUtil.getPickpocketTarget().getName().equalsIgnoreCase("Knight of Ardougne"))
                    no_op();
                else
                    prepareMenuHover();
                break;
        }
        if(myPlayer().getHeight() >= stunnedPlayerHeightThreshold) {
            int sleepTime = RngUtil.gaussian(1000, 250, 0, 2400);
            ScriptPaint.setStatus(String.format("MidStun - additional wait (%dms)", sleepTime));
            sleep(sleepTime);
        }
    }

    private MidStunActions rollForAction() {
        validActions.add(MidStunActions.EAT);
        validActions.add(MidStunActions.EXTENDED_NO_OP);
        validActions.add(MidStunActions.SPAM_PICKPOCKET);
        validActions.add(MidStunActions.PREPARE_MENU_HOVER);

        MidStunActions selectedAction = null;
        int weightSum = validActions.stream()
                .mapToInt(action -> action.executionWeight)
                .sum();
        int roll = random(1, weightSum);
        for (MidStunActions action: validActions) {
            roll -= action.executionWeight;
            if(roll <= 0) {
                selectedAction = action;
                break;
            }
        }
        validActions.clear();
        if(selectedAction == null) {
            script.warn("Error: rolling for an action returned null, this should not happen.");
            script.stop(false);
        }
        return selectedAction;
    }

    private void eat() throws InterruptedException {
        int nextFoodSlot = FoodUtil.getInvSlotContainingFoodWithoutOverheal();
        if(nextFoodSlot == -1) {
            log("Using food item not in FoodUtil, May overheal");
            nextFoodSlot = inventory.getSlot(new ActionFilter<>("Eat", "Drink"));
        }
        ScriptPaint.setStatus("MidStun - Eating");

        if(!inventory.interact(nextFoodSlot, "Eat", "Drink")) {
            warn(String.format("Error: Unable to use item in slot (%d) to heal.", nextFoodSlot));
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }
        int lambdaTemp = nextFoodSlot;
        ConditionalSleep2.sleep(2000, () -> inventory.getItemInSlot(lambdaTemp) == null);

        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if(pickpocketTarget == null) {
            script.warn("Error: pickpocket target is null even after attempting to re-query");
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

    private void spamPickpocket() throws InterruptedException {
        ScriptPaint.setStatus("MidStun - spam pickpocket");
        while(myPlayer().getHeight() >= stunnedPlayerHeightThreshold) {
            PickpocketUtil.pickpocketTarget();
            sleep(RngUtil.gaussian(500, 100, 0, 1000));
        }
    }

    private void prepareMenuHover() throws InterruptedException {
        ScriptPaint.setStatus("MidStun - hover menu option");
        if(!PickpocketUtil.menuHoverPickpocketOption())
            log("Pickpocket menu hover failed :(");

    }
}
