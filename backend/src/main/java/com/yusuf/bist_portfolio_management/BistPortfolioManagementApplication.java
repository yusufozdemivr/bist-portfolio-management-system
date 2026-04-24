package com.yusuf.bist_portfolio_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class BistPortfolioManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(BistPortfolioManagementApplication.class, args);
	}

}
