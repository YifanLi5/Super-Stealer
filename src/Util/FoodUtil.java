package Util;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;

import static Util.GlobalMethodProvider.globalMethodProvider;

public enum FoodUtil {
    TROUT("Trout", 7),
    SALMON("Salmon", 9),
    TUNA("Tuna", 10),
    LOBSTER("Lobster", 12),
    WINE("Jug of wine", 12),
    SWORDFISH("Swordfish", 14),
    MONKFISH("Monkfish", 16);

    private final String itemName;
    private final int healthRestoreAmount;

    FoodUtil(String itemName, int healthRestoreAmount) {
        this.itemName = itemName;
        this.healthRestoreAmount = healthRestoreAmount;
    }

    public static int getHealAmountForFoodItem(Item item) {
        if (item == null)
            return 0;
        String itemName = item.getName();
        FoodUtil matchingEnum = null;
        for (FoodUtil foodEnum : FoodUtil.values()) {
            if (foodEnum.getItemName().equalsIgnoreCase(itemName)) {
                matchingEnum = foodEnum;
                break;
            }
        }
        return matchingEnum == null ? 0 : matchingEnum.healthRestoreAmount;
    }

    public static String[] getAllFoodNames() {
        FoodUtil[] allFoods = FoodUtil.values();
        String[] foodNames = new String[allFoods.length];
        for (int i = 0; i < allFoods.length; i++) {
            foodNames[i] = allFoods[i].getItemName();
        }
        return foodNames;
    }

    public static int getInvSlotContainingFoodWithoutOverheal() {
        int currentHp = globalMethodProvider.skills.getDynamic(Skill.HITPOINTS);
        int maxHp = globalMethodProvider.skills.getStatic(Skill.HITPOINTS);
        boolean found = false;
        int idx = 0;
        Item[] items = globalMethodProvider.inventory.getItems();
        for (; idx < 28; idx++) {
            int healAmount = getHealAmountForFoodItem(items[idx]);
            if (healAmount > 0 && maxHp - currentHp >= healAmount) {
                found = true;
                break;
            }
        }

        return found ? idx : -1;
    }

    public String getItemName() {
        return itemName;
    }

    public int getHealthRestoreAmount() {
        return healthRestoreAmount;
    }

}