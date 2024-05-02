package Task.Subclasses;

import Task.Task;
import Util.GlobalMethodProvider;
import Util.MidStunUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.HashSet;
import java.util.concurrent.Callable;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class MidStunTask extends Task {

    private final static String[] junk = {"Jug", "Bowl", "Vial"};

    private enum MidStunActions {
        EAT(RngUtil.gaussian(1000, 250, 300, 1700), () ->
                globalMethodProvider.myPlayer().getHealthPercentCache() < 65
                        && globalMethodProvider.inventory.contains(new ActionFilter<>("Eat", "Drink"))
                , false
        ),
        NO_OP(RngUtil.gaussian(500, 100, 300, 700), () -> true, true),
        EXTENDED_NO_OP(RngUtil.gaussian(50, 25, 0, 150), () -> true, true),
        SPAM_PICKPOCKET(RngUtil.gaussian(750, 75, 450, 1050), () -> true, true),
        PREPARE_MENU_HOVER(RngUtil.gaussian(750, 75, 450, 1050), () -> true, true),
        DROP_JUNK(RngUtil.gaussian(1000, 250, 300, 1700), () ->
                globalMethodProvider.inventory.contains(junk)
        , false),
        // Todo: implement after I get access
        CAST_SHADOW_VEIL(RngUtil.gaussian(1000, 250, 300, 1700), () -> true, false);


        final int executionWeight;
        final Callable<Boolean> canRun;
        final boolean isTerminal;

        MidStunActions(int executionWeight, Callable<Boolean> canRun, boolean isTerminal) {
            this.executionWeight = executionWeight;
            this.canRun = canRun;
            this.isTerminal = isTerminal;
        }
    }
    private final HashSet<MidStunActions> validActions = new HashSet<>();

    private int eatAtHpPercentage;

    public MidStunTask(Bot bot) throws InterruptedException {
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

        // Too many players at mass ardy knight splash world result in very large menu.
        NPC target = PickpocketUtil.getPickpocketTarget();
        if(target != null && !target.getName().equalsIgnoreCase("Knight of Ardougne"))
            validActions.add(MidStunActions.PREPARE_MENU_HOVER);

        this.eatAtHpPercentage = RngUtil.gaussian(50, 10, 35, 70);
        log("initial eatAtHpPercentage -> " + this.eatAtHpPercentage);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return MidStunUtil.isPlayerStunned() && PickpocketUtil.getPickpocketTarget() != null;
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
        MidStunActions action = rollForAction();
        if(action == null) {
            return;
        }

        switch (action) {
            case EAT:
                if(playersHealthPercent() < this.eatAtHpPercentage) {
                    MidStunUtil.eat();
                    if(playersHealthPercent() >= this.eatAtHpPercentage) {
                        this.eatAtHpPercentage = RngUtil.gaussian(50, 15, 35, 70);
                        log("next eatAtHpPercentage -> " + this.eatAtHpPercentage);
                    }
                }
                break;
            case NO_OP:
                break;
            case EXTENDED_NO_OP:
                MidStunUtil.extendedNo_op();
                break;
            case SPAM_PICKPOCKET:
                MidStunUtil.spamPickpocket();
                break;
            case PREPARE_MENU_HOVER:
                MidStunUtil.prepareMenuHover();
                break;
            case DROP_JUNK:
                MidStunUtil.dropJunk();
                break;
        }

        if(action.isTerminal) {
            ConditionalSleep2.sleep(3000, () -> !MidStunUtil.isPlayerStunned());
        }
    }

    private MidStunActions rollForAction() {
        try {
            if(MidStunActions.EAT.canRun.call())
                validActions.add(MidStunActions.EAT);
            else validActions.remove(MidStunActions.EAT);

            if(MidStunActions.DROP_JUNK.canRun.call())
                validActions.add(MidStunActions.DROP_JUNK);
            else validActions.remove(MidStunActions.DROP_JUNK);

        } catch (Exception e) {
            stopScriptNow("Got exception when attempting to use canRun Callable of MidStunActions enum.");
            return null;
        }

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

    private int playersHealthPercent() {
        double result = ((double) skills.getDynamic(Skill.HITPOINTS) / skills.getStatic(Skill.HITPOINTS)) * 100;
        return  (int) Math.round(result);
    }
}
