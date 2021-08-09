package utils;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;


@Data
public class LinearSolverUtil {

    public static final double INFINITY = Double.POSITIVE_INFINITY;
    public static final int M_VALUE = 10000;

    private MPSolver solver;

    public LinearSolverUtil(MPSolver solver){
        this.solver = solver;
    }

    /**
     *
     */
    public MPVariable makeIntConstant(double value){
        return this.solver.makeIntVar(value, value, "");
    }

    /**
     *
     */
    public MPVariable makeNumConstant(double value){
        return this.solver.makeNumVar(value, value, "");
    }

    /**
     * add constraint: expr <= maxValue
     * @param varArr
     * @param coefArr
     * @param maxValue
     * @param constraintName
     * @return MPConstraint
     */
    public MPConstraint addLessOrEqual(MPVariable[] varArr, double[] coefArr, double maxValue, String constraintName){
        MPConstraint c = this.solver.makeConstraint(-INFINITY, maxValue, constraintName);
        IntStream.range(0, varArr.length).forEach(i -> c.setCoefficient(varArr[i], coefArr[i]));
        return c;
    }

    /**
     * add constraint: expr >= minValue
     * @return MPConstraint
     */
    public MPConstraint addGreaterOrEqual(MPVariable[] varArr, double[] coefArr, double minValue, String constraintName){
        MPConstraint c = this.solver.makeConstraint(minValue, INFINITY, constraintName);
        IntStream.range(0, varArr.length).forEach(i -> c.setCoefficient(varArr[i], coefArr[i]));
        return c;
    }

    /**
     * add constraint: expr = minValue
     * @return MPConstraint
     */
    public MPConstraint addEqual(MPVariable[] varArr, double[] coefArr, double targetValue, String constraintName){
        MPConstraint c = this.solver.makeConstraint(targetValue, targetValue, constraintName);
        IntStream.range(0, varArr.length).forEach(i -> c.setCoefficient(varArr[i], coefArr[i]));
        return c;
    }

    /**
     * min(x_1, x_2, ... x_n)的线性表达
     * x_i为
     * @param varArr
     * @param mValue
     * @return
     */
    public MPVariable min(MPVariable[] varArr, int mValue, String varName){
        // 定义minV变量
        double lb = Arrays.stream(varArr).mapToDouble(MPVariable::lb).min().orElse(0.0);
        double ub = Arrays.stream(varArr).mapToDouble(MPVariable::lb).max().orElse(0.0);
        MPVariable minV = solver.makeIntVar(lb, ub, varName);
        // 定义y_i变量
        MPVariable[] ys = this.solver.makeBoolVarArray(varArr.length);

        // minV <= x_i => minV-x_i <= 0
        IntStream.range(0, varArr.length).forEach(i -> addLessOrEqual(new MPVariable[]{minV, varArr[i]}, new double[]{1.0, -1.0}, 0, varName));
        // minV >= x_i-yi*M => minV-x_i+yi*M >= 0
        IntStream.range(0, varArr.length).forEach(i -> addGreaterOrEqual(new MPVariable[]{minV, varArr[i], ys[i]}, new double[]{1.0, -1.0, mValue}, 0, varName));
        // sum{y_i}=|ys|-1
        addEqual(ys, IntStream.range(0, ys.length).mapToDouble(i -> 1.0).toArray(), ys.length-1, "");
        return minV;

    }

    public MPVariable min(MPVariable[] varArr,  String varName){
        return min(varArr, M_VALUE, varName);
    }

}
