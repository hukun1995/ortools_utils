package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kun.hu
 * @param <E> 状态枚举
 * @param <R> 结果明细
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StatusResult<E, R> {
    /**
     * 结果枚举
     */
    private E status;

    /**
     * 陈列结果
     */
    private R result;

}
