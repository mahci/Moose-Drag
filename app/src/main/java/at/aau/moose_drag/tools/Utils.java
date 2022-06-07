package at.aau.moose_drag.tools;

public class Utils {

    public static final int PPI = 312; // For calculating movement in mm

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

}
