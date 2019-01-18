package com.rbkmoney.midgard.adapter.mts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.midgard.adapter.mts"})
public class MidgardAdapterMtsApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MidgardAdapterMtsApplication.class, args);
    }

}