package Util;

import org.osbot.rs07.api.Quests;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// https://osbot.org/forum/topic/167452-bank-utility-for-getting-accessible-banks/
public class BankAreaUtil {

    enum Bank {
        AL_KHARID(Banks.AL_KHARID),
        ARCEUUS_HOUSE(Banks.ARCEUUS_HOUSE),
        ARDOUGNE_NORTH(Banks.ARDOUGNE_NORTH),
        ARDOUGNE_SOUTH(Banks.ARDOUGNE_SOUTH),
        CAMELOT(Banks.CAMELOT),
        CANIFIS(Banks.CANIFIS),
        CASTLE_WARS(Banks.CASTLE_WARS),
        CATHERBY(Banks.CATHERBY),
        DRAYNOR(Banks.DRAYNOR),
        DUEL_ARENA(Banks.DUEL_ARENA),
        EDGEVILLE(Banks.EDGEVILLE),
        FALADOR_EAST(Banks.FALADOR_EAST),
        FALADOR_WEST(Banks.FALADOR_WEST),
        GNOME_STRONGHOLD(Banks.GNOME_STRONGHOLD),
        GRAND_EXCHANGE(Banks.GRAND_EXCHANGE),
        HOSIDIUS_HOUSE(Banks.HOSIDIUS_HOUSE),
        LOVAKENGJ_HOUSE(Banks.LOVAKENGJ_HOUSE),
        LOVAKITE_MINE(Banks.LOVAKITE_MINE),
        LUMBRIDGE_LOWER(Banks.LUMBRIDGE_LOWER),
        LUMBRIDGE_UPPER(Banks.LUMBRIDGE_UPPER),
        PEST_CONTROL(Banks.PEST_CONTROL),
        PISCARILIUS_HOUSE(Banks.PISCARILIUS_HOUSE),
        SHAYZIEN_HOUSE(Banks.SHAYZIEN_HOUSE),
        TZHAAR(Banks.TZHAAR),
        VARROCK_EAST(Banks.VARROCK_EAST),
        VARROCK_WEST(Banks.VARROCK_WEST),
        YANILLE(Banks.YANILLE),
        BARBARIAN_ASSAULT(new Area(2534, 3576, 2537, 3572)),
        BURGH_DE_ROTT(new Area(3496, 3213, 3499, 3210)),
        CRAFTING_GUILD(new Area(2933, 3284, 2936, 3281)),
        ETCETERIA(new Area(2618, 3896, 2620, 3893)),
        FISHING_TRAWLER(new Area(2661, 3162, 2665, 3160)),
        FISHING_GUILD(new Area(2584, 3422, 2588, 3418)),
        GRAND_TREE_WEST(new Area(2440, 3489, 2442, 3487).setPlane(1)),
        GRAND_TREE_SOUTH(new Area(2448, 3482, 2450, 3479).setPlane(1)),
        JATISZO(new Area(2415, 3803, 2418, 3801)),
        KOUREND(new Area(1610, 3683, 1613, 3680).setPlane(2)),
        LLETYA(new Area(2350, 3163, 2354, 3162)),
        LUNAR_ISLE(new Area(2097, 3919, 2102, 3917)),
        LANDS_END(new Area(1508, 3423, 1511, 3419)),
        NARDAH(new Area(3424, 2892, 3430, 2889)),
        NEITIZNOT(new Area(2335, 3808, 2337, 3805)),
        PORT_PHASMATYS(new Area(3686, 3471, 3691, 3463)),
        PISCATORIS(new Area(2327, 3690, 2332, 3687)),
        SHILO_VILLAGE(new Area(2849, 2955, 2855, 2953)),
        SANDCRABS(new Area(1717, 3466, 1722, 3463)),
        SHANTAY_PASS(new Area(3305, 3123, 3308, 3119)),
        SULPHUR_MINE(new Area(1453, 3859, 1458, 3856)),
        TREE_GNOME_STRONGHOLD(new Area(2444, 3427, 2446, 3422).setPlane(1)),
        VINERY(new Area(1802, 3571, 1808, 3571)),
        WARRIORS_GUILD(new Area(2843, 3544, 2846, 3539)),
        WOODCUTTING_GUILD(new Area(1589, 3480, 1593, 3476)),
        ZEAH_COOKING(new Area(1653, 3613, 1658, 3607));

        private final Area area;

        Bank(final Area area) {
            this.area = area;
        }

        public Area getArea() {
            return area;
        }
    }


    public static Area[] getBankAreas() {
        return Arrays.stream(Bank.values()).map(Bank::getArea).toArray(Area[]::new);
    }

    public static Area[] getAccessibleBanks(final MethodProvider api) {
        List<Area> accessibleBanks = new ArrayList<>();

        // f2p
        accessibleBanks.add(Bank.AL_KHARID.getArea());
        accessibleBanks.add(Bank.CASTLE_WARS.getArea());
        accessibleBanks.add(Bank.DRAYNOR.getArea());
        accessibleBanks.add(Bank.DUEL_ARENA.getArea());
        accessibleBanks.add(Bank.EDGEVILLE.getArea());
        accessibleBanks.add(Bank.FALADOR_EAST.getArea());
        accessibleBanks.add(Bank.FALADOR_WEST.getArea());
        accessibleBanks.add(Bank.GRAND_EXCHANGE.getArea());
        accessibleBanks.add(Bank.LUMBRIDGE_UPPER.getArea());
        accessibleBanks.add(Bank.VARROCK_EAST.getArea());
        accessibleBanks.add(Bank.VARROCK_WEST.getArea());
        accessibleBanks.add(Bank.SHANTAY_PASS.getArea());

        if (api.getSkills().getVirtualLevel(Skill.CRAFTING) >= 40 && api.getEquipment().isWearingItem(EquipmentSlot.CHEST, "Brown apron")) {
            accessibleBanks.add(Bank.CRAFTING_GUILD.getArea());
        }

        // p2p
        if (api.getClient().isMember() && api.getWorlds().isMembersWorld()) {
            accessibleBanks.add(Bank.ARCEUUS_HOUSE.getArea());
            accessibleBanks.add(Bank.ARDOUGNE_NORTH.getArea());
            accessibleBanks.add(Bank.ARDOUGNE_SOUTH.getArea());
            accessibleBanks.add(Bank.CAMELOT.getArea());
            accessibleBanks.add(Bank.CATHERBY.getArea());
            accessibleBanks.add(Bank.GNOME_STRONGHOLD.getArea());
            accessibleBanks.add(Bank.HOSIDIUS_HOUSE.getArea());
            accessibleBanks.add(Bank.LOVAKENGJ_HOUSE.getArea());
            accessibleBanks.add(Bank.LOVAKITE_MINE.getArea());
            accessibleBanks.add(Bank.PEST_CONTROL.getArea());
            accessibleBanks.add(Bank.PISCARILIUS_HOUSE.getArea());
            accessibleBanks.add(Bank.SHAYZIEN_HOUSE.getArea());
            accessibleBanks.add(Bank.TZHAAR.getArea());
            accessibleBanks.add(Bank.YANILLE.getArea());
            accessibleBanks.add(Bank.BARBARIAN_ASSAULT.getArea());
            accessibleBanks.add(Bank.VINERY.getArea());
            accessibleBanks.add(Bank.FISHING_TRAWLER.getArea());
            accessibleBanks.add(Bank.KOUREND.getArea());
            accessibleBanks.add(Bank.ZEAH_COOKING.getArea());
            accessibleBanks.add(Bank.LANDS_END.getArea());
            accessibleBanks.add(Bank.NARDAH.getArea());
            accessibleBanks.add(Bank.SANDCRABS.getArea());
            accessibleBanks.add(Bank.SULPHUR_MINE.getArea());

            if (api.getQuests().isComplete(Quests.Quest.PRIEST_IN_PERIL)) {
                accessibleBanks.add(Bank.PORT_PHASMATYS.getArea());
                accessibleBanks.add(Bank.CANIFIS.getArea());
            }

            if (api.getQuests().isStarted(Quests.Quest.IN_AID_OF_THE_MYREQUE)) {
                accessibleBanks.add(Bank.BURGH_DE_ROTT.getArea());
            }

            if (api.getQuests().isComplete(Quests.Quest.THE_FREMENNIK_TRIALS)) {
                accessibleBanks.add(Bank.ETCETERIA.getArea());
                if (api.getQuests().isStarted(Quests.Quest.THE_FREMENNIK_ISLES)) {
                    accessibleBanks.add(Bank.JATISZO.getArea());
                    accessibleBanks.add(Bank.NEITIZNOT.getArea());
                }
            }

            if (api.getQuests().isComplete(Quests.Quest.SHILO_VILLAGE)) {
                accessibleBanks.add(Bank.SHILO_VILLAGE.getArea());
            }

            if (api.getQuests().isStarted(Quests.Quest.MOURNINGS_END_PART_I)) {
                accessibleBanks.add(Bank.LLETYA.getArea());
            }

            if (api.getQuests().isComplete(Quests.Quest.SWAN_SONG)) {
                accessibleBanks.add(Bank.PISCATORIS.getArea());
            }

            if (api.getSkills().getVirtualLevel(Skill.FISHING) >= 68) {
                accessibleBanks.add(Bank.FISHING_GUILD.getArea());
            }

            if (api.getSkills().getVirtualLevel(Skill.ATTACK) + api.getSkills().getVirtualLevel(Skill.STRENGTH) >= 130) {
                accessibleBanks.add(Bank.WARRIORS_GUILD.getArea());
            }

            if (api.getSkills().getVirtualLevel(Skill.WOODCUTTING) >= 60) {
                accessibleBanks.add(Bank.WOODCUTTING_GUILD.getArea());
            }

            // First time entering minigame(?) Need to figure this out.
            if (false) {
                accessibleBanks.add(Bank.GRAND_TREE_WEST.getArea());
                accessibleBanks.add(Bank.GRAND_TREE_SOUTH.getArea());
                accessibleBanks.add(Bank.TREE_GNOME_STRONGHOLD.getArea());
            }

            // Only needs partial completion, need to figure out the check for it.
            if (api.getQuests().isComplete(Quests.Quest.LUNAR_DIPLOMACY)) {
                accessibleBanks.add(Bank.LUNAR_ISLE.getArea());
            }

            // Only needs one part finished, need to figure out the check for that as well.
            if (api.getQuests().isStarted(Quests.Quest.RECIPE_FOR_DISASTER)) {
                accessibleBanks.add(Bank.LUMBRIDGE_LOWER.getArea());
            }
        }

        return accessibleBanks.toArray(new Area[0]);
    }

}