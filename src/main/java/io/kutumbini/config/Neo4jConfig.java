package io.kutumbini.config;

import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.ConfigurationSource;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@Configuration
//@EnableNeo4jRepositories(basePackages = "io.kutumbini")
//@EnableTransactionManagement
public class Neo4jConfig {
	
	@Autowired
    Environment environment;
	
	@Bean
    public SessionFactory sessionFactory() {
        // with domain entity base package(s)
        return new SessionFactory(configuration(), "io.kutumbini");
    }

    @Bean
    public org.neo4j.ogm.config.Configuration configuration() {
    	String profile = environment.getActiveProfiles()[0];
        ConfigurationSource properties = new ClasspathConfigurationSource("application-" + profile + ".properties");
        org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration.Builder(properties).build();
        return configuration;
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory());
    }
}
