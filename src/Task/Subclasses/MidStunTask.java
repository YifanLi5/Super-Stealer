package Task.Subclasses;

import Task.Task;
import Util.MidStunUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.HashSet;

public class MidStunTask extends Task {

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

        switch (rollForAction()) {
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
                ConditionalSleep2.sleep(3000, () -> !MidStunUtil.isPlayerStunned());
                break;
            case EXTENDED_NO_OP:
                MidStunUtil.extendedNo_op();
                ConditionalSleep2.sleep(3000, () -> !MidStunUtil.isPlayerStunned());
                break;
            case SPAM_PICKPOCKET:
                MidStunUtil.spamPickpocket();
                ConditionalSleep2.sleep(3000, () -> !MidStunUtil.isPlayerStunned());
                break;
            case PREPARE_MENU_HOVER:
                MidStunUtil.prepareMenuHover();
                ConditionalSleep2.sleep(3000, () -> !MidStunUtil.isPlayerStunned());
                break;
            case DROP_JUNK:
                MidStunUtil.dropJunk();
                break;
        }
    }

    private MidStunActions rollForAction() {
        if(myPlayer().getHealthPercentCache() < 65) {
            validActions.add(MidStunActions.EAT);
        }
        else validActions.remove(MidStunActions.EAT);

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

    private int playersHealthPercent() {
        double result = ((double) skills.getDynamic(Skill.HITPOINTS) / skills.getStatic(Skill.HITPOINTS)) * 100;
        return  (int) Math.round(result);
    }
}
