package Util;

import UI.ScriptPaint;
import Util.Enums.FoodEnum;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.concurrent.Callable;

import static Task.Task.LOGOUT_ON_SCRIPT_STOP;
import static Util.GlobalMethodProvider.globalMethodProvider;
import static org.osbot.rs07.script.MethodProvider.sleep;

public class MidStunUtil {
    public final static String[] junk = {"Jug", "Bowl", "Vial", "Pie dish"};
    public static int approxVerticesCountStunned;

    public static boolean isPlayerStunned() {
        return approxVerticesCountStunned - globalMethodProvider.myPlayer().getModel().getVerticesCount() <= 5;
    }

    public static void eat() throws InterruptedException {
        int nextFoodSlot = FoodEnum.getInvSlotContainingFoodWithoutOverheal();
        if (nextFoodSlot == -1) {
            globalMethodProvider.log("Using healing item not in FoodUtil, May overheal");
            nextFoodSlot = globalMethodProvider.inventory.getSlot(new ActionFilter<>("Eat", "Drink"));
            if (nextFoodSlot == -1) {
                globalMethodProvider.log("Unable to find an inventory slot containing food. Aborting eat/drink.");
                return;
            }
        }
        ScriptPaint.setStatus("MidStun - Eating");

        final int itemId_b4Interact = globalMethodProvider.inventory.getItemInSlot(nextFoodSlot).getId();
        final int finalNextFoodSlot = nextFoodSlot;
        final Callable<Boolean> eatItem = () -> globalMethodProvider.inventory.interact(finalNextFoodSlot, "Eat", "Drink");

        if (!RetryUtil.retry(eatItem, 5, 600)) {
            globalMethodProvider.warn(String.format("Error: Unable to use item in slot (%d) to heal.", nextFoodSlot));
            globalMethodProvider.getBot().getScriptExecutor().stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }

        ConditionalSleep2.sleep(2000, () -> {
            Item item = globalMethodProvider.inventory.getItemInSlot(finalNextFoodSlot);
            return item == null || item.getId() != itemId_b4Interact;
        });
    }

    public static void no_op() {
        ScriptPaint.setStatus("MidStun - no_op");
        ConditionalSleep2.sleep(5000, () -> !MidStunUtil.isPlayerStunned());
    }

    public static void extendedNo_op() throws InterruptedException {
        int sleepTime = RngUtil.gaussian(6500, 500, 3000, 9000);
        String msg = String.format("MidStun - Long AFK (%dms)", sleepTime);
        globalMethodProvider.log(msg);
        ScriptPaint.setStatus(msg);
        globalMethodProvider.mouse.moveOutsideScreen();
        sleep(sleepTime);
    }

    public static void spamPickpocket() throws InterruptedException {
        ScriptPaint.setStatus("MidStun - spam pickpocket");
        while (isPlayerStunned()) {
            PickpocketUtil.pickpocketTarget();
            sleep(RngUtil.ppCadenceGaussian());
        }
    }

    public static void prepareMenuHover() throws InterruptedException {
        ScriptPaint.setStatus("MidStun - hover menu option");
        if (!PickpocketUtil.menuHoverPickpocketOption())
            globalMethodProvider.log("Pickpocket menu hover failed :(");
    }

    public static void dropJunk() {
        if (globalMethodProvider.inventory.contains(junk)) {
            ScriptPaint.setStatus("MidStun - drop junk");
            globalMethodProvider.inventory.dropAll(junk);
        }
    }
}
