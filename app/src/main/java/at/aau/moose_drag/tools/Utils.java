package at.aau.moose_drag.tools;

import android.os.Vibrator;

public class Utils {

    public static final int PPI = 312; // For calculating movement in mm

    private static Vibrator mVibrator;

    /**
     * Conver px to mm
     * @param px Pixels
     * @return mm equivalent
     */
    public static double px2mm(double px) {
        return (px / PPI) * 25.4;
    }

    /**
     * Conver px to mm
     * @param px Pixels
     * @return mm equivalent
     */
    public static float px2mm(float px) {
        return (float) ((px / PPI) * 25.4);
    }

    /**
     * Set the vibrator
     * @param vib Vibrator
     */
    public static void setVibrater(Vibrator vib) {
        mVibrator = vib;
    }

    /**
     * Vibrate for millisec
     * @param millisec time in milliseconds
     */
    public static void vibrate(long millisec) {
        if (mVibrator != null) mVibrator.vibrate(millisec);
    }

}
