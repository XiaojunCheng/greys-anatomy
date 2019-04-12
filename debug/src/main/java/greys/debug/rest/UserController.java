package greys.debug.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author Xiaojun.Cheng
 * @date 2018/8/10
 */
@RestController
public class UserController {

    @GetMapping(path = "/showTrace/{username}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String showTrace(@PathVariable String username) {
        return username + "_" + new Date();
    }

}
