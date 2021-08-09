package model;

import lombok.Data;
import lombok.Getter;


public enum AlgorithmResultEnum {
    // 最优解
    OPTIMAL(0, "最优解"),
    // 可行解
    FEASIBLE(1, "可行解"),
    // 无解
    INFEASIBLE(2, "无解"),
    // 未知(时长偏短，尚未求得初始可行解)
    UNKNOWN(3, "未知");

    /**
     * 结果码
     */
    @Getter
    private int resultCode;

    /**
     * 结果描述
     */
    @Getter
    private String resultDesc;


    /**
     * 构造方法
     * @param resultCode
     * @param resultDesc
     */
    AlgorithmResultEnum(int resultCode, String resultDesc){
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
    }
}
