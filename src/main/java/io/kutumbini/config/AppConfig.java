package io.kutumbini.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import io.kutumbini.web.filter.AppFilter;

@Configuration
@ComponentScan(basePackages = {"io.kutumbini"})
@EnableNeo4jRepositories("io.kutumbini")
public class AppConfig {
	
	@Bean
	public FilterRegistrationBean<AppFilter> appFilter() {
	    FilterRegistrationBean<AppFilter> registrationBean 
	      = new FilterRegistrationBean<>();
	         
	    registrationBean.setFilter(new AppFilter());
	    registrationBean.addUrlPatterns("/*");
	    registrationBean.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
	         
	    return registrationBean;    
	}
}
