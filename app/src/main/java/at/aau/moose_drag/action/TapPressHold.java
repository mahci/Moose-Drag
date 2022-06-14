package at.aau.moose_drag.action;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.graphics.PointF;
import android.os.CountDownTimer;
import android.view.MotionEvent;

import at.aau.moose_drag.control.Networker;
import at.aau.moose_drag.data.Memo;
import at.aau.moose_drag.tools.Out;
import at.aau.moose_drag.tools.Utils;
import at.aau.moose_drag.views.MainActivity;

import static at.aau.moose_drag.data.Consts.*;

public class TapPressHold {
    private final String NAME = "TapPressHold/";

    // Constants
    private final int TAP_TIMEOUT = 200; // ms
    private final double MIN_UP_dY_mm = 2; // mm

    // Tracking
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mNumPointers = 0;
    private PointF mLastPointF = new PointF();
    private boolean mTapped;
    private boolean mPressedFirst = false;
    private boolean mPressedSecond = false;
    private boolean mGrabbed = false;

    // Timers
    private CountDownTimer mTapTimer;

    // Methods ----------------------------------------------------------------------------
    public TapPressHold() {

    }

    /**
     * Act on the event
     * @param event MotionEvent
     */
    public void act(MotionEvent event) {
        Out.d(NAME, "acting...");

        if (mTapTimer == null) {
            mTapTimer = new CountDownTimer(TAP_TIMEOUT, 10) {
                @Override
                public void onTick(long millisUntilFinished) {}

                @Override
                public void onFinish() {
                    mPressedFirst = false;
                }
            };
        }

        switch (event.getActionMasked()) {

        case MotionEvent.ACTION_DOWN: {
            final int pointerIndex = event.getActionIndex();
            mActivePointerId = event.getPointerId(pointerIndex);

            mNumPointers = 1;

            press();

            mLastPointF = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));

            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);
            int activeIndex = event.findPointerIndex(mActivePointerId);

            mNumPointers++;

            // Same finger is returned
            if (pointerId == mActivePointerId) {
                press();

                mLastPointF = new PointF(event.getX(activeIndex), event.getY(activeIndex));
            } else { // New pointer
                // If the new pointer is added to the left
                if (activeIndex != -1 && event.getX(pointerIndex) < event.getX(activeIndex)) {
                    press();

                    mActivePointerId = event.getPointerId(pointerIndex);
                    activeIndex = event.findPointerIndex(mActivePointerId);
                    mLastPointF = new PointF(event.getX(activeIndex), event.getY(activeIndex));
                }
            }

            break;
        }

        case MotionEvent.ACTION_MOVE: {
            int activeIndex = event.findPointerIndex(mActivePointerId);

            if (activeIndex != -1 && mGrabbed) {
                final float dY = Utils.px2mm(event.getY(activeIndex) - mLastPointF.y);

                if (dY < -MIN_UP_dY_mm) { // Up while holding down => revert
                    revert();
                }
            }

            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);

            mNumPointers--;

            if (pointerId == mActivePointerId) { // Active pointer is lifted up
                liftOff();
            }

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;

            mNumPointers = 0;

            allLiftOff();

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
            mTapTimer.start();
        }
        else { // Tapped + press
            grab();
        }

        Out.d(TAG, "mPF, mPS, mT", mPressedFirst, mPressedSecond, mTapped);
    }

    private void liftOff() {
        final String TAG = NAME + "liftOff";

        if (mPressedFirst) {
            mTapped = true;

            mPressedFirst = false;
            mPressedSecond = false;
            mGrabbed = false;

            // TODO: Another Timeout?
        } else if (mPressedSecond) {
//            release();
        }

        Out.d(TAG, "mPF, mPS, mT", mPressedFirst, mPressedSecond, mTapped);
    }

    private void allLiftOff() {
        final String TAG = NAME + "allLiftOff";

        if (mPressedFirst) {
            mTapped = true;

            mPressedFirst = false;
            mPressedSecond = false;
            mGrabbed = false;

            // TODO: Another Timeout?
        } else if (mPressedSecond) {
            release();
        }

        Out.d(TAG, "mPF, mPS, mT", mPressedFirst, mPressedSecond, mTapped);
    }

    private void grab() {
        Memo grabMemo = new Memo(STRINGS.DRAG, STRINGS.GRAB, 0, 0);
        Networker.get().sendMemo(grabMemo);

        // Change flags
        mPressedFirst = false;
        mTapped = false;
        mPressedSecond = true;
        mGrabbed = true;

        // Cancel the tap timer
        mTapTimer.cancel();
    }

    private void release() {
        Memo relMemo = new Memo(STRINGS.DRAG, STRINGS.RELEASE, 0, 0);
        Networker.get().sendMemo(relMemo);

        // Reset flags
        mPressedFirst = false;
        mPressedSecond = false;
        mTapped = false;
        mGrabbed = false;
    }

    private void revert() {
        Memo relMemo = new Memo(STRINGS.DRAG, STRINGS.REVERT, 0, 0);
        Networker.get().sendMemo(relMemo);

        // Reset flags
        mPressedFirst = false;
        mPressedSecond = false;
        mTapped = false;
        mGrabbed = false;
    }
}
