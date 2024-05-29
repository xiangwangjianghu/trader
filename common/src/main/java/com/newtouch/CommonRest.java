package com.newtouch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p></p>
 *
 * @author CC
 * @since 2023/10/7
 */
@RestController
@RequestMapping("/common")
public class CommonRest {

    @GetMapping
    public String ss(){
        return "heeeeeeeee";
    }


}
