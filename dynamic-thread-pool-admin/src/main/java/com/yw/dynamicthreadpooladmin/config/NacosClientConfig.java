package com.yw.dynamicthreadpooladmin.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2024/9/25
 */
@Configuration
@EnableConfigurationProperties(NacosClientConfigProperties.class)
public class NacosClientConfig {

    //matchIfMissing = false,如果未填写配置，默认为不注入当前bean
    @Bean("configService")
    public ConfigService configService(NacosClientConfigProperties properties) throws NacosException {
        Properties result = new Properties();
        result.put(PropertyKeyConst.SERVER_ADDR, properties.getServerAddr());
        result.put(PropertyKeyConst.NAMESPACE, properties.getNamespace());
        result.put(PropertyKeyConst.USERNAME, properties.getUsername());
        result.put(PropertyKeyConst.PASSWORD, properties.getPassword());
        return NacosFactory.createConfigService(result);
    }

}