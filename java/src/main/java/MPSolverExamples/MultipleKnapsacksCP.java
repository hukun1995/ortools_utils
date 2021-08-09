package MPSolverExamples;

import com.google.ortools.sat.*;
import lombok.extern.slf4j.Slf4j;
import utils.ORToolsLoader;

@Slf4j
public class MultipleKnapsacksCP {
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
        CpModel model = new CpModel();

        IntVar[][] x = new IntVar[data.numItems][data.numBins];
        for (int i = 0; i < data.numItems; ++i) {
            for (int j = 0; j < data.numBins; ++j) {
                x[i][j] = model.newBoolVar("");
            }
        }

        // 一个品至多只能放一个包
        for (int i = 0; i < data.numItems; ++i) {
            model.addLessOrEqual(LinearExpr.sum(x[i]), 1);
        }

        // 一个包重量不超
        for (int j = 0; j < data.numBins; ++j) {
            IntVar[] t = new IntVar[data.numItems];
            int[] w = new int[data.numItems];
            for (int i = 0; i < data.numItems; ++i) {
                t[i] = x[i][j];
                w[i] = data.weights[i];
            }
            model.addLessOrEqual(LinearExpr.scalProd(t, w), data.binCapacities[j]);
        }

        // 收益最大
        IntVar[] o = new IntVar[data.numItems*data.numBins];
        int[] ow = new int[data.numItems*data.numBins];
        int index = 0;
        for (int i = 0; i < data.numItems; ++i) {
            for (int j = 0; j < data.numBins; ++j) {
                o[index] = x[i][j];
                ow[index] = data.values[i];
                index += 1;
            }
        }
        model.maximize(LinearExpr.scalProd(o, ow));

        CpSolver solver = new CpSolver();
        solver.getParameters().setLogSearchProgress(true);
        solver.getParameters().setNumSearchWorkers(4);
        solver.getParameters().setMaxTimeInSeconds(5);
        final CpSolverStatus status = solver.solve(model);

        // Check that the problem has an optimal solution.
        if (true) {
            System.out.println("Total packed value: " + solver.objectiveValue() + "\n");
            double totalWeight = 0;
            for (int j = 0; j < data.numBins; ++j) {
                double binWeight = 0;
                double binValue = 0;
                System.out.println("Bin " + j + "\n");
                for (int i = 0; i < data.numItems; ++i) {
                    if (solver.value(x[i][j]) == 1) {
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
