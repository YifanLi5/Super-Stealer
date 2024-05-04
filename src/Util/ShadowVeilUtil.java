package Util;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;

import java.util.Arrays;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class ShadowVeilUtil implements MessageListener {

    public static boolean isSvOffCooldown = true;
    private static final String[] fireRune = {"Fire rune", "Lava rune", "Smoke rune", "Steam rune"};
    private static final String[] earthRune = {"Earth rune", "Lava rune", "Dust rune", "Mud rune"};

    private static MessageListener messageListenerInstance;

    public static MessageListener initMessageListener(Bot bot) {
        if(messageListenerInstance == null) {
            messageListenerInstance = new ShadowVeilUtil();
            bot.addMessageListener(messageListenerInstance);
        }

        return messageListenerInstance;
    }

    @Override
    public void onMessage(Message message) {
        if(message.getType() != Message.MessageType.GAME)
            return;
        String msg = message.getMessage();
        if(msg.contains("Your Shadow Veil")) {
            globalMethodProvider.log("shadow veil is ready.");
            isSvOffCooldown = true;
        } else if (msg.contains("Your thieving abilities")) {
            isSvOffCooldown = false;
        }
    }

    public static int getNumShadowVeilCasts() throws InterruptedException {
        if(!canCastSV())
            return 0;

        int fireRuneSource = globalMethodProvider.inventory.contains(fireRune) ? (int) globalMethodProvider.inventory.getAmount(fireRune) : Integer.MAX_VALUE;
        int earthRuneSource = globalMethodProvider.inventory.contains(earthRune) ? (int) globalMethodProvider.inventory.getAmount(earthRune) : Integer.MAX_VALUE;
        return Arrays.stream(
                new int[]{(int) globalMethodProvider.inventory.getAmount("Cosmic rune"), fireRuneSource, earthRuneSource}
        ).map(num -> num / 5).min().getAsInt();
    }

    //placeholder for magic.canCast(SHADOW_VEIL), it doesn't work atm.
    public static boolean canCastSV() {
        if(!globalMethodProvider.magic.open()) {
            globalMethodProvider.log("Unable to open magic tab");
            return false;
        }
        RS2Widget shadowVeilSpellWidget = globalMethodProvider.widgets.singleFilter(218, rs2Widget -> rs2Widget.getSpellName().contains("Shadow Veil"));
        if(shadowVeilSpellWidget.getSpriteIndex1() == 1334 || globalMethodProvider.skills.getStatic(Skill.MAGIC) < 47) {
            globalMethodProvider.log("Unable to cast Shadow veil, Widget is using blacked out sprite or < 47 magic, ");
            return false;
        }
        return true;
    }
}
