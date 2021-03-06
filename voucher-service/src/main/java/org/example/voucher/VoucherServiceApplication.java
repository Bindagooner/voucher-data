package org.example.voucher;

import org.example.voucher.configuration.RabbitProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableBinding(RabbitProcessor.class)
public class VoucherServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoucherServiceApplication.class);
    }
}
