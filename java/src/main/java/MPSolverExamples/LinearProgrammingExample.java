package MPSolverExamples;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import lombok.extern.slf4j.Slf4j;
import utils.ORToolsLoader;

import java.util.stream.IntStream;

@Slf4j
public class LinearProgrammingExample {
    static {
        ORToolsLoader.load("/Users/bianlifeng/my_project/ortools_utils/java/src/lib/libjniortools.jnilib");
    }

    public static void main(String[] args) throws Exception {
        //Declare the solver
        MPSolver solver = new MPSolver(
                "LinearProgrammingExample", MPSolver.OptimizationProblemType.BOP_INTEGER_PROGRAMMING);

        double infinity = Double.POSITIVE_INFINITY;

        // v1,v2是 非负的连续变量
        MPVariable v1 = solver.makeNumVar(0.0, infinity, "v1");
        System.out.println(v1.lb());
        MPVariable v2 = solver.makeNumVar(0.0, infinity, "v2");
        // v3, v4是非负的整数变量
        MPVariable v3 = solver.makeIntVar(0.0, infinity, "v3");
        MPVariable v4 = solver.makeIntVar(0.0, infinity, "v4");

        // v1 + v2 <= 3.5
        MPConstraint c1 = solver.makeConstraint(-infinity, 3.5, "c1");
        c1.setCoefficient(v1, 1);
        c1.setCoefficient(v2, 1);
        // v3 + 2*v4 <= 4
        MPConstraint c2 = solver.makeConstraint(-infinity, 4, "c2");
        c2.setCoefficient(v3, 1);
        c2.setCoefficient(v4, 2);

        // Maximize v1 + 2*v2 + 3*v3 + 4*v4
        MPObjective objective = solver.objective();
        objective.setCoefficient(v1, 1);
        objective.setCoefficient(v2, 2);
        objective.setCoefficient(v3, 3);
        objective.setCoefficient(v4, 4);
        objective.setMaximization();

        // Model manipulation
        solver.setTimeLimit(10);
        solver.setNumThreads(2);
        solver.setHint(new MPVariable[]{v1, v2, v3, v4}, new double[]{0.0, 0.0, 0.0, 0.0});

        System.out.println(solver.exportModelAsLpFormat());
//        solver.enableOutput();

        MPSolver.ResultStatus resultStatus = solver.solve();
        // Check that the problem has an optimal solution.
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
            System.out.println("Solution");
            // The objective value of the solution.
            System.out.println("objValue" + solver.objective().value());
            System.out.println("v1 = " + v1.solutionValue());
            System.out.println("v2 = " + v2.solutionValue());
            System.out.println("v3 = " + v3.solutionValue());
            System.out.println("v4 = " + v4.solutionValue());
        }

        // print solver info
        System.out.println("Number of variables = " + solver.numVariables());
        System.out.println("Number of constraints = " + solver.numConstraints());
        System.out.println("bestBound = " + solver.objective().bestBound());
        System.out.println("wallTime = " + solver.wallTime());

    }
}
