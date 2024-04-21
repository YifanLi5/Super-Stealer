package Task.Subclasses;

import UI.ScriptPaint;
import Task.Task;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.EquipmentSlot;

public class EquipDodgyNecklaceTask extends Task {

    private final static String DODGY_NECKLACE = "Dodgy necklace";

    public EquipDodgyNecklaceTask(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return !equipment.isWearingItem(EquipmentSlot.AMULET, DODGY_NECKLACE) && inventory.contains(DODGY_NECKLACE);
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Equipping " + DODGY_NECKLACE);
        ScriptPaint.setStatus("Equipping " + DODGY_NECKLACE);
        equipment.equip(EquipmentSlot.AMULET, DODGY_NECKLACE);
    }
}
