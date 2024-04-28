package Util;

import UI.ScriptPaint;
import com.sun.org.apache.bcel.internal.generic.RET;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.utility.ConditionalSleep2;

import static Task.Task.LOGOUT_ON_SCRIPT_STOP;
import static Util.GlobalMethodProvider.globalMethodProvider;
import static org.osbot.rs07.script.MethodProvider.sleep;

public class MidStunUtil {

    private final static String[] junk = {"Jug", "Bowl", "Vial"};
    public static int approxVerticesCountStunned;

    public static boolean isPlayerStunned() {
        return approxVerticesCountStunned - globalMethodProvider.myPlayer().getModel().getVerticesCount() <= 5;
    }

    public static void eat() throws InterruptedException {
        int nextFoodSlot = FoodUtil.getInvSlotContainingFoodWithoutOverheal();
        if(nextFoodSlot == -1) {
            globalMethodProvider.log("Using healing item not in FoodUtil, May overheal");
            nextFoodSlot = globalMethodProvider.inventory.getSlot(new ActionFilter<>("Eat", "Drink"));
            if(nextFoodSlot == -1) {
                globalMethodProvider.log("Unable to find an inventory slot containing food. Aborting eat/drink.");
                return;
            }
        }
        ScriptPaint.setStatus("MidStun - Eating");

        if(!globalMethodProvider.inventory.interact(nextFoodSlot, "Eat", "Drink")) {
            globalMethodProvider.warn(String.format("Error: Unable to use item in slot (%d) to heal.", nextFoodSlot));
            globalMethodProvider.getBot().getScriptExecutor().stop(LOGOUT_ON_SCRIPT_STOP);
            return;
        }
        int lambdaTemp = nextFoodSlot;
        ConditionalSleep2.sleep(2000, () -> globalMethodProvider.inventory.getItemInSlot(lambdaTemp) == null);

        NPC pickpocketTarget = PickpocketUtil.getPickpocketTarget();
        if(pickpocketTarget == null) {
            globalMethodProvider.warn("Error: pickpocket target is null even after attempting to re-query");
            globalMethodProvider.getBot().getScriptExecutor().stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }

    public static void no_op() {
        ScriptPaint.setStatus("MidStun - no_op");
        ConditionalSleep2.sleep(3000, () -> !MidStunUtil.isPlayerStunned());
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
        while(isPlayerStunned()) {
            PickpocketUtil.pickpocketTarget();
            sleep(RngUtil.gaussian(325, 50, 0, 450) );
        }
    }

    public static void prepareMenuHover() throws InterruptedException {
        ScriptPaint.setStatus("MidStun - hover menu option");
        if(!PickpocketUtil.menuHoverPickpocketOption())
            globalMethodProvider.log("Pickpocket menu hover failed :(");
    }

    public static void dropJunk() {
        if(globalMethodProvider.inventory.contains(junk))
            globalMethodProvider.inventory.dropAll(junk);
    }


}
