package at.aau.moose_drag.control;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.graphics.PointF;
import android.os.CountDownTimer;
import android.view.MotionEvent;

import at.aau.moose_drag.action.TapPressHold;
import at.aau.moose_drag.action.TwoFingerSwipe;
import at.aau.moose_drag.data.Memo;
import at.aau.moose_drag.tools.Out;
import at.aau.moose_drag.tools.Utils;

import static at.aau.moose_drag.data.Consts.*;
import static at.aau.moose_drag.experiment.Experiment.*;

public class Actioner {
    private final String NAME = "Actioner/";
    private static Actioner instance; // Singelton instance
    // -------------------------------------------------------------------------------

    private TECHNIQUE mActiveTech = TECHNIQUE.TWO_FINGER_SWIPE;

    private final int PPI = 312; // For calculating movement in mm

    // Algorithm parameters
//    private int leftmostId = INVALID_POINTER_ID; // Id of the left finger
//    private int leftmostIndex = INVALID_POINTER_ID; // Index of the leftmost finger
//    private int actionIndex = INVALID_POINTER_ID; // New finger's index
//
//    private int nPointers;
//    private int mActivePointerId = INVALID_POINTER_ID;
//
//    private boolean mGrabbed = false;
//    private PointF[] mLastPointF = new PointF[2];
//    private final float[] mDelYs = new float[2];
//
//    private boolean mPressedFirst = false;
//    private boolean mTapped = false;
//    private boolean mPressedSecond = false;

    // Thresholds
//    private final long MAX_GES_DUR_ms = 1 * 1000; // ms
//    private final double MIN_DOWN_DELY_mm = 2; // mm
//    private final double MIN_UP_DELY_mm = 2; // mm
//    private final int TAP_TIMEOUT = 200; // ms

    private final TwoFingerSwipe mTwoFingerSwipe;
    private final TapPressHold mTapPressHold;

    // -------------------------------------------------------------------------------

    /**
     * Get the Singleton instance
     * @return Actioner instance
     */
    public static Actioner get() {
        if (instance == null) instance = new Actioner();
        return instance;
    }

    private Actioner() {
        mTwoFingerSwipe = new TwoFingerSwipe();
        mTapPressHold = new TapPressHold();
    }

    /**
     * Set the config
     * @param memo Memo from Desktop
     */
    public void config(Memo memo) {
        final String TAG = NAME + "config";

        switch (memo.getMode()) {
            case STRINGS.TECH: {
                mActiveTech = TECHNIQUE.valueOf(memo.getStrValue(1));
                break;
            }
        }
    }

    public void setActiveTech(TECHNIQUE t) {
        mActiveTech = t;
    }

    public void printSamples(MotionEvent ev) {
        final String TAG = NAME + "printSamples";

        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        for (int h = 0; h < historySize; h++) {
            Out.d(TAG, "(Hist) At time:", ev.getHistoricalEventTime(h));
            for (int p = 0; p < pointerCount; p++) {
                Out.d(TAG, "  pointer:",
                        ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
            }
        }
        Out.d(TAG, "At time:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            Out.d(TAG, "  pointer:",
                    ev.getPointerId(p), ev.getX(p), ev.getY(p));
        }
    }

    // ================================================================================
    /**
     * Perform the action
     * @param event MotionEvent to process and perform
     */
    public void drag(MotionEvent event) {
        String TAG = NAME + "drag";
        Out.d(TAG, "Dragging...");
        switch (mActiveTech) {
        case TAP_PRESS_HOLD: mTapPressHold.act(event); break;
        case TWO_FINGER_SWIPE: mTwoFingerSwipe.act(event); break;
        }
    }
}
