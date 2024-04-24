package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.BankAreaUtil;
import Util.PouchUtil;
import Util.StartingEquipmentUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.def.ItemDefinition;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static Task.Subclasses.OpenCoinPouchesTask.COIN_POUCH;

public class BankTask extends Task {
    public BankTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return (inventory.filter(new ActionFilter<>("Eat", "Drink")).isEmpty() && skills.getDynamic(Skill.HITPOINTS) < 30)
                || inventory.isFull();
    }


    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Banking");
        if(!PouchUtil.openPouches()) {
            stopScriptNow("Unable to open-all pouches prior to banking");
            return;
        }

        // if bank is far away -> webwalk
        // if bank is close but not too close -> walk
        // otherwise canReach, doorHandler, and bank.open()
        Area returnArea = myPlayer().getArea(3);
        Entity bankingEntity = bank.closest();
        int bankDistanceToPlayer = bankingEntity == null ?
                Integer.MAX_VALUE :
                myPlayer().getPosition().distance(bankingEntity);
        boolean useWW = bankDistanceToPlayer > 40;
        boolean useWalk = bankDistanceToPlayer > 20 && bankDistanceToPlayer <= 40;

        if(useWW) {
            ScriptPaint.setStatus("Webwalking to bank");
            log("Attempting to webwalk to bank.");
            if(!walking.webWalk(BankAreaUtil.getAccessibleBanks(bot.getMethods()))) {
                stopScriptNow("Failed to webwalk to bank.");
                return;
            }
        } else if(useWalk || !map.canReach(bankingEntity)) {
            ScriptPaint.setStatus("walking to bank");
            log("Attempting to walk to bank entity @ " + bankingEntity.getPosition());
            if(!walking.walk(bankingEntity.getArea(3))) {
                stopScriptNow("Failed to walk to bank.");
                return;
            }
        }

        // If in pickpocket anim, player will be stuck and bank.open will return false.
        int attempts = 0;
        while(attempts < 5 && !bank.isOpen()) {
            bank.open();
            attempts++;
        }

        if(bank.isOpen() && bank.depositAll()) {
            HashMap<ItemDefinition, Integer> invDiffToStart = StartingEquipmentUtil.findInvDiff();
            log("diff");
            StartingEquipmentUtil.logInventoryDefinition(invDiffToStart);
            for(Map.Entry<ItemDefinition, Integer> diffItemDef: invDiffToStart.entrySet()) {
                int itemId = diffItemDef.getKey().getId();
                int amount = diffItemDef.getValue();
                if(amount <= 0)
                    continue;
                if(bank.getAmount(itemId) < amount) {
                    stopScriptNow(String.format("Insufficient amount of item %s to continue.", diffItemDef.getKey().getName()));
                    break;
                }
                if(!bank.withdraw(itemId, amount)) {
                    stopScriptNow(String.format("Unable to withdraw item: %s | amount: %d.", diffItemDef.getKey().getName(), diffItemDef.getValue()));
                    break;
                }
                sleep(600);
            }
        }

        if(attempts >= 5) {
            stopScriptNow("Unable restock at bank");
            return;
        }

        if(bank.isOpen())
            bank.close();

        if(useWW) {
            ScriptPaint.setStatus("Webwalking back to thieve >:)");
            if(!walking.webWalk(returnArea)) {
                stopScriptNow("Failed to webwalk to back to thieve-able NPCs.");
            }
        }
        else if(useWalk){
            ScriptPaint.setStatus("Walking back to thieve >:)");
            if(!walking.walk(returnArea)) {
                stopScriptNow("Failed to walk to back to thieve-able NPCs.");
            }
        }
        if(!ConditionalSleep2.sleep(20000, () -> returnArea.contains(myPosition()))) {
            stopScriptNow("Player is not back at returnArea after banking.");
        }
    }
}
