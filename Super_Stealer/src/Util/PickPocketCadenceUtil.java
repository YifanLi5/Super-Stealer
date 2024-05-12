package Util;

import static org.osbot.rs07.script.MethodProvider.random;

public class PickPocketCadenceUtil {
    public static final int ppMean;
    public static final int ppStddev;

    static {
        ppMean = random(200, 800);
        int temp = ppMean / random(2, 4);
        ppStddev = random(ppMean - temp, ppMean + temp);
    }
}
