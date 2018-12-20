package com.rbkmoney.midgard.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.midgard.base"})
public class MidgardClearingApplication {

    //TODO: разобраться Failed to load property source from location 'classpath:/application.yml'
    //TODO: где-то midgard, а гле-то midgard-clearing

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MidgardClearingApplication.class, args);
    }

}
