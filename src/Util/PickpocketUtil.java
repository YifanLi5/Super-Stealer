package Util;

import org.osbot.rs07.api.def.NPCDefinition;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.event.InteractionEvent;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.awt.*;
import java.util.List;

import static Util.GlobalMethodProvider.methodProvider;

public class PickpocketUtil {

    public static final String PICKPOCKET = "Pickpocket";
    public static List<NPCDefinition> userSelections;

    private static NPC pickpocketTarget;

    public static void initPickpocketUtil(List<NPCDefinition> selections) {
        PickpocketUtil.userSelections = selections;
    }

    public static int[] getSelectedNPCIds() {
        return userSelections.stream().mapToInt(NPCDefinition::getId).toArray();
    }

    public static boolean setPickpocketTarget() throws InterruptedException {
        int attempts = 0;
        while(pickpocketTarget == null || !pickpocketTarget.exists() && attempts < 10) {
            attempts++;
            methodProvider.log(String.format("Npc instance is null. Attempting to locate a new npc instance. %d/10", attempts));
            pickpocketTarget = methodProvider.npcs.closest(PickpocketUtil.getSelectedNPCIds());
            MethodProvider.sleep(500);
        }
        return pickpocketTarget != null && pickpocketTarget.exists();
    }

    public static boolean pickpocketTarget() throws InterruptedException {
        if(pickpocketTarget == null || !pickpocketTarget.exists()) {
            if(!setPickpocketTarget()) {
                return false;
            }
        }
        int attempts = 0;
        boolean interactionSuccessful = false;
        InteractionEvent ppEvent = new InteractionEvent(pickpocketTarget, PICKPOCKET);
        ppEvent.setOperateCamera(false);
        while(!ppEvent.hasFinished() && attempts < 10) {
            methodProvider.execute(ppEvent);
            attempts++;
            interactionSuccessful = ppEvent.hasFinished();
            if(!interactionSuccessful) {
                ppEvent = new InteractionEvent(pickpocketTarget, PICKPOCKET);
                ppEvent.setOperateCamera(false);
            }
        }
        return interactionSuccessful;
    }

    public static boolean isPlayerAdjacentToPickpocketNPC() throws InterruptedException {
        Position myPlayerPosition = methodProvider.myPosition();
        if(pickpocketTarget == null || !pickpocketTarget.exists()) {
            if(!setPickpocketTarget()) {
                return false;
            }
        }

        Position pickpocketNPCPosition = pickpocketTarget.getPosition();
        return myPlayerPosition.getZ() == pickpocketNPCPosition.getZ() &&
                (Math.abs(myPlayerPosition.getX() - pickpocketNPCPosition.getX()) == 1 ^ Math.abs(myPlayerPosition.getY() - pickpocketNPCPosition.getY()) == 1);
    }

    public static boolean menuHoverPickpocketOption() throws InterruptedException {
        if(methodProvider.menu.isOpen()) {
            return methodProvider.menu.getMenu()
                    .stream()
                    .filter(option -> option.action.equals("Pickpocket"))
                    .findFirst()
                    .orElse(null) == null;
        }

        if(!setPickpocketTarget()) {
            methodProvider.warn("Error: Unable to find a pickpocket NPC to menu hover pickpocket option.");
            methodProvider.bot.getScriptExecutor().stop(false);
            return false;
        }
        EntityDestination ed = new EntityDestination(methodProvider.bot, pickpocketTarget);
        if(!methodProvider.mouse.click(ed, true)) {
            methodProvider.log("Unable to right click on pickpocket NPC to menu hover pickpocket option.");
            return false;
        }
        boolean menuOpened = ConditionalSleep2.sleep(1000, () -> methodProvider.menu.isOpen());
        if(!menuOpened) {
            methodProvider.log("Menu is not open.");
            return false;
        }
        List<Option> options = methodProvider.menu.getMenu();

        Rectangle optionRec = options.stream()
                .filter(option -> option.action.equals("Pickpocket"))
                .findFirst()
                .map(option -> methodProvider.menu.getOptionRectangle(options.indexOf(option)))
                .orElse(null);
        if(optionRec == null) {
            methodProvider.log("Opened menu does not contain option to pickpocket");
            return false;
        }
        return methodProvider.mouse.move(new RectangleDestination(methodProvider.bot, optionRec))
                || methodProvider.menu.getMenu()
                .stream()
                .filter(option -> option.action.equals("Pickpocket"))
                .findFirst()
                .orElse(null) == null;
    }
}

