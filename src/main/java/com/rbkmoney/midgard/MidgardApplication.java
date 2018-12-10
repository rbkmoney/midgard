package com.rbkmoney.midgard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.midgard", "com.rbkmoney.dbinit"})
public class MidgardApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MidgardApplication.class, args);
    }

}
