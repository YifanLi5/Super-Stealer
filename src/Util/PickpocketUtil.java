package Util;

import org.osbot.rs07.api.Diaries;
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
import java.util.concurrent.Callable;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class PickpocketUtil {

    public static final String PICKPOCKET = "Pickpocket";
    public static List<NPCDefinition> userSelections;


    private static NPC pickpocketTarget;

    public static void initPickpocketUtil(List<NPCDefinition> selections) {
        PickpocketUtil.userSelections = selections;
    }

    public static NPC getPickpocketTarget() throws InterruptedException {
        if(pickpocketTarget == null || !pickpocketTarget.exists()) {
            if(!setPickpocketTarget()) {
                return null;
            }
        }
        return pickpocketTarget;
    }

    public static int[] getSelectedNPCIds() {
        return userSelections.stream().mapToInt(NPCDefinition::getId).toArray();
    }

    public static boolean setPickpocketTarget() throws InterruptedException {
        int attempts = 0;
        while((pickpocketTarget == null || !pickpocketTarget.exists()) && attempts < 10) {
            attempts++;
            globalMethodProvider.log(String.format("Npc instance is null. Attempting to locate a new npc instance. %d/10", attempts));
            pickpocketTarget = globalMethodProvider.npcs.closest(PickpocketUtil.getSelectedNPCIds());
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
            globalMethodProvider.execute(ppEvent);
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
        Position myPlayerPosition = globalMethodProvider.myPosition();
        if(pickpocketTarget == null || !pickpocketTarget.exists()) {
            if(!setPickpocketTarget()) {
                return false;
            }
        }

        Position pickpocketNPCPosition = pickpocketTarget.getPosition();
        boolean isAdjacent = myPlayerPosition.getZ() == pickpocketNPCPosition.getZ() &&
                (Math.abs(myPlayerPosition.getX() - pickpocketNPCPosition.getX()) == 1 ^ Math.abs(myPlayerPosition.getY() - pickpocketNPCPosition.getY()) == 1);

        return isAdjacent || myPlayerPosition.equals(pickpocketNPCPosition);
    }

    public static boolean menuHoverPickpocketOption() throws InterruptedException {
        if(globalMethodProvider.menu.isOpen()) {
            return globalMethodProvider.menu.getMenu()
                    .stream()
                    .filter(option -> option.action.equals("Pickpocket"))
                    .findFirst()
                    .orElse(null) != null;
        }

        if(!setPickpocketTarget()) {
            globalMethodProvider.warn("Error: Unable to find a pickpocket NPC to menu hover pickpocket option.");
            globalMethodProvider.bot.getScriptExecutor().stop(false);
            return false;
        }
        EntityDestination ed = new EntityDestination(globalMethodProvider.bot, pickpocketTarget);
        if(!globalMethodProvider.mouse.click(ed, true)) {
            globalMethodProvider.log("Unable to right click on pickpocket NPC to menu hover pickpocket option.");
            return false;
        }
        boolean menuOpened = ConditionalSleep2.sleep(1000, () -> globalMethodProvider.menu.isOpen());
        if(!menuOpened) {
            globalMethodProvider.log("Menu is not open.");
            return false;
        }
        List<Option> options = globalMethodProvider.menu.getMenu();

        Rectangle optionRec = options.stream()
                .filter(option -> option.action.equals("Pickpocket"))
                .findFirst()
                .map(option -> globalMethodProvider.menu.getOptionRectangle(options.indexOf(option)))
                .orElse(null);
        if(optionRec == null) {
            globalMethodProvider.log("Opened menu does not contain option to pickpocket");
            Rectangle menuRectangle = globalMethodProvider.menu.getRectangle();

            Point currentMousePosition = globalMethodProvider.mouse.getPosition();
            globalMethodProvider.mouse.move(
                    (int) (currentMousePosition.getX() + MethodProvider.random(-100, 100)),
                    menuRectangle.y + MethodProvider.random(50)
            );
            return false;
        }
        return globalMethodProvider.mouse.move(new RectangleDestination(globalMethodProvider.bot, optionRec))
                || globalMethodProvider.menu.getMenu()
                .stream()
                .filter(option -> option.action.equals("Pickpocket"))
                .findFirst()
                .orElse(null) != null;
    }

    public static int getMaxPossibleCoinPouchStack() {
        if(globalMethodProvider.diaries.isComplete(Diaries.Diary.ARDOUGNE_ELITE))
            return 140;
        else if(globalMethodProvider.diaries.isComplete(Diaries.Diary.ARDOUGNE_HARD))
            return 84;
        else if(globalMethodProvider.diaries.isComplete(Diaries.Diary.ARDOUGNE_MEDIUM))
            return 56;
        else
            return 28;
    }
}