package at.aau.moose_drag.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.aau.moose_drag.DAReceiver;
import at.aau.moose_drag.R;
import at.aau.moose_drag.control.Actioner;
import at.aau.moose_drag.control.AdminManager;
import at.aau.moose_drag.control.Logger;
import at.aau.moose_drag.control.Networker;
import at.aau.moose_drag.data.Consts.*;
import at.aau.moose_drag.log.MotionEventLog;
import at.aau.moose_drag.tools.Out;
import at.aau.moose_drag.tools.Utils;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity/";
    // -------------------------------------------------------------------------------

    static boolean isAdmin = false; // is the app admin?
    static final int OVERLAY_PERMISSION_CODE = 2; // code for overlay permission intent

    private ExecutorService executorService; // for running threads
    private AlertDialog.Builder dialogBuilder; // for creating dialogs
    private AlertDialog dialog; // dialog for everyting!

    private boolean mUpPressed, mDownPressed;

    private ComponentName mAdminComponent;
    // -------------------------------------------------------------------------------

    // Main Handler
    @SuppressLint("HandlerLeak")
    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message mssg) {
            Log.d(TAG, "handleMessage: " + mssg.what);
            if (mssg.what == INTS.CLOSE_DLG) {
                if (dialog != null) dialog.dismiss();
                drawUI();
            }

            if (mssg.what == INTS.SHUT_DOWN) {
                Out.d(TAG, "Finnishing the activity");
                finishAffinity();
                System.exit(0);
            }
        }
    };
    // -------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); // For removing the status bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting
        executorService = Executors.newSingleThreadExecutor();
        dialogBuilder = new AlertDialog.Builder(this);
        Utils.setVibrator((Vibrator) getSystemService(VIBRATOR_SERVICE));
        Networker.get().setMainHandler(mainHandler);

        // Init
        checkAdmin();

        showDialog("Connecting to desktop...");
        Networker.get().connect();
    }

    /**
     * Show an AlertDialog
     * @param mssg Message to show
     */
    private void showDialog(String mssg) {
        dialog = dialogBuilder.create();
        dialog.setMessage(mssg);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Draw the main UI
     */
    private void drawUI() {
        // Get the overlay permission (possible only with admin)
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
        } else {
            drawTouchViewGroup();
        }
    }

    /**
     * Make sure the app has adimin permissions
     */
    private void checkAdmin() {
        //-- Get the admin permission [for removing the status bar]
        final DevicePolicyManager dpm = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = DAReceiver.getComponentName(this);
        isAdmin = dpm.isAdminActive(mAdminComponent);
        Out.d(TAG, "Admin check", isAdmin);

        if (!isAdmin) {

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    DAReceiver.getComponentName(this));
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.add_admin_extra_app_text));
            Out.d(TAG, intent);
            startActivityForResult(intent, 1);

//            ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    result -> {
//
//                    });
//
//
//            // Launch the activity to have the user enable our admin.
//            Out.d(TAG, "Admin check", "Launching activity...");
//            final ComponentName deviceAdminASample = null;
//            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminASample);
//            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                    getString(R.string.add_admin_extra_app_text));
//
//            activityResultLauncher.launch(intent);
//            startActivityForResult(intent, 1);
        } else {
//            Toast.makeText(this, "Is Admin", Toast.LENGTH_SHORT).show();
            boolean result = dpm.setStatusBarDisabled(mAdminComponent, true);
            Out.d(TAG, "Status Bar disabling result", result);
        }

    }

    /**
     * Set isAdmin from outside the activity
     * @param isa is admin?
     */
    public static void setIsAdim(boolean isa) {
        isAdmin = isa;
    }

    /**
     * Draw the custom view (to apear under the status bar)
     */
    public void drawTouchViewGroup() {
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        WindowManager winManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;

        TouchViewGroup view = new TouchViewGroup(this);

        view.setBackgroundColor(Color.WHITE);
        view.setKeepScreenOn(true);

        assert winManager != null;
        winManager.addView(view, params);
    }

    /**
     * Custom view class
     */
    private class TouchViewGroup extends ViewGroup {

        public TouchViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {

        }

        /**
         * Intercept the touches on the view
         * @param ev - MotionEvent
         * @return Always true (to pass the events to children)
         */
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (ev.getY() <= getStBarHeight()) {
                // Redraw the layout
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                startActivity(getIntent());
            }

            return false;
        }

        //-------------- MAIN PART! ----------------------------
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Ignore the bottom 3 cm
            if (event.getY() < Utils.mm2px(80)) {
                Actioner.get().drag(event);

                // LOG
                Logger.get().logMotionEvent(new MotionEventLog(event));
            }

//            ViewConfiguration vc = ViewConfiguration.get(this.getContext());
//            int slop = vc.getScaledTouchSlop();
//            Out.d(TAG, "Slop px: " + slop, "mm: " + Utils.px2mm(slop));
            return super.onTouchEvent(event);
        }
        //------------------------------------------------------
    }

    /**
     * Get the height of status bar
     * @return int (px)
     */
    private int getStBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }


//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        int action = event.getAction();
//        int keyCode = event.getKeyCode();
//        switch (keyCode) {
//
//        case KeyEvent.KEYCODE_VOLUME_UP:
//            if (action == KeyEvent.ACTION_DOWN) {
//                mDownPressed = true;
//
//                Actioner.get().setActiveTech(Experiment.TECHNIQUE.TAP_PRESS_HOLD);
//
//                if (mUpPressed) System.exit(0);
//
//            } else if (action == KeyEvent.ACTION_UP) {
//                mDownPressed = false;
//            }
//
//            return true;
//
//        case KeyEvent.KEYCODE_VOLUME_DOWN:
//            if (action == KeyEvent.ACTION_DOWN) {
//                mUpPressed = true;
//
//                Actioner.get().setActiveTech(Experiment.TECHNIQUE.TWO_FINGER_SWIPE);
//
//                if (mDownPressed) System.exit(0);
//
//            } else if (action == KeyEvent.ACTION_UP) {
//                mUpPressed = false;
//            }
//
//            return true;
//        default:
//            return super.dispatchKeyEvent(event);
//        }
//    }

}