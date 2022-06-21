package at.aau.moose_drag.log;

import com.google.gson.Gson;

import at.aau.moose_drag.data.Consts;
import at.aau.moose_drag.experiment.Experiment;

import static at.aau.moose_drag.data.Consts.STRINGS.*;

public class GeneralLog {
    public Experiment.TASK task;
    public Experiment.TECHNIQUE technique;
    public int block_num;
    public int trial_num;

    public static String getLogHeader() {
        return "task" + SP +
                "tech" + SP +
                "block_num" + SP +
                "trial_num";
    }

    @Override
    public String toString() {
        return task.toString() + SP +
                technique + SP +
                block_num + SP +
                trial_num;
    }
}
