package Util;

import org.osbot.rs07.api.def.ItemDefinition;
import org.osbot.rs07.api.model.Item;

import java.util.HashMap;
import java.util.Map;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class StartingEquipmentUtil {
    private static HashMap<ItemDefinition, Integer> startingInventorySetup = null;

    public static HashMap<ItemDefinition, Integer> getCurrentInventorySetup() {
        HashMap<ItemDefinition, Integer> invSetup = new HashMap<>();
        Item[] inventory = globalMethodProvider.inventory.getItems();
        for(Item item: inventory) {
            // Coin pouch is not bankable, will cause issues with BankTask if user starts script with them present.
            if(item == null || item.getName().equals("Coin pouch") || item.getName().equals("Coins"))
                continue;
            invSetup.compute(item.getDefinition(), (k, v) -> v == null ? 1 : v + 1);
        }
        return invSetup;
    }

    public static void setStartingInventory() {
        startingInventorySetup = getCurrentInventorySetup();
        globalMethodProvider.log("Starting inventory setup");
        logInventoryDefinition(startingInventorySetup);
    }

    public static HashMap<ItemDefinition, Integer> getStartingInventory() throws InterruptedException {
        assertStartingInventorySetupNotNull();
        return startingInventorySetup;
    }

    public static HashMap<ItemDefinition, Integer> findInvDiff() throws InterruptedException {
        assertStartingInventorySetupNotNull();

        HashMap<ItemDefinition, Integer> diff = new HashMap<>();
        HashMap<ItemDefinition, Integer> currentInvSetup = getCurrentInventorySetup();
        for(Map.Entry<ItemDefinition, Integer> startItemDef: startingInventorySetup.entrySet()) {
            int amountInCurrent = currentInvSetup.getOrDefault(startItemDef.getKey(), 0);
            int diffFromStart = startItemDef.getValue() - amountInCurrent;
            diff.put(startItemDef.getKey(), diffFromStart);
        }
        return diff;
    }

    public static String[] getNamesOfStartingItems() throws InterruptedException {
        assertStartingInventorySetupNotNull();
        return startingInventorySetup.keySet().stream().map(ItemDefinition::getName).toArray(String[]::new);
    }

    public static void logInventoryDefinition(HashMap<ItemDefinition, Integer> inventoryDefinition) {
        StringBuilder logBuilder = new StringBuilder("\n");
        for(Map.Entry<ItemDefinition, Integer> startItemDef: inventoryDefinition.entrySet()) {
            logBuilder.append(String.format("Name: %s | Quantity: %d\n", startItemDef.getKey().getName(), startItemDef.getValue()));
        }
        globalMethodProvider.log(logBuilder);
    }

    private static void assertStartingInventorySetupNotNull() throws InterruptedException {
        if(startingInventorySetup == null) {
            globalMethodProvider.warn("Error: startingInventory hashmap is null. Did not call setStartingInventory().");
            globalMethodProvider.getBot().getScriptExecutor().stop(false);
        }
    }
}
