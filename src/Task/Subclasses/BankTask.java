package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.BankAreaUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Entity;

public class BankTask extends Task {
    public BankTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return (inventory.filter(new ActionFilter<>("Eat", "Drink")).isEmpty())
                || inventory.isFull();
    }


    //Todo: Split function into static methods in BankUtil
    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Banking");
        // if bank is far away -> webwalk
        // if bank is close but not too close -> walk
        // otherwise just use bank.open()
        Area returnArea = myPlayer().getArea(7);
        Entity bankingEntity = bank.closest();
        int bankDistanceToPlayer = bankingEntity == null ? Integer.MAX_VALUE : myPlayer().getPosition().distance(bankingEntity);
        boolean useWW = bankDistanceToPlayer > 40;
        boolean useWalk = bankDistanceToPlayer > 20 && bankDistanceToPlayer <= 40;

        if(useWW) {
            ScriptPaint.setStatus("Webwalking to bank");
            log("Attempting to webwalk to bank.");
            if(!walking.webWalk(BankAreaUtil.getAccessibleBanks(bot.getMethods()))) {
                script.warn("Error: Failed to webwalk to bank.");
                script.stop(LOGOUT_ON_SCRIPT_STOP);
                Task.stopScriptNow = true;
                return;
            }
        } else if(useWalk) {
            ScriptPaint.setStatus("walking to bank");
            log("Attempting to walk to bank entity @ " + bankingEntity.getPosition());
            if(!walking.walk(bankingEntity.getArea(5))) {
                script.warn("Error: Failed to walk to bank.");
                script.stop(LOGOUT_ON_SCRIPT_STOP);
                Task.stopScriptNow = true;
                return;
            }
        }

        //If in pickpocket anim, bank.open will return false. Need multiple tries.
        int attempts = 0;
        while(attempts < 5) {
            //Todo: Change from lobster to user selection in inventory
            if(bank.open()
                    && bank.depositAll(item -> item.getName().toLowerCase().endsWith("seed")) //For master farmers
                    && bank.withdraw("Jug of wine", 10))
            {
                log("Banking Success");
                break;
            }
            attempts++;
            sleep(1000);
        }

        if(attempts >= 5) {
            log("Unable restock at bank");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
            Task.stopScriptNow = true;
            return;
        }

        if(bank.isOpen())
            bank.close();

        if(useWW) {
            ScriptPaint.setStatus("Webwalking back to thieve >:)");
            if(!walking.webWalk(returnArea)) {
                script.warn("Error: Failed to webwalk to back to thieve-able NPCs.");
                script.stop(LOGOUT_ON_SCRIPT_STOP);
                Task.stopScriptNow = true;
            }
        }
        else if(useWalk){
            ScriptPaint.setStatus("Walking back to thieve >:)");
            if(!walking.walk(returnArea)) {
                script.warn("Error: Failed to walk to back to thieve-able NPCs.");
                script.stop(LOGOUT_ON_SCRIPT_STOP);
                Task.stopScriptNow = true;
            }
        }
    }
}
