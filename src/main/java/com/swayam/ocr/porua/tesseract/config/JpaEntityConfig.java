package com.swayam.ocr.porua.tesseract.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.swayam.ocr.porua.tesseract.model.Book;

@Configuration
public class JpaEntityConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(JpaVendorAdapter jpaVendorAdapter, DataSource dataSource) {
	LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
	factory.setJpaVendorAdapter(jpaVendorAdapter);
	factory.setPackagesToScan(Book.class.getPackageName());
	// factory.setMappingResources(mappingResources);
	factory.setDataSource(dataSource);
	return factory;
    }

}
