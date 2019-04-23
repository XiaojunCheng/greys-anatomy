package com.github.ompc.greys.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chengxiaojun
 */
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        List<Test.Pojo> list = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Test.Pojo pojo = new Test.Pojo();
            pojo.setName("name " + i);
            pojo.setAge(i + 2);
            list.add(pojo);
        }
        Test test = new Test();
        test.test(list);
        return "Hello World! Welcome to visit .com!";
    }
}
