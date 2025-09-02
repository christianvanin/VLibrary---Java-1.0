package com.vcontrol.utility;

import java.util.Timer;
import java.util.TimerTask;

public final class Utils {

    private Utils() {}
    public static void delay(Runnable runnable, long delayMs) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
                timer.cancel();
            }
        }, delayMs);
    }
}
