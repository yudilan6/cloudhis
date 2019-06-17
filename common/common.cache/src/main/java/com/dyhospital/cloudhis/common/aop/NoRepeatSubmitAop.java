package com.dyhospital.cloudhis.common.aop;

import com.dyhospital.cloudhis.common.annotation.NoRepeatSubmit;
import com.dyhospital.cloudhis.common.redis.lock.RedisLockClient;
import com.dyhospital.cloudhis.common.web.constants.BaseResultCode;
import com.dyhospital.cloudhis.common.web.exception.reg.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @program: yyzlpt_code
 * @description: 拦截重复请求aop
 * @author: linhao
 * @create: 2019/02/20 17:10
 **/
@Aspect
@Component
@Slf4j
@PropertySource(value = {
        "classpath:/config/norepeatsubmit-${spring.profiles.active}.properties"})
public class NoRepeatSubmitAop {

    @Autowired
    RedisLockClient redisLockClient;

    @Value("${norepeatsubmit.enabled}")
    private Boolean enabled;

    public static final String HEADER_ACCESS_TOKEN = "x-access-token";
    public static final String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    public static final String CONTROLLER = "org.springframework.stereotype.Controller";

    private static String[] getAllFiels(Object object) {
        Class<?> clazz = object.getClass();

        ArrayList fielList;
        int length = 0;
        for (fielList = new ArrayList(); Object.class != clazz; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            length = fields.length;

            for (int i = 0; i < length; ++i) {
                Field f = fields[i];
                fielList.add(f.getName());
            }
        }
        String[] fields = new String[length];
        return (String[]) fielList.toArray(fields);
    }

    private static String getParam(Object[] args, String[] uniqueProperty) throws Exception {
        String param = "";
        if (uniqueProperty.length > 0) {
            for (Object object : args) {
                String[] fileds = getAllFiels(object);
                for (String filed : fileds) {
                    for (String unique : uniqueProperty) {
                        if (unique.toLowerCase().equals(filed.toLowerCase())) {
                            param += BeanUtils.getProperty(object, filed);
                            break;
                        }
                    }
                }
            }
        }
        return param;
    }


    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint pjp, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        StringBuffer stringBuffer = new StringBuffer();
        if (null != enabled && enabled) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            String token = request.getHeader(HEADER_ACCESS_TOKEN);
            Annotation[] annotations = pjp.getTarget().getClass().getAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getName().equals(REST_CONTROLLER) || annotation.annotationType().getName().equals(CONTROLLER)) {
                    String[] uniqueProperty = noRepeatSubmit.uniqueProperty();
                    String param = "";
                    //  唯一属性参数长度大于零
                    if (uniqueProperty.length > 0) {
                        Object[] args = pjp.getArgs();
                        param = getParam(args, uniqueProperty);
                    }
                    String ip = getWapWGIP(request);
                    stringBuffer = new StringBuffer("SYS:NOREPEATSUBMIT:");
                    stringBuffer.append(ip).append(param).append(token).append(request.getRequestURL());
                    getLock(stringBuffer.toString(),noRepeatSubmit.lockTime());
                    break;
                }
            }

        }
        Object object = null;
        try {
            object = pjp.proceed();
        } finally {
            redisLockClient.unLock(stringBuffer.toString());
        }
        return object;
    }

    private void getLock(String key,long lockTime) {

        try {
            redisLockClient.getLock(key, lockTime);
        } catch (JedisConnectionException e) {
            throw new BizException(BaseResultCode.SYSTEM_ERROR, e);
        } catch (Exception e) {
            throw new BizException(BaseResultCode.REPEATSUBMIT, e);
        }


    }


    /**
     * 从Http Header中解析访问wap网关IP <功能详细描述>
     *
     * @param request
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    private static String getWapWGIP(HttpServletRequest request) {
        String wapWGIP = request.getHeader("x-forwarded-for");

        if (wapWGIP == null || wapWGIP.length() == 0 || "unknown".equalsIgnoreCase(wapWGIP)) {
            wapWGIP = request.getHeader("Proxy-Client-IP");
        }
        if (wapWGIP == null || wapWGIP.length() == 0 || "unknown".equalsIgnoreCase(wapWGIP)) {
            wapWGIP = request.getHeader("WL-Proxy-Client-IP");
        }
        if (wapWGIP == null || wapWGIP.length() == 0 || "unknown".equalsIgnoreCase(wapWGIP)) {
            wapWGIP = request.getRemoteAddr();
        }

        return wapWGIP;
    }
}
