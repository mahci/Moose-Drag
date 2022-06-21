package at.aau.moose_drag.log;

import static at.aau.moose_drag.data.Consts.STRINGS.SP;
import static at.aau.moose_drag.tools.Utils.double3Dec;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import at.aau.moose_drag.tools.Utils;

public class MotionEventLog {
    public MotionEvent event;

    public MotionEventLog(MotionEvent me) {
        event = me;
    }

    public static String getLogHeader() {
        return "action" + SP +

                "flags" + SP +
                "edge_flags" + SP +
                "source" + SP +

                "event_time" + SP +
                "down_time" + SP +

                "number_pointers" + SP +

                "finger_1_index" + SP +
                "finger_1_id" + SP +
                "finger_1_orientation" + SP +
                "finger_1_pressure" + SP +
                "finger_1_size" + SP +
                "finger_1_toolMajor" + SP +
                "finger_1_toolMinor" + SP +
                "finger_1_touchMajor" + SP +
                "finger_1_touchMinor" + SP +
                "finger_1_x" + SP +
                "finger_1_y" + SP +

                "finger_2_index" + SP +
                "finger_2_id" + SP +
                "finger_2_orientation" + SP +
                "finger_2_pressure" + SP +
                "finger_2_size" + SP +
                "finger_2_toolMajor" + SP +
                "finger_2_toolMinor" + SP +
                "finger_2_touchMajor" + SP +
                "finger_2_touchMinor" + SP +
                "finger_2_x" + SP +
                "finger_2_y" + SP +

                "finger_3_index" + SP +
                "finger_3_id" + SP +
                "finger_3_orientation" + SP +
                "finger_3_pressure" + SP +
                "finger_3_size" + SP +
                "finger_3_toolMajor" + SP +
                "finger_3_toolMinor" + SP +
                "finger_3_touchMajor" + SP +
                "finger_3_touchMinor" + SP +
                "finger_3_x" + SP +
                "finger_3_y" + SP +

                "finger_4_index" + SP +
                "finger_4_id" + SP +
                "finger_4_orientation" + SP +
                "finger_4_pressure" + SP +
                "finger_4_size" + SP +
                "finger_4_toolMajor" + SP +
                "finger_4_toolMinor" + SP +
                "finger_4_touchMajor" + SP +
                "finger_4_touchMinor" + SP +
                "finger_4_x" + SP +
                "finger_4_y" + SP +

                "finger_5_index" + SP +
                "finger_5_id" + SP +
                "finger_5_orientation" + SP +
                "finger_5_pressure" + SP +
                "finger_5_size" + SP +
                "finger_5_toolMajor" + SP +
                "finger_5_toolMinor" + SP +
                "finger_5_touchMajor" + SP +
                "finger_5_touchMinor" + SP +
                "finger_5_x" + SP +
                "finger_5_y";
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(event.getActionMasked()).append(SP);

        result.append("0x").append(Integer.toHexString(event.getFlags())).append(SP);
        result.append("0x").append(Integer.toHexString(event.getEdgeFlags())).append(SP);
        result.append("0x").append(Integer.toHexString(event.getSource())).append(SP);

        result.append(event.getEventTime()).append(SP);
        result.append(event.getDownTime()).append(SP);

        // Pointers' info (for 0 - (nPointer -1) => real values | for the rest to 5 => dummy)
        int nPointers = event.getPointerCount();
        result.append(nPointers).append(SP);
        int pi;
        for(pi = 0; pi < nPointers; pi++) {
            result.append(pi).append(SP); // Index
            result.append(event.getPointerId(pi)).append(SP); // Id
            // PointerCoords
            result.append(pointerCoordsToStr(event, pi)).append(SP);
        }

        for (pi = nPointers; pi < 5; pi++) {
            result.append(-1).append(SP); // Index = -1
            result.append(-1).append(SP); // Id = -1
            // PointerCoords = empty
            result.append(pointerCoordsToStr(new MotionEvent.PointerCoords()))
                    .append(SP);
        }

        String resStr = result.toString();
        return resStr.substring(0, resStr.length() - 1); // Remove the last SP
    }

    /**
     * Truly GET the PointerCoords!
     * @param me MotionEvent
     * @param pointerIndex int pointer index
     * @return String
     */
    public static String pointerCoordsToStr(MotionEvent me, int pointerIndex) {
        MotionEvent.PointerCoords result = new MotionEvent.PointerCoords();
        me.getPointerCoords(pointerIndex, result);
        return pointerCoordsToStr(result);
    }

    /**
     * Get the string for a MotionEvent.PointerCoord
     * @return String (semi-colon separated)
     */
    public static String pointerCoordsToStr(MotionEvent.PointerCoords inPC) {
        return Utils.double3Dec(inPC.orientation) + SP +
                Utils.double3Dec(inPC.pressure) + SP +
                Utils.double3Dec(inPC.size) + SP +
                Utils.double3Dec(inPC.toolMajor) + SP +
                Utils.double3Dec(inPC.toolMinor) + SP +
                Utils.double3Dec(inPC.touchMajor) + SP +
                Utils.double3Dec(inPC.touchMinor) + SP +
                Utils.double3Dec(inPC.x) + SP +
                Utils.double3Dec(inPC.y);

    }
}
