package top.crush.mall.api.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Crush
 * @date 2021/4/23
 * @dercsiption 添加购物项param
 */
@Data
public class SaveCartItemParam implements Serializable {
    @ApiModelProperty("商品数量")
    private Integer goodsCount;
    @ApiModelProperty("商品id")
    private Long goodsId;
}