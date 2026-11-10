package com.sqx.common.doc;

import com.sqx.common.doc.properties.SpringDocProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author chengming
 * @since 2024/10/17
 */
@Configuration
@EnableConfigurationProperties(SpringDocProperties.class)
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI(SpringDocProperties springDocProperties){
        Info info = springDocProperties.getInfo();
        ExternalDocumentation externalDocs = springDocProperties.getExternalDocs();
        Components components = springDocProperties.getComponents();

        // 返回信息
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(info);
        openAPI.externalDocs(externalDocs);
        openAPI.components(components);

        Set<String> keySet = components.getSecuritySchemes().keySet();
        List<SecurityRequirement> list = new ArrayList<>();
        SecurityRequirement securityRequirement = new SecurityRequirement();
        keySet.forEach(securityRequirement::addList);
        list.add(securityRequirement);
        openAPI.security(list);

        return openAPI;
    }
}
