package com.example.hedera.consensus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.hedera")
public class ConsensusApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsensusApplication.class, args);
    }

}
