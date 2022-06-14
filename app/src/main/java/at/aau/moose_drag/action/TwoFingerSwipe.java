package at.aau.moose_drag.action;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.graphics.PointF;
import android.view.MotionEvent;

import at.aau.moose_drag.control.Networker;
import at.aau.moose_drag.data.Memo;
import at.aau.moose_drag.tools.Utils;

import static at.aau.moose_drag.data.Consts.*;

public class TwoFingerSwipe {
    private final String NAME = "TwoFingerSwipe/";

    // Constants
    private final double MIN_DOWN_dY_mm = 2; // mm
    private final double MIN_UP_dY_mm = 2; // mm

    // Flags

    // Tracking
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mNumPointers = 0;
    private PointF[] mLastPointF = new PointF[2];
    private float[] mDelYs = new float[2];
    private boolean mGrabbed = false;

    // Methods ----------------------------------------------------------------------------
    public TwoFingerSwipe() {

    }

    /**
     * Act on the event
     * @param event MotionEvent
     */
    public void act(MotionEvent event) {

        switch (event.getActionMasked()) {

        case MotionEvent.ACTION_DOWN: {
            final int pointerIndex = event.getActionIndex();
            mActivePointerId = event.getPointerId(pointerIndex);

            mNumPointers = 1;

            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);
            final int activeIndex = event.findPointerIndex(mActivePointerId);

            mNumPointers++;

            // Same the two pointers' locations
            if (mNumPointers == 2) {
                mLastPointF[0] = new PointF(event.getX(0), event.getY(0));
                mLastPointF[1] = new PointF(event.getX(1), event.getY(1));
            }

            break;
        }

        case MotionEvent.ACTION_MOVE: {

            if (mNumPointers == 2) {

                // Get the movement amount of both pointers
                mDelYs[0] = Utils.px2mm(event.getY(0) - mLastPointF[0].y);
                mDelYs[1] = Utils.px2mm(event.getY(1) - mLastPointF[1].y);

                if (!mGrabbed) { // Object not grabbed

                    if (mDelYs[0] > MIN_DOWN_dY_mm && mDelYs[1] > MIN_DOWN_dY_mm) {
                        grab();
                    }

                } else { // Grabbed

                    // Continue down or change to up?
                    if (mDelYs[0] > 0 && mDelYs[1] > 0) { // Down

                        // Save the new locations
                        mLastPointF[0] = new PointF(
                                event.getX(0),
                                event.getY(0));
                        mLastPointF[1] = new PointF(
                                event.getX(1),
                                event.getY(1));

                    } else if (mDelYs[0] < -MIN_UP_dY_mm
                            && mDelYs[1] < -MIN_UP_dY_mm) { // Back up
                        revert();
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

            mNumPointers--;

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;

            mNumPointers = 0;

            if (mGrabbed) {
                release();
            }

            break;
        }

        }
    }

    private void grab() {
        Memo grabMemo = new Memo(STRINGS.DRAG, STRINGS.GRAB, 0, 0);
        Networker.get().sendMemo(grabMemo);

        mGrabbed = true;
    }

    private void release() {
        Memo relMemo = new Memo(STRINGS.DRAG, STRINGS.RELEASE, 0, 0);
        Networker.get().sendMemo(relMemo);

        mGrabbed = false;
    }

    private void revert() {
        Memo relMemo = new Memo(STRINGS.DRAG, STRINGS.REVERT, 0, 0);
        Networker.get().sendMemo(relMemo);

        mGrabbed = false;
    }



}
