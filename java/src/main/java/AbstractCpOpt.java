import com.google.ortools.sat.*;
import lombok.extern.slf4j.Slf4j;
import model.AlgorithmResultEnum;
import model.StatusResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractCpOpt<R> {

    /**
     * 模型
     */
    protected CpModel model;

    /**
     * 求解器
     */
    protected CpSolver solver;

    /**
     * 常量1
     */
    protected IntVar constOne;

    /**
     * 常量0
     */
    protected IntVar constZero;

    /**
     * 目标权重Map
     * key=目标名, value={key=变量, value=权重}
     */
    protected Map<String, Map<IntVar, Integer>> objVarWeightMap;

    /**
     * 初始化
     */
    protected abstract void init();

    /**
     * 前(预)剪枝
     */
    protected void preCut(){}

    /**
     * 返回模型的变量,约束信息
     * @param s 说明
     * @return
     */
    protected void logModelInfo(String s){
        log.info("{}, 共计: {} 个变量, {} 个约束", s,
                this.model.getBuilder().getVariablesCount(), this.model.getBuilder().getConstraintsCount());
    }


    /**
     * 变量创建抽象方法
     */
    protected abstract void doCreateVars();

    /**
     * 创建变量
     */
    private void createVars(){
        log.info("正在进行变量创建...");

        // 常量赋值
        this.constOne = this.model.newConstant(1);
        this.constZero = this.model.newConstant(0);

        // 变量创建
        doCreateVars();

        logModelInfo("变量创建完成");
    }

    /**
     * 中间变量计算抽象方法
     */
    protected abstract void doComputeInterVar();

    /**
     * 计算中间变量
     */
    private void computeInterVar(){
        log.info("正在进行中间变量计算...");

        // 计算中间变量
        doComputeInterVar();

        logModelInfo("中间变量计算完成");
    }

    /**
     * 约束增加抽象方法
     */
    protected abstract void doAddConstraints();

    /**
     * 增加约束
     */
    private void addConstraints(){
        log.info("正在添加约束...");

        //增加约束
        doAddConstraints();

        logModelInfo("约束添加完毕");
    }

    /**
     * 刷新目标变量-权重
     */
    void refreshObjVarWeight(String objName, IntVar var, int weight){
        // 权重为0, 直接返回
        if(weight == 0){
            return;
        }
        this.objVarWeightMap.putIfAbsent(objName, new HashMap<>(16));
        Map<IntVar, Integer> varWeight = this.objVarWeightMap.get(objName);
        varWeight.put(var, varWeight.getOrDefault(var, 0) + weight);
    }

    /**
     * 目标设置抽象方法
     */
    protected abstract void doSetObjective();

    /**
     * 获得目标的线性表达式
     * @return 目标的线性表达式
     */
    LinearExpr getObjExpr(){
        // 保序
        List<IntVar> objVarList = new ArrayList<>();
        List<Integer> objWeightList = new ArrayList<>();
        this.objVarWeightMap.values().forEach(varWeight -> varWeight.forEach((var, weight) -> {
            objVarList.add(var);
            objWeightList.add(weight);
        }));
        return LinearExpr.scalProd(objVarList.toArray(new IntVar[0]), objWeightList.stream().mapToInt(i->i).toArray());
    }

    /**
     * 获得目标的线性表达式
     * @return 目标的线性表达式
     */
    LinearExpr getObjExpr(String objName){
        // 保序
        List<IntVar> objVarList = new ArrayList<>();
        List<Integer> objWeightList = new ArrayList<>();
        this.objVarWeightMap.get(objName).forEach((var, weight) -> {
            objVarList.add(var);
            objWeightList.add(weight);
        });
        return LinearExpr.scalProd(objVarList.toArray(new IntVar[0]), objWeightList.stream().mapToInt(i->i).toArray());
    }

    /**
     * 设置目标
     */
    private void setObjective(){
        log.info("正在设置目标...");

        // 初始化权重Map
        this.objVarWeightMap = new HashMap<>();

        //增加约束
        doSetObjective();

        logModelInfo("目标添加完毕");
    }

    protected void setInitialSolution(){}

    protected void doSetSolverParam(){}

    /**
     * 求解器参数设置
     */
    private void setSolverParam(){
        // 默认配置
        // 设置运行时长上限(和货架主题数成正比)
        this.solver.getParameters().setMaxTimeInSeconds(100);
        // 设置是否打计算日志
        this.solver.getParameters().setLogSearchProgress(true);
        // 设置并行度
        this.solver.getParameters().setNumSearchWorkers(8);

        // 个性化配置
        doSetSolverParam();
    }

    /**
     * 尝试求解
     * 进行cpmodel求解,为防止异常进行允许一次重试
     * @return 求解状态枚举
     */
    private CpSolverStatus trySolve(){
        // 求解器参数设置
        setSolverParam();
        // 设置回调函数
        CpSolverSolutionCallback callBack = getSolutionCallBack();
        CpSolverStatus status;
        log.info("开始求解...");
//        CpSolverStatus status = solver.solve(model);
        try{
            status = solver.solveWithSolutionCallback(model, callBack);
        }catch (Exception e1){
            log.info("cp_solver求解异常");
            e1.printStackTrace();
            // 重试
            try{
                // 停止1min
                Thread.sleep(60000);
                log.info("开始求解重试");
                status = solver.solveWithSolutionCallback(model, callBack);
            }catch (Exception e2){
                status = CpSolverStatus.UNKNOWN;
                log.info("求解重试失败");
            }

        }
        log.info("求解完成");
        return status;
    }

    private CpSolverSolutionCallback getSolutionCallBack(){
        return null;
    }

    /**
     * 打印目标值明细
     */
    private void logObjDetail(){
        if(this.objVarWeightMap == null || this.objVarWeightMap.isEmpty()){
            return;
        }
        log.info("objDetail: {}", this.objVarWeightMap.entrySet().stream().map(kv -> {
            int objValue = (int) kv.getValue().entrySet().stream().mapToLong(ow -> solver.value(ow.getKey()) * ow.getValue()).sum();
            return String.format("%s: %s", kv.getKey(), objValue);
        }).collect(Collectors.joining(", ")));
    }

    /**
     * 结果转化抽象方法
     */
    protected abstract R parseResult();

    /**
     * 默认结果抽象方法
     */
    protected abstract R getDefaultResult();

    /**
     * 最优化算法入口函数
     */
    public StatusResult<AlgorithmResultEnum, R> execute(){
        // 初始化
        init();

        // create the cp model
        this.model = new CpModel();

        // 前剪枝
        preCut();

        // 变量创建
        createVars();

        // 中间变量计算
        computeInterVar();

        // 设置约束
        addConstraints();

        // 设置目标量
        setObjective();

        // 设置初始解
        setInitialSolution();

        // 求解器初始化
        this.solver = new CpSolver();

        // 尝试求解
        CpSolverStatus status = trySolve();

        // 结果枚举
        AlgorithmResultEnum algorithmResultEnum;
        // 新陈列
        R result;

        // 最优或可行解
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            if (status == CpSolverStatus.OPTIMAL){
                algorithmResultEnum = AlgorithmResultEnum.OPTIMAL;
            }else{
                algorithmResultEnum = AlgorithmResultEnum.FEASIBLE;
            }
            // 打印: 求解状态, 理论最优目标值, 当前目标值, 求解时间
            log.info("{}, objBound: {}, obj: {}, wallTime: {}",
                    algorithmResultEnum.getResultDesc(), solver.bestObjectiveBound(), solver.objectiveValue(), solver.wallTime());
            // 打印各目标值明细
            logObjDetail();
            // 结果转化
            result = parseResult();
        }else{
            algorithmResultEnum = AlgorithmResultEnum.INFEASIBLE;
            log.info("{}", algorithmResultEnum.getResultDesc());
            result = getDefaultResult();
        }
        return new StatusResult<>(algorithmResultEnum, result);
    }
}
