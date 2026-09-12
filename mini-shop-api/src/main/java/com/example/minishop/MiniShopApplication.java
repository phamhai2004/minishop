package com.example.minishop;

import com.example.minishop.config.CloudinaryProperties;
import com.example.minishop.config.JwtProperties;
import com.example.minishop.config.QdrantProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        CloudinaryProperties.class,
        QdrantProperties.class
})
public class MiniShopApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(
                TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        );

        SpringApplication.run(MiniShopApplication.class, args);
    }

}
