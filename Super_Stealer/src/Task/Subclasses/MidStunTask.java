package Task.Subclasses;

import Task.Task;
import Util.Enums.MidStunActionsEnum;
import Util.MidStunUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MidStunTask extends Task {

    private int eatAtHpPercentage;

    public MidStunTask(Bot bot) {
        super(bot);
        this.eatAtHpPercentage = RngUtil.gaussian(50, 10, 35, 70);
        log("initial eatAtHpPercentage -> " + this.eatAtHpPercentage);
    }

    @Override
    public boolean shouldRun() throws InterruptedException {
        return MidStunUtil.isPlayerStunned() && PickpocketUtil.getPickpocketTarget() != null;
    }

    @Override
    public void runTask() throws InterruptedException {
        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if (pickpocketTarget == null) {
            script.warn("pickpocket target is null even after attempting to re-query");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }
        MidStunActionsEnum action = rollForAction();
        if (action == null) {
            return;
        }

        switch (action) {
            case EAT:
                if (playersHealthPercent() < this.eatAtHpPercentage) {
                    MidStunUtil.eat();
                    if (playersHealthPercent() >= this.eatAtHpPercentage) {
                        this.eatAtHpPercentage = RngUtil.gaussian(50, 15, 35, 70);
                        log("next eatAtHpPercentage -> " + this.eatAtHpPercentage);
                    }
                }
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
                MidStunUtil.prepareMenuHover();
                break;
            case DROP_JUNK:
                MidStunUtil.dropJunk();
                break;
        }

        if (action.isTerminal) {
            ConditionalSleep2.sleep(3000, () -> !MidStunUtil.isPlayerStunned());
        }
    }

    private MidStunActionsEnum rollForAction() {

        List<MidStunActionsEnum> validActions = Arrays.stream(MidStunActionsEnum.values()).filter(action -> {
            try {
                return action.canRun.call();
            } catch (Exception e) {
                stopScriptNow(String.format("Got exception %s get valid mid stun actions.", e.getClass().getSimpleName()));
                return false;
            }
        }).collect(Collectors.toList());

        MidStunActionsEnum selectedAction = null;
        int weightSum = validActions.stream()
                .mapToInt(action -> action.executionWeight)
                .sum();
        int roll = random(1, weightSum);
        for (MidStunActionsEnum action : validActions) {
            roll -= action.executionWeight;
            if (roll <= 0) {
                selectedAction = action;
                break;
            }
        }

        if (selectedAction == null) {
            script.warn("Error: rolling for an action returned null, this should not happen.");
            script.stop(false);
        }
        return selectedAction;
    }

    private int playersHealthPercent() {
        double result = ((double) skills.getDynamic(Skill.HITPOINTS) / skills.getStatic(Skill.HITPOINTS)) * 100;
        return (int) Math.round(result);
    }
}
