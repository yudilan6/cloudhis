package com.dyhospital.cloudhis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
/**
 * @author yangzongxue
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(com.dyhospital.cloudhis.GatewayApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(com.dyhospital.cloudhis.GatewayApplication.class, args);
	}


}
