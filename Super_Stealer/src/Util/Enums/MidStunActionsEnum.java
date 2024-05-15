package Util.Enums;

import Util.MidStunUtil;
import Util.PickpocketUtil;
import Util.RngUtil;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.NPC;

import java.util.concurrent.Callable;

import static Util.GlobalMethodProvider.globalMethodProvider;

public enum MidStunActionsEnum {
    EAT(RngUtil.gaussian(1000, 250, 300, 1700), () ->
            globalMethodProvider.myPlayer().getHealthPercentCache() < 65
                    && globalMethodProvider.inventory.contains(new ActionFilter<>("Eat", "Drink"))
            , false
    ),
    NO_OP(RngUtil.gaussian(250, 100, 0, 500), () -> true, true),
    EXTENDED_NO_OP(RngUtil.gaussian(50, 25, 0, 150), () -> true, true),
    SPAM_PICKPOCKET(RngUtil.gaussian(750, 75, 450, 1050), () -> true, true),
    PREPARE_MENU_HOVER(RngUtil.gaussian(750, 75, 450, 1050), () -> {
        // Do not hover Ardy knights, too large menu if too many people.
        NPC target = PickpocketUtil.getPickpocketTarget();
        return target != null && !target.getName().equalsIgnoreCase("Knight of Ardougne");
    }, true),
    DROP_JUNK(RngUtil.gaussian(1000, 250, 300, 1700), () ->
            globalMethodProvider.inventory.contains(MidStunUtil.junk)
            , false);

    public final int executionWeight;
    public final Callable<Boolean> canRun;
    public final boolean isTerminal;

    MidStunActionsEnum(int executionWeight, Callable<Boolean> canRun, boolean isTerminal) {
        this.executionWeight = executionWeight;
        this.canRun = canRun;
        this.isTerminal = isTerminal;
    }
}
