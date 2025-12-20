package com.ysmjjsy.goya.auth.server;

import com.ysmjjsy.goya.component.common.exception.ParamsValidationException;
import com.ysmjjsy.goya.component.web.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 22:18
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("get")
    public Response<Void> test(){
        throw new ParamsValidationException();
//        String s = I18nResolver.resolveEnum(ResponseCodeEnum.PARAMS_VALIDATION_ERROR);
//        log.warn("s------------>{}",s);
//        return Response.success();
    }
}
