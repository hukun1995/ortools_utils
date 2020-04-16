package utils;

import com.google.ortools.sat.Constraint;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author kun.hu
 * Cp_Model工具类
 */
public class CpModelUtil {

    public static int getMinDomain(IntVar var){
        return var.getBuilder().getDomainList().get(0).intValue();
    }

    public static int getMaxDomain(IntVar var){
        List<Long> domainList = var.getBuilder().getDomainList();
        return domainList.get(domainList.size() - 1).intValue();
    }

    /** 获得变量求和
     * @param toSumVars 需要求和的变量数组
     * @param minValue  结果变量最小值
     * @param maxValue  结果变量最大值
     * @param varName  结果变量名
     * @return  变量求和等价变量
     */
    public static IntVar sum(CpModel model, IntVar[] toSumVars, int minValue, int maxValue, String varName){
        return linearExprToIntVar(model, LinearExpr.sum(toSumVars), minValue, maxValue, varName);
    }

    public static IntVar sum(CpModel model, IntVar[] toSumVars){
        return sum(model, toSumVars, 0, Integer.MAX_VALUE, "");
    }

    /**
     * @param model  cp_model对象
     * @param expr  线性表达式
     * @param minValue  结果变量最小值
     * @param maxValue  结果变量最大值
     * @param varName  结果变量名
     * @return  线性表达式等价变量
     */
    public static IntVar linearExprToIntVar(CpModel model, LinearExpr expr, int minValue, int maxValue, String varName){
        IntVar exprVar = model.newIntVar(minValue, maxValue, varName);
        model.addEquality(exprVar, expr);
        return exprVar;
    }

    public static IntVar linearExprToIntVar(CpModel model, LinearExpr expr, String varName){
        return linearExprToIntVar(model, expr, 0, Integer.MAX_VALUE, varName);
    }

    /**
     * @param model  cp_model对象
     * @param expr  线性表达式
     * @param minValue  结果变量最小值
     * @param maxValue  结果变量最大值
     * @param varName  结果变量名
     * @return  与线性表达式绝对值相等的变量
     */
    public static IntVar linearExprToAbsIntVar(CpModel model, LinearExpr expr, int minValue, int maxValue, String varName){
        IntVar exprAbsVar = model.newIntVar(minValue, maxValue, varName);
        model.addAbsEquality(exprAbsVar, linearExprToIntVar(model, expr, Integer.MIN_VALUE, Integer.MAX_VALUE, ""));
        return exprAbsVar;
    }

    public static IntVar linearExprToAbsIntVar(CpModel model, LinearExpr expr){
        return linearExprToAbsIntVar(model, expr, 0, Integer.MAX_VALUE, "");
    }

    /**
     * @param model  cp_model对象
     * @param toProductVars  需要连乘的变量数组
     * @param minValue  结果变量最小值
     * @param maxValue  结果变量最大值
     * @param varName  结果变量名
     * @return  变量连乘等价变量
     */
    public static IntVar productVar(CpModel model, IntVar[] toProductVars, int minValue, int maxValue, String varName){
        IntVar productVar = model.newIntVar(minValue, maxValue, varName);
        model.addProductEquality(productVar, toProductVars);
        return productVar;
    }

    public static IntVar productVar(CpModel model, IntVar var1, IntVar var2, int minValue, int maxValue, String varName){
        return productVar(model, new IntVar[]{var1, var2}, minValue, maxValue, varName);
    }
    public static IntVar productVar(CpModel model, IntVar[] toProductVars){
        return productVar(model, toProductVars, 0, Integer.MAX_VALUE, "");
    }

    /**
     * @param model  cp_model对象
     * @param var1  被除数
     * @param var2 除数
     * @param minValue  结果变量最小值
     * @param maxValue  结果变量最大值
     * @param varName  结果变量名
     * @return  变量相除等价变量
     */
    public static IntVar divVar(CpModel model, IntVar var1, IntVar var2, int minValue, int maxValue, String varName){
        IntVar divVar = model.newIntVar(minValue, maxValue, varName);
        // 除数保证=max(1, var2) > 1
        IntVar var3 = max(model, new IntVar[]{var2, model.newConstant(1)}, 1, Integer.MAX_VALUE, "");
        // 除数不为0, 结果 = var1/var2; 除数为0, 结果 = var1/1
        model.addDivisionEquality(divVar, var1, var3);
        return divVar;
    }

    public static IntVar divVar(CpModel model, IntVar var1, IntVar var2){
        return divVar(model, var1, var2, 0, Integer.MAX_VALUE, "");
    }

    /**
     * 变量做差
     * @param var1 被减数
     * @param var2 减数
     * @return var1-var2对应表达式
     */
    public static LinearExpr minusExpr(IntVar var1, IntVar var2){
        return LinearExpr.scalProd(new IntVar[]{var1, var2}, new int[]{1, -1});
    }
    /**
     * 变量做差
     * @param model
     * @param var1 被减数
     * @param var2 减数
     * @return
     */
    public static IntVar minusVar(CpModel model, IntVar var1, IntVar var2, int minValue, int maxValue, String varName){
        return linearExprToIntVar(model, minusExpr(var1, var2), minValue, maxValue, "");
    }

    public static IntVar minusVar(CpModel model, IntVar var1, IntVar var2){
        return minusVar(model, var1, var2, Integer.MIN_VALUE, Integer.MAX_VALUE, "");
    }

    /**
     * 求变量数组中的最大变量
     * @param model  cp_model对象
     * @param valueVars 值变量数组
     * @return 最大变量
     */
    public static IntVar max(CpModel model, IntVar[] valueVars, int minValue, int maxValue, String varName){
        // 只有一个变量直接返回
        if(valueVars.length == 1){
           return valueVars[0];
        }
        IntVar maxVar = model.newIntVar(minValue, maxValue, varName);
        model.addMaxEquality(maxVar, valueVars);
        return maxVar;
    }

    public static IntVar max(CpModel model, IntVar[] valueVars) {
        return max(model, valueVars, 0, Integer.MAX_VALUE, "");
    }

    /**
     * 求变量数组中的最小变量
     * @param model  cp_model对象
     * @param valueVars 值变量数组
     * @return 最小变量
     */
    public static IntVar min(CpModel model, IntVar[] valueVars, int minValue, int maxValue, String varName){
        // 只有一个变量直接返回
        if(valueVars.length == 1){
            return valueVars[0];
        }
        IntVar minVar = model.newIntVar(minValue, maxValue, varName);
        model.addMinEquality(minVar, valueVars);
        return minVar;
    }

    public static IntVar min(CpModel model, IntVar[] valueVars) {
        return min(model, valueVars, 0, Integer.MAX_VALUE, "");
    }

    /**
     * 获得可选变量中的最大变量
     * @param valueVars 值变量数组
     * @param optionalVars 选择变量(0-1变量)数组
     * @return 实际最大变量
     */
    public static IntVar optionalMax(CpModel model, IntVar[] valueVars, IntVar[] optionalVars, int minValue, int maxValue, String varName){
        if(valueVars.length == 0){
            return model.newConstant(0);
        }
        IntVar[] optionalValues = new IntVar[valueVars.length];
        for(int i=0; i<valueVars.length; i++){
            // optionalValueVar = valueVar - (1-optionalVar)*maxValue
            // optional ? valueVar : valueVar - maxValue
            optionalValues[i] = linearExprToIntVar(model, LinearExpr.scalProd(new IntVar[]{valueVars[i], model.newConstant(maxValue), optionalVars[i]},
                    new int[]{1, -1, maxValue}), minValue-maxValue, maxValue, "");
        }
        return max(model, optionalValues, minValue-maxValue, maxValue, varName);
    }

    public static IntVar optionalMax(CpModel model, IntVar[] valueVars, IntVar[] optionalVars){
        int maxValue = (int) Arrays.stream(valueVars).mapToLong(CpModelUtil::getMaxDomain).max().orElse(0);
        return optionalMax(model, valueVars, optionalVars, 0, maxValue, "");
    }

    /**
     * 获得可选值中的最大值
     * @param values 值数组
     * @param optionalVars 选择变量(0-1变量)数组
     * @return 实际最大值
     */
    public static IntVar optionalMax(CpModel model, int[] values, IntVar[] optionalVars, int minValue, int maxValue, String varName){
        if(values.length == 0){
            return model.newConstant(0);
        }
        IntVar[] optionalValues = new IntVar[values.length];
        for(int i=0; i<values.length; i++){
            // 可选值=原值*(0|1)
            optionalValues[i] = linearExprToIntVar(model, LinearExpr.term(optionalVars[i], values[i]), minValue, maxValue, "");
        }
        return max(model, optionalValues, minValue, maxValue, varName);
    }

    public static IntVar optionalMax(CpModel model, int[] values, IntVar[] optionalVars){
        return optionalMax(model, values, optionalVars, 0, Integer.MAX_VALUE, "");
    }


    /**
     * 获得可选变量中的最小变量
     * @param valueVars 值变量数组
     * @param optionalVars 选择变量(0-1变量)数组
     * @return 实际最小变量
     */
    public static IntVar optionalMin(CpModel model, IntVar[] valueVars, IntVar[] optionalVars, int minValue, int maxValue, String varName){
        if(valueVars.length == 0){
            return model.newConstant(0);
        }
        IntVar[] optionalValues = new IntVar[valueVars.length];
        for(int i=0; i<valueVars.length; i++){
            // optionalValueVar = valueVar + (1-optionalVar)*maxValue
            // optional ? valueVar : valueVar + maxValue
            optionalValues[i] = linearExprToIntVar(model, LinearExpr.scalProd(new IntVar[]{valueVars[i], model.newConstant(maxValue), optionalVars[i]},
                    new int[]{1, 1, -maxValue}), "");
        }

        return min(model, optionalValues, minValue, 2*maxValue, varName);

    }

    public static IntVar optionalMin(CpModel model, IntVar[] valueVars, IntVar[] optionalVars){
        int maxValue = (int) Arrays.stream(valueVars).mapToLong(CpModelUtil::getMaxDomain).max().orElse(0);
        return optionalMin(model, valueVars, optionalVars, 0, maxValue, "");
    }

    /**
     * 获得可选值中的最小值
     * @param values 值数组
     * @param optionalVars 选择变量(0-1变量)数组
     * @param maxVar 可选值中的最大值
     * @return 实际最小值
     */
    public static IntVar optionalMin(CpModel model, int[] values, IntVar[] optionalVars, IntVar maxVar, int minValue, int maxValue, String varName){
        IntVar[] optionalValues = new IntVar[values.length];
        for(int i=0; i<values.length; i++){
            IntVar optionalValue = model.newIntVar(minValue, maxValue, "");
            // 实际存在, 可选值=自身
            model.addEquality(optionalValue, values[i]).onlyEnforceIf(optionalVars[i]);
            // 实际不存在, 可选值=最大值
            model.addEquality(optionalValue, maxVar).onlyEnforceIf(optionalVars[i].not());
            optionalValues[i] = optionalValue;
        }
        return min(model, optionalValues, minValue, maxValue, varName);
    }

    public static IntVar optionalMin(CpModel model, int[] values, IntVar[] optionalVars, IntVar maxVar){
        return optionalMin(model, values, optionalVars, maxVar, 0, Integer.MAX_VALUE, "");
    }

    /**
     * 获得可选值的平均值
     * @param values 值数组
     * @param optionalVars 选择变量(0-1变量)数组
     * @return 实际最小值
     */
    public static IntVar optionalAvg(CpModel model, int[] values, IntVar[] optionalVars, int minValue, int maxValue, String varName){
        // 值总和
        IntVar valuesSum = linearExprToIntVar(model, LinearExpr.scalProd(optionalVars, values), "");
        // 值个数
        IntVar valuesCount = linearExprToIntVar(model, LinearExpr.sum(optionalVars), "");
        // 值总和/值个数
        return divVar(model, valuesSum, valuesCount, minValue, maxValue, varName);
    }

    public static IntVar optionalAvg(CpModel model, int[] values, IntVar[] optionalVars){
        return optionalAvg(model, values, optionalVars, 0, Integer.MAX_VALUE, "");
    }

    /**
     * 获得可选变量的平均值
     * @param valueVars 值数组
     * @param optionalVars 选择变量(0-1变量)数组
     * @return 实际最小值
     */
    public static IntVar optionalAvg(CpModel model, IntVar[] valueVars, IntVar[] optionalVars, int minValue, int maxValue, String varName){
        // 值总和
        IntVar[] optionalValues = new IntVar[valueVars.length];
        for(int i=0; i<valueVars.length; i++){
            optionalValues[i] = productVar(model, new IntVar[]{valueVars[i], optionalVars[i]}, minValue, maxValue, "");
        }
        IntVar valuesSum = sum(model, optionalValues);
        // 值个数
        IntVar valuesCount = sum(model, optionalVars);
        // 值总和/值个数
        return divVar(model, valuesSum, valuesCount, minValue, maxValue, varName);
    }

    public static IntVar optionalAvg(CpModel model, IntVar[] valueVars, IntVar[] optionalVars){
        return optionalAvg(model, valueVars, optionalVars, 0, Integer.MAX_VALUE, "");
    }

    /**
     * 获得同时成立的约束A成立<=>B成立, a成立<=>b成立
     * @param trueConstraint 约束A
     * @param falseConstraint 约束a
     * @param trueAndConstraint 约束B
     * @param falseAndConstraint 约束b
     * @return 实际最小值
     */
    public static void addBoolAndConstraint(CpModel model, Constraint trueConstraint, Constraint falseConstraint,
                                           Constraint trueAndConstraint, Constraint falseAndConstraint){
        // 约束等价bool变量
        IntVar boolVar = model.newBoolVar("");
        trueConstraint.onlyEnforceIf(boolVar);
        falseConstraint.onlyEnforceIf(boolVar.not());

        trueAndConstraint.onlyEnforceIf(boolVar);
        falseAndConstraint.onlyEnforceIf(boolVar.not());
    }

    /**
     * 增加可选择位置值相邻约束
     * @param positions 位置值数组(各不相等)
     * @param optionalVars 选择变量(0-1变量)数组
     */
    public static void addOptionalNeighborConstraints(CpModel model, int[] positions, IntVar[] optionalVars) {
        // 被选中的变量个数
        IntVar selectedNum = CpModelUtil.linearExprToIntVar(model, LinearExpr.sum(optionalVars), 0, positions.length, "");
        // 获得可选位置最大值
        IntVar maxPos = optionalMax(model, positions, optionalVars);
        int[] minus = new int[]{1, -1};
        // 遍历每一个值
        for(int i=0; i<positions.length; i++){
            // 被选中的变量需要满足: maxPos - 任意pos < 选中变量数  <=> maxPos - 选中变量数 < 任意pos
            model.addLessThan(LinearExpr.scalProd(new IntVar[]{maxPos, selectedNum}, minus), positions[i])
                    .onlyEnforceIf(optionalVars[i]);
        }
    }

    /**
     * 增加位置变量相邻约束
     * @param positionVars 位置变量数组(各不相等)
     */
    public static void addNeighborConstraints(CpModel model, IntVar[] positionVars) {
        int gap = positionVars.length;
        // 获得最大位置变量
        IntVar maxPos = max(model, positionVars);
        // 获得最小位置变量
        IntVar minPos = min(model, positionVars);
        // (maxPos - minPos = 位置变量个数 - 1)
        model.addEquality(LinearExpr.scalProd(new IntVar[]{maxPos, minPos}, new int[]{1, -1}), gap-1);
    }

}
