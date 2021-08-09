package utils;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import org.junit.Test;

import static org.junit.Assert.*;

public class CpModelUtilTest {

    static {
        ORToolsLoader.load("/Users/bianlifeng/my_project/ortools_utils/java/src/lib/libjniortools.jnilib");
    }

    @Test
    public void addLessOrEqual() {
        CpModel model = new CpModel();

        IntervalVar v = model.newFixedInterval(1,2, "");

    }

}