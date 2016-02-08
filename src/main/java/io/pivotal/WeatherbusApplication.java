package io.pivotal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
public class WeatherbusApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherbusApplication.class, args);
	}
}
