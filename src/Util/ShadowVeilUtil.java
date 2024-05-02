package Util;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.listener.MessageListener;

import java.util.Arrays;

import static Util.GlobalMethodProvider.globalMethodProvider;

public class ShadowVeilUtil implements MessageListener {

    public static boolean canCastSV = true;
    private static final String[] fireRune = {"Fire rune", "Lava rune"};
    private static final String[] earthRune = {"Earth rune", "Lava rune"};

    @Override
    public void onMessage(Message message) {
        if(message.getType() != Message.MessageType.GAME)
            return;
        String msg = message.getMessage();
        if(msg.contains("Your Shadow Veil")) {
            globalMethodProvider.log("shadow veil is ready.");
            canCastSV = true;
        } else if (msg.contains("Your thieving abilities")) {
            canCastSV = false;
        }
    }

    public static int getNumShadowVeilCasts() throws InterruptedException {
        if(!globalMethodProvider.magic.canCast(Spells.ArceuusSpells.SHADOW_VEIL)) {
            return 0;
        }
        int fireRuneSource = globalMethodProvider.inventory.contains(fireRune) ? (int) globalMethodProvider.inventory.getAmount(fireRune) : Integer.MAX_VALUE;
        int earthRuneSource = globalMethodProvider.inventory.contains(earthRune) ? (int) globalMethodProvider.inventory.getAmount(earthRune) : Integer.MAX_VALUE;
        return Arrays.stream(
                new int[]{(int) globalMethodProvider.inventory.getAmount("Cosmic rune"), fireRuneSource, earthRuneSource}
        ).map(num -> num / 5).min().getAsInt();
    }
}
