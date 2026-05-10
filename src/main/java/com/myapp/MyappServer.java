package com.myapp;

import com.myapp.util.LoggingInterceptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class MyappServer implements WebMvcConfigurer
{

    public static void main(String[] args)
    {
        SpringApplication.run(MyappServer.class, args);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new LoggingInterceptor()).addPathPatterns("/**");
    }


    @Bean
    public OpenAPI customOpenAPI()
    {
        return new OpenAPI()
            .info(new Info()
                .title("WEZVATECH DEVSECOPS SAMPLE PROJECT")
                .version("Version 1.0 - mw")
                .description("This is a sample project for Devsecops CICD pipeline.")
                .termsOfService("urn:tos")
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")));
    }
}
