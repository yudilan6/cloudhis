package com.dyhospital.cloudhis.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**使用环境中的配置覆盖基本配置，如果无环境配置，则获取默认配置。
 * @author panda
 */
@Configuration
@PropertySource(value = {
        "classpath:/config/redis.properties",
        "classpath:/config/redis-${spring.profiles.active}.properties"})
public class ApplicationConfig {
}
