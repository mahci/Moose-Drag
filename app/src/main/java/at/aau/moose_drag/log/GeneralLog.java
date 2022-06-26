package at.aau.moose_drag.log;

import com.google.gson.Gson;

import at.aau.moose_drag.data.Consts;
import at.aau.moose_drag.experiment.Experiment;

import static at.aau.moose_drag.data.Consts.STRINGS.*;

import androidx.annotation.NonNull;

public class GeneralLog {
    public Experiment.TASK task;
    public Experiment.TECHNIQUE technique;
    public int block_num;
    public int trial_num;
    public String trialStr;

    public static String getLogHeader() {
        return "task" + SP +
                "technique" + SP +
                "block_num" + SP +
                "trial_num" + SP +
                getTrialLogHeader();
    }

    private static String getTrialLogHeader() {
        return "trial_x" + SP +
                "trial_y" + SP +
                "object_w" + SP +
                "target_w" + SP +
                "axis" + SP +
                "direction";
    }

    @NonNull
    @Override
    public String toString() {
        return task.toString() + SP +
                technique + SP +
                block_num + SP +
                trial_num + SP +
                trialStr;
    }
}
