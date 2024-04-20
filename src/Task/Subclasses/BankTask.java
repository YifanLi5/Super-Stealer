package Task.Subclasses;

import Paint.ScriptPaint;
import Task.Task;
import Util.BankUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;

public class BankTask extends Task {
    public BankTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return (inventory.filter(new ActionFilter<>("Eat")).isEmpty())
                || inventory.isFull();
    }

    @Override
    public void runTask() throws InterruptedException {
        ScriptPaint.setStatus("Banking");
        boolean canIBank = false;
        if(bank.closest() == null) {
            log("No banking entity found, attempting to WW to closest.");
            if(walking.webWalk(BankUtil.getAccessibleBanks(bot.getMethods()))) {
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

        //Todo: Change from lobster to user selection in inventory
        if(bank.open()
                && bank.depositAll(item -> item.getName().toLowerCase().endsWith("seed"))
                && bank.withdraw("Lobster", 10) && bank.close())
        {
            log("Banking Success");
        } else {
            log("Unable restock at bank");
            script.stop(LOGOUT_ON_SCRIPT_STOP);
        }
    }
}
