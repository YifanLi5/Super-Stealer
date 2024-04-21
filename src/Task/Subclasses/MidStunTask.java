package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.MidStunUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.HashSet;
import java.util.concurrent.Callable;

public class MidStunTask extends Task {

    public final static int STUNNED_HEIGHT = 240;
    private final static String[] junk = {"Jug", "Bowl", "Vial"};

    private enum MidStunActions {
        EAT(RngUtil.gaussian(1000, 250, 300, 1700)),
        NO_OP(RngUtil.gaussian(500, 100, 300, 700)),
        EXTENDED_NO_OP(RngUtil.gaussian(50, 25, 0, 150)),
        SPAM_PICKPOCKET(RngUtil.gaussian(750, 75, 450, 1050)),
        PREPARE_MENU_HOVER(RngUtil.gaussian(750, 75, 450, 1050)),
        DROP_JUNK(RngUtil.gaussian(1000, 250, 300, 1700)),
        CAST_SHADOW_VEIL(RngUtil.gaussian(1000, 250, 300, 1700)); // Todo: implement after I get access

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

        validActions.add(MidStunActions.EXTENDED_NO_OP);
        validActions.add(MidStunActions.SPAM_PICKPOCKET);
        validActions.add(MidStunActions.PREPARE_MENU_HOVER);

    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return myPlayer().getHeight() >= STUNNED_HEIGHT && PickpocketUtil.getPickpocketTarget() != null;
    }

    @Override
    public void runTask() throws InterruptedException {
        if(camera.getPitchAngle() != 67)
            camera.movePitch(67);
        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if(pickpocketTarget == null) {
            script.warn("pickpocket target is null even after attempting to re-query");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }

        switch (rollForAction()) {
            case EAT:
                if(myPlayer().getHealthPercentCache() < 75)
                    MidStunUtil.eat();
                pickpocketTarget.hover();
                break;
            case NO_OP:
                MidStunUtil.no_op();
                break;
            case EXTENDED_NO_OP:
                MidStunUtil.extendedNo_op();
                break;
            case SPAM_PICKPOCKET:
                MidStunUtil.spamPickpocket();
                break;
            case PREPARE_MENU_HOVER:
                if(PickpocketUtil.getPickpocketTarget().getName().equalsIgnoreCase("Knight of Ardougne"))
                    MidStunUtil.no_op();
                else
                    MidStunUtil.prepareMenuHover();
                break;
            case DROP_JUNK:
                MidStunUtil.dropJunk();
                break;
        }
        ConditionalSleep2.sleep(3000, () -> myPlayer().getHeight() < STUNNED_HEIGHT);
    }

    private MidStunActions rollForAction() {
        if(myPlayer().getHealthPercentCache() < 65) {
            script.log("< 65% hp, can now eat as mid stun action");
            validActions.add(MidStunActions.EAT);
        }
        else if(validActions.contains(MidStunActions.EAT)){
            validActions.remove(MidStunActions.EAT);
            script.log(">= 65% hp, no more eating as mid stun action");
        }

        if(inventory.contains(junk))
            validActions.add(MidStunActions.DROP_JUNK);
        else validActions.remove(MidStunActions.DROP_JUNK);


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

        if(selectedAction == null) {
            script.warn("Error: rolling for an action returned null, this should not happen.");
            script.stop(false);
        }
        return selectedAction;
    }
}
