package com.newtouch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock")
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "code")
    private Integer code;

    @TableField("name")
    private String name;

    @TableField("abbr_name")
    private String abbrName;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Transient
    private String loginToken;

    @Transient
    private String loginMsg;
}
