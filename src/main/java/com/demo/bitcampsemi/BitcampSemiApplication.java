package com.demo.bitcampsemi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"data.*", "config","com.demo.bitcampsemi","websocket"})
@MapperScan("data.mapper")
public class BitcampSemiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BitcampSemiApplication.class, args);
    }

}
