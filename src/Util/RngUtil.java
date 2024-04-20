package Util;

import java.util.concurrent.ThreadLocalRandom;

public class RngUtil {

    public static int gaussian(int mean, int stddev, int lowBound, int highBound) {
        int gaussian = (int) Math.abs((ThreadLocalRandom.current().nextGaussian() * stddev + mean));
        if(gaussian < lowBound)
            gaussian = lowBound;
        else if(gaussian > highBound)
            gaussian = highBound;
        return gaussian;
    }
}
