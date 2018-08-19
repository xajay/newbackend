package com.mde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * @author xajay testing in conference room
 *
 */
@SpringBootApplication
public class Config extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Config.class, args);
	}

/*	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Config.class);
	}*/
}
