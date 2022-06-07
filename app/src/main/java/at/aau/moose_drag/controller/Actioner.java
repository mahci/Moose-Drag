package at.aau.moose_drag.controller;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.graphics.PointF;
import android.os.CountDownTimer;
import android.view.MotionEvent;

import at.aau.moose_drag.data.Memo;
import at.aau.moose_drag.tools.Out;
import at.aau.moose_drag.tools.Utils;

import static at.aau.moose_drag.data.Consts.*;
import static at.aau.moose_drag.experiment.Experiment.*;

public class Actioner {
    private final String NAME = "Actioner/";
    private static Actioner instance; // Singelton instance
    // -------------------------------------------------------------------------------

    private TECHNIQUE mActiveTech = TECHNIQUE.TFSD;

    private final int PPI = 312; // For calculating movement in mm

    // Algorithm parameters
    private int leftmostId = INVALID_POINTER_ID; // Id of the left finger
    private int leftmostIndex = INVALID_POINTER_ID; // Index of the leftmost finger
    private int actionIndex = INVALID_POINTER_ID; // New finger's index

    private int nPointers;
    private int mActivePointerId = INVALID_POINTER_ID;

    private boolean mGrabbed = false;
    private PointF[] mLastPointF = new PointF[2];
    private final float[] mDelYs = new float[2];

    private boolean mPressedFirst = false;
    private boolean mTapped = false;
    private boolean mPressedSecond = false;

    // Thresholds
    private final long MAX_GES_DUR_ms = 1 * 1000; // ms
    private final double MIN_DOWN_DELY_mm = 2; // mm
    private final double MIN_UP_DELY_mm = 2; // mm
    private final int TAP_TIMEOUT = 200; // ms

    private CountDownTimer TAP_TIMER = new CountDownTimer(TAP_TIMEOUT, 10) {
        @Override
        public void onTick(long millisUntilFinished) {}

        @Override
        public void onFinish() {
            mPressedFirst = false;
        }
    };

    // -------------------------------------------------------------------------------

    /**
     * Get the Singleton instance
     * @return Actioner instance
     */
    public static Actioner get() {
        if (instance == null) instance = new Actioner();
        return instance;
    }

    /**
     * Set the config
     * @param memo Memo from Desktop
     */
    public void config(Memo memo) {
        final String TAG = NAME + "config";

        switch (memo.getMode()) {
            case STRINGS.TECH: {
                mActiveTech = TECHNIQUE.valueOf(memo.getValue1Str());
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

        switch (mActiveTech) {
        case TPH: dragTPH(event); break;
        case TFSD: dragTFSD(event); break;
        }
    }

    // TPH ===========================================================================
    private void dragTPH(MotionEvent event) {

        switch (event.getActionMasked()) {

        case MotionEvent.ACTION_DOWN: {
            final int pointerIndex = event.getActionIndex();
            mActivePointerId = event.getPointerId(pointerIndex);
            nPointers = 1;

            press();

            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);
            final int activeIndex = event.findPointerIndex(mActivePointerId);

            nPointers++;

            // Same finger is returned
            if (pointerId == mActivePointerId) {
                press();
            } else { // New pointer
                // If the new pointer is added to the left
                if (activeIndex != -1 && event.getX(pointerIndex) < event.getX(activeIndex)) {
                    press();

                    mActivePointerId = event.getPointerId(pointerIndex);
                }
            }

            break;
        }

        case MotionEvent.ACTION_MOVE: {

            final int activeIndex = event.findPointerIndex(mActivePointerId);
            if (activeIndex == -1) break;

            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);

            nPointers--;

            if (pointerId == mActivePointerId) { // Active pointer is up
                up();
            }

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;

            nPointers = 0;

            up();

            break;
        }

        }
    }

    private void press() {
        final String TAG = NAME + "press";

        if (!mTapped) { // First press
            mPressedFirst = true;
            mPressedSecond = false;

            // Start the count down
            TAP_TIMER.start();
        }
        else { // Tapped + press
            Memo grabMemo = new Memo("DRAG", "GRAB", 0, 0);
            Networker.get().sendMemo(grabMemo);

            mPressedFirst = false;
            mTapped = false;
            mPressedSecond = true;

            TAP_TIMER.cancel();
        }

        Out.d(TAG, "mPF, mPS, mT", mPressedFirst, mPressedSecond, mTapped);
    }

    private void up() {
        final String TAG = NAME + "up";

        if (mPressedFirst) {
            mTapped = true;
            mPressedFirst = false;
            mPressedSecond = false;

            // TODO: Another Timeout?

        } else if (mPressedSecond) {
            // Release
            Memo relMemo = new Memo("DRAG", "REL", 0, 0);
            Networker.get().sendMemo(relMemo);

            // Reset
            mPressedFirst = false;
            mPressedSecond = false;
            mTapped = false;
        }

        Out.d(TAG, "mPF, mPS, mT", mPressedFirst, mPressedSecond, mTapped);
    }

    // TFSD ===========================================================================
    private void dragTFSD(MotionEvent event) {

        switch (event.getActionMasked()) {

        case MotionEvent.ACTION_DOWN: {
            final int pointerIndex = event.getActionIndex();
            mActivePointerId = event.getPointerId(pointerIndex);

            nPointers = 1;

            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);
            final int activeIndex = event.findPointerIndex(mActivePointerId);

            nPointers++;

            if (nPointers == 2) {
                mLastPointF[0] = new PointF(event.getX(0), event.getY(0));
                mLastPointF[1] = new PointF(event.getX(1), event.getY(1));
            }

            // Same finger is returned
            if (pointerId == mActivePointerId) {
                final float x = event.getX(activeIndex);
                final float y = event.getY(activeIndex);

            } else { // New pointer
                // If the new pointer is added to the left
                if (activeIndex != -1 && event.getX(pointerIndex) < event.getX(activeIndex)) {
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);

                    mActivePointerId = event.getPointerId(pointerIndex);

                }
            }

            break;
        }

        case MotionEvent.ACTION_MOVE: {

            if (nPointers == 2) {

                // Get the movement direction of both pointers (abs. for amt comparison)
                mDelYs[0] = Utils.px2mm(event.getY(0) - mLastPointF[0].y);
                mDelYs[1] = Utils.px2mm(event.getY(1) - mLastPointF[1].y);

                // Is the object grabbed or not?
                if (!mGrabbed) {

                    if (mDelYs[0] > MIN_DOWN_DELY_mm && mDelYs[1] > MIN_DOWN_DELY_mm) {
                        Memo grabMemo = new Memo("DRAG", "GRAB", 0, 0);
                        Networker.get().sendMemo(grabMemo);

                        mGrabbed = true;
                    }

                } else {

                    // Continue down or change to up?
                    if (mDelYs[0] > 0 && mDelYs[1] > 0) { // Down
                        mLastPointF[0] = new PointF(
                                event.getX(0),
                                event.getY(0));
                        mLastPointF[1] = new PointF(
                                event.getX(1),
                                event.getY(1));
                    } else if (mDelYs[0] < 0 && mDelYs[1] < 0) { // Back up

                        if (mDelYs[0] < -MIN_UP_DELY_mm && mDelYs[1] < -MIN_UP_DELY_mm) {
                            Memo relMemo = new Memo("DRAG", "CANCEL", 0, 0);
                            Networker.get().sendMemo(relMemo);

                            mGrabbed = false;
                        }
                    }


                }

            }


            final int activeIndex = event.findPointerIndex(mActivePointerId);
            if (activeIndex == -1) break;

            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);

            nPointers--;

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;

            nPointers = 0;

            if (mGrabbed) {
                mGrabbed = false;
                Memo relMemo = new Memo("DRAG", "REL", 0, 0);
                Networker.get().sendMemo(relMemo);
            }

            break;
        }

        }
    }


    /**
     * Check if a pointer is leftmost
     * @param me MortionEvent
     * @param pointerIndex index of the pointer to check
     * @return boolean
     */
    public boolean isLeftMost(MotionEvent me, int pointerIndex) {
        return findLeftMostIndex(me) == pointerIndex;
    }

    /**
     * Find the index of leftmost pointer
     * @param me MotionEvent
     * @return Index of the leftmost pointer
     */
    public int findLeftMostIndex(MotionEvent me) {
        String TAG = NAME + "findLeftMostIndex";

        int nPointers = me.getPointerCount();
        Out.d(TAG, "nPointers", me.getPointerCount());
        if (nPointers == 0) return -1;
        if (nPointers == 1) return 0;

        // > 1 pointers (POINTER_DOWN or POINTER_UP)
        int lmIndex = 0;
        for (int pix = 0; pix < me.getPointerCount(); pix++) {
            if (me.getX(pix) < me.getX(lmIndex)) lmIndex = pix;
        }

        return lmIndex;
    }

    /**
     * Find the id of the leftmost pointer
     * @param me MotionEvent
     * @return Id of the leftmost pointer
     */
    private int findLeftMostId(MotionEvent me) {
        int lmIndex = findLeftMostIndex(me);
        if (lmIndex == -1) return INVALID_POINTER_ID;
        else return me.getPointerId(lmIndex);
    }

    /**
     * Update the leftmost properties and lastPoint
     */
    private void updatePointers(MotionEvent me) {
        String TAG = NAME + "updatePointers";

        leftmostIndex = findLeftMostIndex(me);
        leftmostId = me.getPointerId(leftmostIndex);
//        mLastPointF = new PointF(me.getX(leftmostIndex), me.getY(leftmostIndex));

    }

    /**
     * Truly GET the PointerCoords!
     * @param me MotionEvent
     * @param pointerIndex Pointer index
     * @return PointerCoords
     */
    public MotionEvent.PointerCoords getPointerCoords(MotionEvent me, int pointerIndex) {
        MotionEvent.PointerCoords result = new MotionEvent.PointerCoords();
        me.getPointerCoords(pointerIndex, result);
        return result;
    }

}
