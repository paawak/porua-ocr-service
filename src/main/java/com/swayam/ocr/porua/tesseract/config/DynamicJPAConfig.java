package com.swayam.ocr.porua.tesseract.config;

import javax.persistence.EntityManager;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

@Configuration
public class DynamicJPAConfig {

    public void foo(EntityManager entityManager) {
	JpaRepositoryFactory jpaRepositoryFactory = new JpaRepositoryFactory(entityManager);
	// jpaRepositoryFactory.
	JpaRepositoryFactoryBean bean;
	// bean.
    }

}
