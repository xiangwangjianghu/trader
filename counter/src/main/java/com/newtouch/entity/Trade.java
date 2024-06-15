package com.newtouch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@TableName("t_trade")
public class Trade implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("uid")
    private Long uid;

    @TableField("oid")
    private Long oid;

    @TableField("code")
    private Integer code;

    @TableField("direction")
    private Integer direction;

    @TableField("price")
    private BigDecimal price;

    @TableField("count")
    private BigDecimal count;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
