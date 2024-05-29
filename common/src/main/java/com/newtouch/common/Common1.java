package com.newtouch.common;

import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;

/**
 * <p></p>
 *
 * @author CC
 * @since 2023/10/7
 */
//@Data
//@Component
public class Common1 {

    public static Object v1(){

        int i = DateUtil.ageOfNow(new Date());
        System.out.println(i);

        boolean blank = StringUtils.isBlank("");
        System.out.println(blank);

        return i;
    }

    public Object v2(){

        int i = DateUtil.ageOfNow(new Date());
        System.out.println(i);

        return i;
    }

    public Common1() {
    }




}
