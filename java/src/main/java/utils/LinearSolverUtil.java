package utils;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.IntStream;


@Data
public class LinearSolverUtil {

    private double infinity = Double.POSITIVE_INFINITY;
    private final int M_VALUE = 10000;

    private MPSolver solver;

    public LinearSolverUtil(MPSolver solver){
        this.solver = solver;
    }

    /**
     * 增加: 约束expr <= maxValue
     * @return MPConstraint
     */
    public MPConstraint addLessOrEqual(List<MPVariable> valueList, List<Double> coefList, double maxValue, String constraintName){
        MPConstraint c = solver.makeConstraint(-this.infinity, maxValue, constraintName);
//        IntStream.range(0, valueList.size()).
//        c1.setCoefficient(v1, 1);
//        c1.setCoefficient(v2, 1);
        return c;
    }

    /**
     * min(x_1, x_2, ... x_n)的线性表达
     * x_i为
     * @param valueList
     * @param m_value
     * @return
     */
    public MPVariable minIntToLinear(List<MPVariable> valueList, int m_value, String varName){
        // 定义minV变量
        double lb = valueList.stream().mapToDouble(MPVariable::lb).min().orElse(0.0);
        double ub = valueList.stream().mapToDouble(MPVariable::lb).max().orElse(0.0);
        MPVariable minV = solver.makeIntVar(lb, ub, varName);

        // z<=x_i
//        valueList.forEach(var -> );
        return minV;

    }

}
