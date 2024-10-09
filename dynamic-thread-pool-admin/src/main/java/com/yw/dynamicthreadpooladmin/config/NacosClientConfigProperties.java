package com.yw.dynamicthreadpooladmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 说明
 *
 * @author: yuanwen
 * @since: 2024/9/25
 */
@ConfigurationProperties(prefix = "spring.cloud.nacos.config", ignoreInvalidFields = true)
public class NacosClientConfigProperties {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 分组
     */
    private String groupId;

    /**
     * 配置文件名
     */
    private String dataId;

    /**
     * 服务地址
     */
    private String serverAddr;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
