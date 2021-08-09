package utils;

import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.junit.Assert.*;

public class LinearSolverUtilTest {
    static {
        ORToolsLoader.load("/Users/bianlifeng/my_project/ortools_utils/java/src/lib/libjniortools.jnilib");
    }

    @Test
    public void addLessOrEqual() {
        MPSolver solver = new MPSolver(
                "LinearProgrammingExample", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);

        LinearSolverUtil solverUtil = new LinearSolverUtil(solver);
        // v1,v2是 非负的连续变量
        MPVariable v1 = solver.makeNumVar(0.0, 2, "v1");
        solverUtil.addLessOrEqual(new MPVariable[]{v1}, new double[]{1.0}, 1, "");
        // Maximize v1
        MPObjective objective = solver.objective();
        objective.setCoefficient(v1, 1);
        objective.setMinimization();

        MPSolver.ResultStatus resultStatus = solver.solve();

        System.out.println("v1 = " + v1.solutionValue());
    }

    @Test
    public void minIntToLinear() {
        MPSolver solver = new MPSolver(
                "LinearProgrammingExample", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);

        LinearSolverUtil solverUtil = new LinearSolverUtil(solver);
        // v1,v2是 非负的连续变量
        MPVariable v1 = solverUtil.makeIntConstant(2.0);
        MPVariable v2 = solverUtil.makeIntConstant(1.0);
        MPVariable v3 = solverUtil.makeIntConstant(3.0);
        MPVariable v = solverUtil.min(new MPVariable[]{v1, v2, v3}, "");

        MPSolver.ResultStatus resultStatus = solver.solve();


        System.out.println("v = " + v.solutionValue());
    }

    @Test
    public void test() {
        CpModel model = new CpModel();
        IntVar a1 = model.newConstant(3);
        IntVar a2 = model.newIntVar(0, 10, "");
        model.addGreaterOrEqualWithOffset(a2, a1, 1);
//        IntVar a2 = model.newConstant(1);
//        IntVar a3 = model.newConstant(1);
//        try {
//            model.addCircuit(new int[]{2,3,4}, new int[]{1,2,3}, new IntVar[]{a1, a2, a3});
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        IntVar res = model.newIntVar(0, 10, "");
//        model.addElement(a1, new int[]{5,6,7}, res);

        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);
        System.out.println("finish");
        System.out.println("a2: " + solver.value(a2));
//        System.out.println("a2: " + solver.value(a2));
//        System.out.println("a3: " + solver.value(a3));
    }
}