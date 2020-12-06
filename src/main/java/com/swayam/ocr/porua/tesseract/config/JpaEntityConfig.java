package com.swayam.ocr.porua.tesseract.config;

import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.swayam.ocr.porua.tesseract.model.Book;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.service.EntityClassUtil;
import com.swayam.ocr.porua.tesseract.service.EntityClassUtil.EntityClassDetails;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

@Configuration
public class JpaEntityConfig {

    public static final String ENTITY_PACKAGE = Book.class.getPackageName();

    private static final String OCR_WORD_TABLE_SUFFIX = "_ocr_word";

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(JpaVendorAdapter jpaVendorAdapter, DataSource dataSource) throws SQLException {
	createEntities(dataSource);
	LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
	factory.setJpaVendorAdapter(jpaVendorAdapter);
	factory.setPackagesToScan(ENTITY_PACKAGE);
	// factory.setMappingResources(mappingResources);
	factory.setDataSource(dataSource);
	return factory;
    }

    private void createEntities(DataSource dataSource) throws SQLException {
	PreparedStatement pStat = dataSource.getConnection().prepareStatement("SELECT base_table_name FROM book");
	ResultSet res = pStat.executeQuery();
	while (res.next()) {
	    String baseTableName = res.getString("base_table_name");
	    EntityClassDetails entityClassDetails = new EntityClassUtil().getEntityClassDetails(baseTableName);
	    Class<?> ocrWordEntity = createOcrWordEntity(baseTableName, entityClassDetails.getOcrWordEntity());
	    createOcrWordRepository(entityClassDetails.getOcrWordEntityRepository(), ocrWordEntity);
	}
    }

    private Class<?> createOcrWordEntity(String baseTableName, String entityClassName) {

	Class<?> ocrWordEntityClass = new ByteBuddy().subclass(OcrWordEntityTemplate.class).annotateType(new Entity() {

	    @Override
	    public Class<? extends Annotation> annotationType() {
		return Entity.class;
	    }

	    @Override
	    public String name() {
		return "";
	    }
	}, new Table() {

	    @Override
	    public Class<? extends Annotation> annotationType() {
		return Table.class;
	    }

	    @Override
	    public UniqueConstraint[] uniqueConstraints() {
		return new UniqueConstraint[0];
	    }

	    @Override
	    public String schema() {
		return "";
	    }

	    @Override
	    public String name() {
		return baseTableName + OCR_WORD_TABLE_SUFFIX;
	    }

	    @Override
	    public Index[] indexes() {
		return new Index[0];
	    }

	    @Override
	    public String catalog() {
		return "";
	    }
	}).name(entityClassName).make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();

	return ocrWordEntityClass;

    }

    private void createOcrWordRepository(String repositoryClassName, Class<?> entityClass) {
	Generic crudRepo = Generic.Builder.parameterizedType(CrudRepository.class, entityClass, Long.class).build();
	Class<?> ocrWordEntityClass = new ByteBuddy().makeInterface(crudRepo).implement(OcrWordRepositoryTemplate.class).name(repositoryClassName).make()
		.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();

    }

}
