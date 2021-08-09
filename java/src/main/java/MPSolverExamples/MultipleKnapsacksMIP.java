package MPSolverExamples;

import com.google.ortools.linearsolver.*;
import lombok.extern.slf4j.Slf4j;
import utils.ORToolsLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class MultipleKnapsacksMIP {
    static {
        ORToolsLoader.load("/Users/bianlifeng/my_project/ortools_utils/java/src/lib/libjniortools.jnilib");
    }
    static class DataModel {
        public final int[] weights = {48, 30, 42, 36, 36, 48, 42, 42, 36, 24, 30, 30, 42, 36, 36, 22, 44, 22, 41, 11, 121, 33, 66, 77, 48, 30, 42, 36, 36};
        public final int[] values = {39, 30, 35, 50, 35, 30, 55, 40, 30, 35, 45, 10, 20, 30, 25, 48, 20, 33, 46, 23, 44, 67, 42, 43, 46, 23, 44, 67, 42};
        public final int numItems = weights.length;
        public final int numBins = 6;
        public final int[] binCapacities = {100, 100, 100, 100, 100, 100};
    }
    public static void main(String[] args) throws Exception {
        final DataModel data = new DataModel();

        // Create the linear solver with the CBC backend.
        MPSolver solver = new MPSolver(
                "MultipleKnapsackMip", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);

        MPVariable[][] x = new MPVariable[data.numItems][data.numBins];
        for (int i = 0; i < data.numItems; ++i) {
            for (int j = 0; j < data.numBins; ++j) {
                x[i][j] = solver.makeIntVar(0, 1, "");
            }
        }

        for (int i = 0; i < data.numItems; ++i) {
            MPConstraint constraint = solver.makeConstraint(0, 1, "");
            for (int j = 0; j < data.numBins; ++j) {
                constraint.setCoefficient(x[i][j], 1);
            }
        }
        for (int j = 0; j < data.numBins; ++j) {
            MPConstraint constraint = solver.makeConstraint(0, data.binCapacities[j], "");
            for (int i = 0; i < data.numItems; ++i) {
                constraint.setCoefficient(x[i][j], data.weights[i]);
            }
        }

        MPConstraint constraint = solver.makeConstraint(749, 800, "");
        for (int i = 0; i < data.numItems; ++i) {
            for (int j = 0; j < data.numBins; ++j) {
                constraint.setCoefficient(x[i][j], data.values[i]);
            }
        }
        MPObjective objective = solver.objective();
        for (int i = 0; i < data.numItems; ++i) {
            for (int j = 0; j < data.numBins; ++j) {
                objective.setCoefficient(x[i][j], data.values[i]);
            }
        }

        solver.setNumThreads(4);

//        solver.setHint(Stream.concat(s.stream(), t.stream()).toArray(MPVariable[]::new),
//                IntStream.range(0, s.size()+t.size()).mapToDouble(i -> i < s.size() ? 1 : 0).toArray());
//        solver.setHint(t.toArray(new MPVariable[0]), IntStream.range(0, t.size()).mapToDouble(i -> 0).toArray());
//        objective.setMaximization();
//        solver.setNumThreads(4);

//        solver.enableOutput();
//        MPSolverParameters m1 = new MPSolverParameters();

//        m1.setDoubleParam(MPSolverParameters.DoubleParam.RELATIVE_MIP_GAP, 0.01);

        MPSolver.ResultStatus resultStatus = solver.solve();


        // Check that the problem has an optimal solution.
        if (true) {
            System.out.println("Total packed value: " + objective.value() + "\n");
            double totalWeight = 0;
            for (int j = 0; j < data.numBins; ++j) {
                double binWeight = 0;
                double binValue = 0;
                System.out.println("Bin " + j + "\n");
                for (int i = 0; i < data.numItems; ++i) {
                    if (x[i][j].solutionValue() == 1) {
                        System.out.println(
                                "Item " + i + " - weight: " + data.weights[i] + "  value: " + data.values[i]);
                        binWeight += data.weights[i];
                        binValue += data.values[i];
                    }
                }
                System.out.println("Packed bin weight: " + binWeight);
                System.out.println("Packed bin value: " + binValue + "\n");
                totalWeight += binWeight;
            }
            System.out.println("Total packed weight: " + totalWeight);
            System.out.println("WallTime: " + solver.wallTime());
        } else {
            System.err.println("The problem does not have an optimal solution.");
        }
    }
}
