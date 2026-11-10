package com.sqx.common.doc.properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author chengming
 * @since 2025/4/24
 */
@ConfigurationProperties(prefix = "springdoc")
public class SpringDocProperties {

    /**
     * 文档基础信息
     */
    @NestedConfigurationProperty
    private Info info;

    /**
     * 扩展文档信息
     */
    @NestedConfigurationProperty
    private ExternalDocumentation externalDocs;

    @NestedConfigurationProperty
    private Components components = null;

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public ExternalDocumentation getExternalDocs() {
        return externalDocs;
    }

    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    public Components getComponents() {
        return components;
    }

    public void setComponents(Components components) {
        this.components = components;
    }
}
