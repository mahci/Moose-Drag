package at.aau.moose_drag.control;

import static at.aau.moose_drag.data.Consts.STRINGS.END;
import static at.aau.moose_drag.data.Consts.STRINGS.EXP_ID;
import static at.aau.moose_drag.data.Consts.STRINGS.GENLOG;
import static at.aau.moose_drag.data.Consts.STRINGS.SP;

import android.os.Environment;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import at.aau.moose_drag.data.Memo;
import at.aau.moose_drag.log.GeneralLog;
import at.aau.moose_drag.log.MotionEventLog;
import at.aau.moose_drag.tools.Out;

import static at.aau.moose_drag.experiment.Experiment.*;

public class Logger {
    private final static String NAME = "Logger/";
    // -------------------------------------------------------------------------------------------
    private static Logger self;

    private static String mLogDirectory; // Main folder for logs

    private GeneralLog mGenLog;
    private String mPcId;

    private PrintWriter mBoxMELogPW, mBarMELogPW, mPeekMELogPW, mTunnelMELogPW;
    private PrintWriter mActiveLogFilePW;

    // -------------------------------------------------------------------------------------------
    public static Logger get() {
        if (self == null) self = new Logger();
        return self;
    }

    /**
     * Constructor
     */
    public Logger() {
        // Create the log dir (if not existed)
        mLogDirectory = Environment.getExternalStorageDirectory() + "/Moose_Drag_Log/";
        boolean res = createDir(mLogDirectory);
        Out.d(NAME, mLogDirectory, res);
    }

    /**
     * Extract log info from Memo
     * @param memo Memo
     */
    public void setLogInfo(Memo memo) {
        switch (memo.getMode()) {
            case EXP_ID: {
                mPcId = memo.getValue1Str();
                openLogFiles();

                break;
            }

            case GENLOG: {
                Out.d(NAME, memo);
                mGenLog = new Gson().fromJson(memo.getValue1Str(), GeneralLog.class);
                setActiveLogFile();

                break;
            }

            case END: {
                closeLogs();

                break;
            }

        }
    }

    /**
     * Log MotionEventInfo
     * @param meventLog MotionEventInfo
     */
    public void logMotionEvent(MotionEventLog meventLog) {
        try {
            if (mActiveLogFilePW == null) setActiveLogFile();

            mActiveLogFilePW.println(mGenLog + SP + meventLog);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private String getLogHeaders() {
        return GeneralLog.getLogHeader() + SP + MotionEventLog.getLogHeader();
    }

    private void openLogFiles() {
        final String boxLogFilePath = mLogDirectory + mPcId + "_" + TASK.BOX + "_MOEV.txt";
        final String barLogFilePath = mLogDirectory + mPcId + "_" + TASK.BAR + "_MOEV.txt";
        final String peekLogFilePath = mLogDirectory + mPcId + "_" + TASK.PEEK + "_MOEV.txt";
        final String tunnelLogFilePath = mLogDirectory + mPcId + "_" + TASK.TUNNEL + "_MOEV.txt";

        try {
            mBoxMELogPW = new PrintWriter(
                    new FileOutputStream(boxLogFilePath, true), true);
            if (isFileEmpty(boxLogFilePath)) mBoxMELogPW.println(getLogHeaders());

            mBarMELogPW = new PrintWriter(
                    new FileOutputStream(barLogFilePath, true), true);
            if (isFileEmpty(barLogFilePath)) mBarMELogPW.println(getLogHeaders());

            mPeekMELogPW = new PrintWriter(
                    new FileOutputStream(peekLogFilePath, true), true);
            if (isFileEmpty(peekLogFilePath)) mPeekMELogPW.println(getLogHeaders());

            mTunnelMELogPW = new PrintWriter(
                    new FileOutputStream(tunnelLogFilePath, true), true);
            if (isFileEmpty(tunnelLogFilePath)) mTunnelMELogPW.println(getLogHeaders());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setActiveLogFile() {
        openLogFiles();

        if (mGenLog != null) {
            switch (mGenLog.task) {
            case BOX: mActiveLogFilePW = mBoxMELogPW; break;
            case BAR: mActiveLogFilePW = mBarMELogPW; break;
            case PEEK: mActiveLogFilePW = mPeekMELogPW; break;
            case TUNNEL: mActiveLogFilePW = mTunnelMELogPW; break;
            }
        }
    }

    /**
     * Close all the log files
     */
    public void closeLogs() {
        if (mActiveLogFilePW != null) mActiveLogFilePW.close();
    }

    /**
     * Create a dir if not existed
     * @param path Dir path
     * @return STATUS
     */
    public boolean createDir(String path) {
        File folder = new File(path);
        Out.d(NAME, folder.exists());
        return folder.mkdir();
    }

    /**
     * Check if a file is empty
     * @param filePath File path
     * @return True (empty), False (not empty)
     */
    public static boolean isFileEmpty(String filePath) {
        try {
            final BufferedReader br = new BufferedReader(new FileReader(filePath));
            return br.readLine() == null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
