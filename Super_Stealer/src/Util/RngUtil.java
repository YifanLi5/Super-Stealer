package Util;

import java.util.concurrent.ThreadLocalRandom;

import static org.osbot.rs07.script.MethodProvider.random;

public class RngUtil {

    public static final int ppMean;
    public static final int ppStddev;

    static {
        ppMean = random(200, 800);
        ppStddev = ppMean / random(2, 4);
    }

    public static int ppCadenceGaussian() {
        return gaussian(ppMean, ppStddev, 0, 1000);
    }


    public static int gaussian(int mean, int stddev, int lowBound, int highBound) {
        int gaussian = (int) Math.abs((ThreadLocalRandom.current().nextGaussian() * stddev + mean));
        if (gaussian < lowBound)
            gaussian = lowBound;
        else if (gaussian > highBound)
            gaussian = highBound;
        return gaussian;
    }
}
