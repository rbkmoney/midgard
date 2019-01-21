package com.rbkmoney.midgard.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.midgard.service"})
public class MidgardClearingApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MidgardClearingApplication.class, args);
    }

}
