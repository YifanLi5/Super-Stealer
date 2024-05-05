package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.BankAreaUtil;
import Util.PouchUtil;
import Util.RetryUtil;
import Util.StartingEquipmentUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.def.ItemDefinition;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.*;
import java.util.stream.Collectors;

public class BankTask extends Task {

    private static class WalkBackParameters {
        Area returnArea;
        boolean usedWebWalk;
    }

    private final WalkBackParameters walkBackParameters = new WalkBackParameters();
    public BankTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return (inventory.filter(new ActionFilter<>("Eat", "Drink")).isEmpty() && skills.getDynamic(Skill.HITPOINTS) <= 5)
                || inventory.isFull();
    }


    @Override
    public void runTask() throws InterruptedException {
        if(StartingEquipmentUtil.getStartingInventory().keySet().isEmpty()) {
            stopScriptNow("Starting equipment was empty inventory will not restock. Stopping now.");
            return;
        }

        if(!PouchUtil.openPouches()) {
            stopScriptNow("Unable to open-all pouches prior to banking");
            return;
        }

        if(!walkToAndOpenNearestBank()) {
            stopScriptNow("Unable to walk back to nearest bank.");
            return;
        }

        boolean bankingSuccess = bank.isOpen() && depositNonStartingItems() && restoreStartingInventory();
        if(!bankingSuccess) {
            stopScriptNow("Unable to preform banking operations successfully");
            return;
        }
        if(!returnToPickPocketArea())
            stopScriptNow("Unable to return back to pickpocket area");

    }

    private boolean walkToAndOpenNearestBank() throws InterruptedException {
        Area returnArea = myPlayer().getArea(3);
        Entity bankingEntity = bank.closest();
        int bankDistanceToPlayer = bankingEntity == null ?
                Integer.MAX_VALUE :
                myPlayer().getPosition().distance(bankingEntity);
        boolean useWW = bankDistanceToPlayer > 40;
        boolean useWalk = bankDistanceToPlayer > 10 && bankDistanceToPlayer <= 30;

        walkBackParameters.returnArea = returnArea;
        walkBackParameters.usedWebWalk = useWW;

        if(useWW) {
            ScriptPaint.setStatus("Banking :: Webwalking to bank");
            log("Attempting to webwalk to bank.");
            if(!walking.webWalk(BankAreaUtil.getAccessibleBanks(bot.getMethods()))) {
                warn("Failed to webwalk to bank.");
                return false;
            }
        } else if(useWalk || !map.canReach(bankingEntity)) {
            ScriptPaint.setStatus("Banking :: walking to bank");
            log("Attempting to walk to bank entity @ " + bankingEntity.getPosition());
            if(!walking.walk(bankingEntity.getArea(3))) {
                warn("Failed to walk to bank.");
                return false;
            }
        }

        return RetryUtil.retry(() -> bank.open(), 5, 1000);
    }

    private boolean depositNonStartingItems() throws InterruptedException {
        ScriptPaint.setStatus("Banking :: Depositing non-starting items");
        HashMap<ItemDefinition, Integer> startingItems = StartingEquipmentUtil.getStartingInventory();
        Set<Integer> itemIds = startingItems.keySet().stream().mapToInt(ItemDefinition::getId).boxed().collect(Collectors.toSet());
        String[] doNotDepositIfCaseInsensitiveSubstring = {"coin", "rune"};
        ScriptPaint.setStatus("Banking :: Depositing non-starting items");
        return RetryUtil.retry(() ->
                bank.depositAllExcept(
                        item -> itemIds.contains(item.getId()) || Arrays.stream(doNotDepositIfCaseInsensitiveSubstring).anyMatch(item.getName().toLowerCase()::contains)
                ), 3, 1000);
    }

    private boolean restoreStartingInventory() throws InterruptedException {
        log("Need to withdraw...");
        ScriptPaint.setStatus("Banking :: Withdrawing items");
        HashMap<ItemDefinition, Integer> diff = StartingEquipmentUtil.getDifferenceFromStartingInventory();
        StartingEquipmentUtil.logInventoryDefinition(diff);

        String errorMsg = null;
        for(Map.Entry<ItemDefinition, Integer> diffItemDef: diff.entrySet()) {
            if(diffItemDef.getKey().getName().endsWith("rune"))
                continue;
            int itemId = diffItemDef.getKey().getId();
            int amount = diffItemDef.getValue();
            if(amount <= 0)
                continue;
            if(bank.getAmount(itemId) < amount) {
                errorMsg = String.format("Insufficient amount of item %s to continue.", diffItemDef.getKey().getName());
                break;
            }

            if(!RetryUtil.retry(() -> bank.withdraw(itemId, amount), 5, 1000)) {
                errorMsg = String.format(
                        "Failed to withdraw item: %s | amount: %d",
                        diffItemDef.getKey().getName(), diffItemDef.getValue()
                );
                break;
            }
            sleep(600);
        }
        if(errorMsg != null) {
            stopScriptNow(errorMsg);
            return false;
        }
        return true;
    }

    private boolean returnToPickPocketArea() {
        if(bank.isOpen())
            bank.close();

        if(this.walkBackParameters.usedWebWalk) {
            ScriptPaint.setStatus("Banking :: Webwalking back to thieve >:)");
            if(!walking.webWalk(this.walkBackParameters.returnArea)) {
                warn("Failed to webwalk to back to thieve-able NPCs.");
                return false;
            }
        }
        else {
            ScriptPaint.setStatus("Banking :: Walking back to thieve >:)");
            if(!walking.walk(this.walkBackParameters.returnArea)) {
                warn("Failed to walk to back to thieve-able NPCs.");
                return false;
            }
        }
        if(!ConditionalSleep2.sleep(15000, () -> this.walkBackParameters.returnArea.contains(myPosition()))) {
            warn("Player is not back at returnArea after banking.");
            return false;
        }
        return true;

    }
}
