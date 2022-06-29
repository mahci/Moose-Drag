package at.aau.moose_drag.tools;

import android.util.Log;

/**
 * A kinda wrapper class for Log
 */
public class Out {

    public static void d(String tag, Object... params) {
        if (params.length > 0) {
            StringBuilder sb = new StringBuilder();
            for(Object p : params) {
                sb.append(p).append(" | ");
            }
            Log.d(tag, sb.toString());
        }
    }

}
