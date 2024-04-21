package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import Util.FindBankUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;

public class BankTask extends Task {
    public BankTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return (inventory.filter(new ActionFilter<>("Eat", "Drink")).isEmpty())
                || inventory.isFull();
    }

    @Override
    public void runTask() throws InterruptedException {


        ScriptPaint.setStatus("Banking");
        boolean canIBank = false;
        if(bank.closest() == null) {
            log("No banking entity found, attempting to WW to closest.");
            if(walking.webWalk(FindBankUtil.getAccessibleBanks(bot.getMethods()))) {
                canIBank = bank.closest() != null;
                log("canIBank: " + canIBank);
            }
        } else {
            log("Banking entity found.");
            canIBank = true;
        }
        if(!canIBank) {
           log("Unable find a banking entity");
           script.stop(LOGOUT_ON_SCRIPT_STOP);
        }

        //If stuck in pickpocket anim, bank.open will return false.
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
        }

        if(bank.isOpen())
            bank.close();

    }
}
