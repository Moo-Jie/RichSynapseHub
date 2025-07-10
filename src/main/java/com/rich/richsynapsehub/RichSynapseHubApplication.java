package com.rich.richsynapsehub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.rich.richsynapsehub.mapper")
public class RichSynapseHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(RichSynapseHubApplication.class, args);
    }

}
