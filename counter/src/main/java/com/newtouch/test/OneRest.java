package com.newtouch.test;

import cn.hutool.core.date.DateUtil;
import com.newtouch.common.Common1;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * <p></p>
 *
 * @author CC
 * @since 2023/10/7
 */
@RestController
@RequestMapping("/oneRest")
public class OneRest {

    @GetMapping
    public String ss (){

        Common1 common1 = new Common1();
        Object o = Common1.v1();

        int i = DateUtil.ageOfNow(new Date());
//        stringutils

        return "oneone";
    }





}
