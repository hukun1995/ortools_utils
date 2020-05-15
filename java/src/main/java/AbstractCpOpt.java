//import com.google.ortools.sat.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class AbstractCpOpt {
//
//    /**
//     * 陈列配置
//     */
//    protected DisplayConfig displayConfig;
//
//    /**
//     * 模型
//     */
//    protected CpModel model;
//
//    /**
//     * 求解器
//     */
//    protected CpSolver solver;
//
//    /**
//     * 常量1
//     */
//    protected IntVar constOne;
//
//    /**
//     * 常量0
//     */
//    protected IntVar constZero;
//
//    /**
//     * 目标权重Map
//     * key=目标名, value={key=变量, value=权重}
//     */
//    protected Map<String, Map<IntVar, Integer>> objVarWeightMap;
//
//    /**
//     * 尺寸取整
//     * @param orgValue
//     * @return
//     */
//    protected static int parseSize(float orgValue){
//        return MathUtil.decToInt(orgValue, PrecisionConf.SIZE_DEC_NUM);
//    }
//
//    /**
//     * 获得psd权重
//     */
//    protected int getPsdWeight(Sku sku, WeightConfig weightConfig){
//        int psdWeight = (int) (sku.getPsdAmount() * weightConfig.getPsdWeight());
//        if(psdWeight <= WeightConfig.MAX_PSD_AMOUNT){
//            return psdWeight;
//        }
//        // 超出部分/10
//        return WeightConfig.MAX_PSD_AMOUNT + (psdWeight - WeightConfig.MAX_PSD_AMOUNT)/10;
//    }
//
//    /**
//     * 初始化
//     * @param oldDisplay 原陈列
//     * @param info 补充信息
//     * @param displayConfig 陈列配置
//     */
//    protected abstract void init(D oldDisplay, I info, DisplayConfig displayConfig);
//
//    /**
//     * 前(预)剪枝
//     */
//    protected void preCut(){}
//
//    /**
//     * 返回模型的变量,约束信息
//     * @param s 说明
//     * @return
//     */
//    protected void logModelInfo(String s){
//        log.info("{}, 共计: {} 个变量, {} 个约束", s,
//                this.model.getBuilder().getVariablesCount(), this.model.getBuilder().getConstraintsCount());
//    }
//
//
//    /**
//     * 变量创建抽象方法
//     */
//    protected abstract void doCreateVars();
//
//    /**
//     * 创建变量
//     */
//    private void createVars(){
//        log.info("正在进行变量创建...");
//
//        // 常量赋值
//        this.constOne = this.model.newConstant(1);
//        this.constZero = this.model.newConstant(0);
//
//        // 变量创建
//        doCreateVars();
//
//        logModelInfo("变量创建完成");
//    }
//
//    /**
//     * 中间变量计算抽象方法
//     */
//    protected abstract void doComputeInterVar();
//
//    /**
//     * 计算中间变量
//     */
//    private void computeInterVar(){
//        log.info("正在进行中间变量计算...");
//
//        // 计算中间变量
//        doComputeInterVar();
//
//        logModelInfo("中间变量计算完成");
//    }
//
//    /**
//     * 约束增加抽象方法
//     */
//    protected abstract void doAddConstraints();
//
//    /**
//     * 增加约束
//     */
//    private void addConstraints(){
//        log.info("正在添加约束...");
//
//        //增加约束
//        doAddConstraints();
//
//        logModelInfo("约束添加完毕");
//    }
//
//    /**
//     * 刷新目标变量-权重
//     */
//    void refreshObjVarWeight(String objName, IntVar var, int weight){
//        // 权重为0, 直接返回
//        if(weight == 0){
//            return;
//        }
//        this.objVarWeightMap.putIfAbsent(objName, new HashMap<>(DefaultValueConf.DEFAULT_HASH_SIZE));
//        Map<IntVar, Integer> varWeight = this.objVarWeightMap.get(objName);
//        varWeight.put(var, varWeight.getOrDefault(var, 0) + weight);
//    }
//
//    /**
//     * 目标设置抽象方法
//     */
//    protected abstract void doSetObjective();
//
//    /**
//     * 获得目标的线性表达式
//     * @return 目标的线性表达式
//     */
//    LinearExpr getObjExpr(){
//        // 保序
//        List<IntVar> objVarList = new ArrayList<>();
//        List<Integer> objWeightList = new ArrayList<>();
//        this.objVarWeightMap.values().forEach(varWeight -> varWeight.forEach((var, weight) -> {
//            objVarList.add(var);
//            objWeightList.add(weight);
//        }));
//        return LinearExpr.scalProd(objVarList.toArray(new IntVar[0]), objWeightList.stream().mapToInt(i->i).toArray());
//    }
//
//    /**
//     * 获得目标的线性表达式
//     * @return 目标的线性表达式
//     */
//    LinearExpr getObjExpr(String objName){
//        // 保序
//        List<IntVar> objVarList = new ArrayList<>();
//        List<Integer> objWeightList = new ArrayList<>();
//        this.objVarWeightMap.get(objName).forEach((var, weight) -> {
//            objVarList.add(var);
//            objWeightList.add(weight);
//        });
//        return LinearExpr.scalProd(objVarList.toArray(new IntVar[0]), objWeightList.stream().mapToInt(i->i).toArray());
//    }
//
//    /**
//     * 设置目标
//     */
//    private void setObjective(){
//        log.info("正在设置目标...");
//
//        // 初始化权重Map
//        this.objVarWeightMap = new HashMap<>(DefaultValueConf.DEFAULT_HASH_SIZE);
//
//        //增加约束
//        doSetObjective();
//
//        logModelInfo("目标添加完毕");
//    }
//
//    protected void setInitialSolution(){}
//
//    protected void doSetSolverParam(){}
//
//    /**
//     * 求解器参数设置
//     */
//    private void setSolverParam(){
//        // 默认配置
//        RunningConfig runningConfig = this.displayConfig.getRunningConfig();
//        // 设置运行时长上限(和货架主题数成正比)
//        this.solver.getParameters().setMaxTimeInSeconds(runningConfig.getMaxRunningSeconds());
//        // 设置是否打计算日志
//        this.solver.getParameters().setLogSearchProgress(runningConfig.isLogSearchProgress());
//        // 设置并行度
//        this.solver.getParameters().setNumSearchWorkers(runningConfig.getNumWorkers());
//
//        // 个性化配置
//        doSetSolverParam();
//    }
//
//    /**
//     * 尝试求解
//     * 进行cpmodel求解,为防止异常进行允许一次重试
//     * @return 求解状态枚举
//     */
//    private CpSolverStatus trySolve(){
//        // 求解器参数设置
//        setSolverParam();
//        // 设置回调函数
//        SolutionCallBack callBack = getSolutionCallBack();
//        CpSolverStatus status;
//        log.info("开始求解...");
////        CpSolverStatus status = solver.solve(model);
//        try{
//            status = solver.solveWithSolutionCallback(model, callBack);
//        }catch (Exception e1){
//            log.info("cp_solver求解异常");
//            e1.printStackTrace();
//            // 重试
//            try{
//                // 停止1min
//                Thread.sleep(60000);
//                log.info("开始求解重试");
//                status = solver.solveWithSolutionCallback(model, callBack);
//            }catch (Exception e2){
//                status = CpSolverStatus.UNKNOWN;
//                log.info("求解重试失败");
//            }
//
//        }
//        log.info("求解完成");
//        return status;
//    }
//
//    private SolutionCallBack getSolutionCallBack(){
//        return new SolutionCallBack(15, 1.0, 0.0);
//    }
//
//    /**
//     * 打印目标值明细
//     */
//    private void logObjDetail(){
//        if(this.objVarWeightMap == null || this.objVarWeightMap.isEmpty()){
//            return;
//        }
//        log.info("objDetail: {}", this.objVarWeightMap.entrySet().stream().map(kv -> {
//            int objValue = (int) kv.getValue().entrySet().stream().mapToLong(ow -> solver.value(ow.getKey()) * ow.getValue()).sum();
//            return String.format("%s: %s", kv.getKey(), objValue);
//        }).collect(Collectors.joining(", ")));
//    }
//
//    /**
//     * 结果转化抽象方法
//     */
//    protected abstract D parseResult();
//
//    /**
//     * 默认结果抽象方法
//     */
//    protected abstract D getDefaultResult();
//
//    /**
//     * 最优化算法入口函数
//     * @param oldDisplay 原陈列
//     * @param info 补充信息
//     * @param displayConfig 陈列配置
//     * @return 算法结果状态,和新的陈列
//     */
//    @Override
//    public AlgorithmResult<AlgorithmResultEnum, D> display(D oldDisplay, I info, DisplayConfig displayConfig){
//        // 初始化
//        init(oldDisplay, info, displayConfig);
//
//        // create the cp model
//        this.model = new CpModel();
//
//        // 前剪枝
//        preCut();
//
//        // 变量创建
//        createVars();
//
//        // 中间变量计算
//        computeInterVar();
//
//        // 设置约束
//        addConstraints();
//
//        // 设置目标量
//        setObjective();
//
//        // 设置初始解
//        setInitialSolution();
//
//        // 求解器初始化
//        this.solver = new CpSolver();
//
//        // 尝试求解
//        CpSolverStatus status = trySolve();
//
//        // 结果枚举
//        AlgorithmResultEnum algorithmResultEnum;
//        // 新陈列
//        D displayResult;
//
//        // 最优或可行解
//        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//            if (status == CpSolverStatus.OPTIMAL){
//                algorithmResultEnum = AlgorithmResultEnum.OPTIMAL;
//            }else{
//                algorithmResultEnum = AlgorithmResultEnum.FEASIBLE;
//            }
//            // 打印: 求解状态, 理论最优目标值, 当前目标值, 求解时间
//            log.info("{}, objBound: {}, obj: {}, wallTime: {}",
//                    algorithmResultEnum.getResultDesc(), solver.bestObjectiveBound(), solver.objectiveValue(), solver.wallTime());
//            // 打印各目标值明细
//            logObjDetail();
//            // 结果转化
//            displayResult = parseResult();
//        }else{
//            algorithmResultEnum = AlgorithmResultEnum.INFEASIBLE;
//            log.info("{}", algorithmResultEnum.getResultDesc());
//            displayResult = getDefaultResult();
//        }
//        return new AlgorithmResult<>(algorithmResultEnum, displayResult);
//    }
//}
