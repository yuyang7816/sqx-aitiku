package com.sqx.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.*;

/**
 * @author chengming
 * @since 2024/10/21
 */
public class CodeGenerator {

    public static final String DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3306/travel-demo?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    public static final String DATASOURCE_USERNAME = "root";
    public static final String DATASOURCE_PASSWORD = "123456";
    public static final String AUTHOR = "西安省钱兄网络科技有限公司";
    public static final String OUTPUT_DIR = "sqx-modules/sqx-app/src/main/java";
    public static final String MAPPER_XML_OUTPUT_DIR = "sqx-modules/sqx-app/src/main/resources/mapper";
    public static final String PARENT_MODULE = "com.sqx.travel";
    public static final String MODULE_NAME = "deposit";
    public static final String[] TABLES = {"deposit_transaction"};
    public static final String[] TABLE_PREFIX = {""};

    public static final Boolean ENABLE_SPRINGDOC = Boolean.TRUE;
    public static final Boolean ENABLE_SWAGGER = Boolean.FALSE;

    public static void main(String[] args) {
        AutoGenerator autoGenerator = new AutoGenerator(buildDataSourceConfig());
        autoGenerator.global(buildGlobalConfig());
        autoGenerator.packageInfo(buildPackageConfig());
        autoGenerator.strategy(buildStrategyConfig());
        autoGenerator.injection(buildInjectionConfig());
        autoGenerator.execute();
    }

    // 数据库配置
    private static DataSourceConfig buildDataSourceConfig() {
        DataSourceConfig.Builder dataSourceConfigBuilder = new DataSourceConfig.Builder(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD);
        return dataSourceConfigBuilder.build();
    }

    // 全局配置
    private static GlobalConfig buildGlobalConfig() {
        GlobalConfig.Builder globalConfigBuilder = new GlobalConfig.Builder();
        globalConfigBuilder
                .author(AUTHOR) // 设置作者
                .disableOpenDir()
                .dateType(DateType.TIME_PACK)
                .commentDate("yyyy-MM-dd HH:mm:ss")
                //.enableSwagger() // 开启 swagger 模式
                .outputDir(OUTPUT_DIR); // 指定输出目录
        if (ENABLE_SPRINGDOC) {
            globalConfigBuilder.enableSpringdoc();
        }
        if (ENABLE_SWAGGER) {
            globalConfigBuilder.enableSwagger();
        }

        return globalConfigBuilder.build();
    }

    // 包配置
    private static PackageConfig buildPackageConfig() {
        PackageConfig.Builder packageConfigBuilder = new PackageConfig.Builder();
        packageConfigBuilder
                .parent(PARENT_MODULE) // 设置父包名
                .moduleName(MODULE_NAME) // 设置父包模块名
                .entity("domain.entity") // 设置实体类包名
                .service("db.service")
                .serviceImpl("db.service.impl")
                .mapper("db.mapper")
                .pathInfo(Collections.singletonMap(OutputFile.xml, MAPPER_XML_OUTPUT_DIR)) // 设置mapperXml生成路径
                .joinPackage("domain.dto");

        return packageConfigBuilder.build();
    }

    // 策略配置
    private static StrategyConfig buildStrategyConfig() {
        StrategyConfig.Builder strategyConfigBuilder = new StrategyConfig.Builder();
        strategyConfigBuilder
                .addInclude(TABLES) // 设置需要生成的表名
                .addTablePrefix(TABLE_PREFIX) // 设置过滤表前缀
                .entityBuilder()
                .enableLombok() // 启用 Lombok
                .enableTableFieldAnnotation() // 启用字段注解
                .addTableFills(new Column("create_time", FieldFill.INSERT))
                .addTableFills(new Column("update_time", FieldFill.UPDATE))
                .idType(IdType.AUTO)
                .javaTemplate("templates/entity.java.vm")
                .controllerBuilder()
                .enableRestStyle() // 启用 REST 风格
                .template("templates/controller.java.vm")
                .serviceBuilder()
                .serviceTemplate("/templates/iService.java.vm")
                .serviceImplTemplate("/templates/iServiceImpl.java.vm")
                .formatServiceFileName("I%sService")
                .formatServiceImplFileName("I%sServiceImpl");

        return strategyConfigBuilder.build();
    }

    // 注入配置
    private static InjectionConfig buildInjectionConfig() {
        InjectionConfig.Builder builder = new InjectionConfig.Builder();

        // 自定义模板，如dto
        List<CustomFile> customFileList = new ArrayList<>();

        for (String table : TABLES) {
            CustomFile saveBo = new CustomFile.Builder()
                    .fileName("SaveBo.java")
                    .templatePath("/templates/saveBo.java.vm")
                    .packageName("domain.bo")
                    .build();

            CustomFile updateBo = new CustomFile.Builder()
                    .fileName("UpdateBo.java")
                    .templatePath("/templates/updateBo.java.vm")
                    .packageName("domain.bo")
                    .build();

            CustomFile pageBo = new CustomFile.Builder()
                    .fileName("PageBo.java")
                    .templatePath("/templates/pageBo.java.vm")
                    .packageName("domain.bo")
                    .build();

            CustomFile infoBo = new CustomFile.Builder()
                    .fileName("InfoBo.java")
                    .templatePath("/templates/infoBo.java.vm")
                    .packageName("domain.bo")
                    .build();

            CustomFile vo = new CustomFile.Builder()
                    .fileName("Vo.java")
                    .templatePath("/templates/vo.java.vm")
                    .packageName("domain.vo")
                    .build();

            CustomFile service = new CustomFile.Builder()
                    .fileName("Service.java")
                    .templatePath("/templates/service.java.vm")
                    .packageName("service")
                    .build();

            CustomFile serviceImpl = new CustomFile.Builder()
                    .fileName("ServiceImpl.java")
                    .templatePath("/templates/serviceImpl.java.vm")
                    .packageName("service.impl")
                    .build();

            customFileList.add(saveBo);
            customFileList.add(updateBo);
            customFileList.add(pageBo);
            customFileList.add(infoBo);
            customFileList.add(vo);
            customFileList.add(service);
            customFileList.add(serviceImpl);
        }

        builder.customFile(customFileList);

        // 自定义变量，可以在模板中使用
        Map<String, Object> customMap = new HashMap<>();
        //customMap.put("mappingPrefix", MAPPING_PREFIX);
        builder.customMap(customMap);

        return builder.build();
    }
}
