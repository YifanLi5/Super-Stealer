package Util;

import org.osbot.rs07.api.def.ItemDefinition;
import org.osbot.rs07.api.model.Item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class StartingEquipmentUtil {
    private static final List<String> itemExclusionList = Arrays.asList("Coin pouch", "Coins", "Jug", "Pie dish");
    private static HashMap<ItemDefinition, Integer> startingInventorySetup = null;

    public static void setStartingInventory() {
        HashMap<ItemDefinition, Integer> invSetup = new HashMap<>();
        Item[] inventory = globalMethodProvider.inventory.getItems();
        for (Item item : inventory) {
            if (item == null || itemExclusionList.contains(item.getName()) || item.getAmount() != 1)
                continue;
            invSetup.compute(item.getDefinition(), (k, v) -> v == null ? 1 : v + 1);
        }
        startingInventorySetup = invSetup;
        globalMethodProvider.log("\nStarting inventory:");
        logInventoryDefinition(startingInventorySetup);
    }


    public static HashMap<ItemDefinition, Integer> getStartingInventory() throws InterruptedException {
        assertStartingInventorySetupNotNull();
        return startingInventorySetup;
    }

    public static HashMap<ItemDefinition, Integer> getDifferenceFromStartingInventory() throws InterruptedException {
        assertStartingInventorySetupNotNull();

        HashMap<ItemDefinition, Integer> diff = new HashMap<>();
        for (Map.Entry<ItemDefinition, Integer> startItemDef : startingInventorySetup.entrySet()) {
            int diffFromStart = startItemDef.getValue() - (int) globalMethodProvider.inventory.getAmount(startItemDef.getKey().getId());
            diff.put(startItemDef.getKey(), diffFromStart);
        }
        return diff;
    }

    public static String[] getNamesOfStartingItems() throws InterruptedException {
        assertStartingInventorySetupNotNull();
        return startingInventorySetup.keySet().stream().map(ItemDefinition::getName).toArray(String[]::new);
    }

    public static void logInventoryDefinition(HashMap<ItemDefinition, Integer> inventoryDefinition) {
        StringBuilder logBuilder = new StringBuilder("*****\n");
        for (Map.Entry<ItemDefinition, Integer> startItemDef : inventoryDefinition.entrySet()) {
            logBuilder.append(String.format("Name: %s | Quantity: %d\n", startItemDef.getKey().getName(), startItemDef.getValue()));
        }
        logBuilder.append("*****");
        globalMethodProvider.log(logBuilder);
    }

    private static void assertStartingInventorySetupNotNull() throws InterruptedException {
        if (startingInventorySetup == null) {
            globalMethodProvider.warn("Error: startingInventory hashmap is null. Did not call setStartingInventory().");
            globalMethodProvider.getBot().getScriptExecutor().stop(false);
        }
    }
}
