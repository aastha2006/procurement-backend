package com.example.procurement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.DependsOn;


@Configuration
//@DependsOn("externalfileconfig")
public class LoggerConfig {

    @Bean
    Logger logger() {
        return LoggerFactory.getLogger("application");
    }

    // @Bean
    // MailTrigger mailTrigger() {
    //     return new MailTrigger();
    // }
}
