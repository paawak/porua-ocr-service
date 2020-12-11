package com.swayam.ocr.porua.tesseract.config;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.swayam.ocr.porua.tesseract.model.Book;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.service.EntityClassUtil;
import com.swayam.ocr.porua.tesseract.service.EntityClassUtil.EntityClassDetails;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

public class JpaEntityConfig implements EnvironmentPostProcessor {

    public static final String ENTITY_PACKAGE = Book.class.getPackageName();

    private static final String OCR_WORD_TABLE_SUFFIX = "_ocr_word";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
	try {
	    createEntities();
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    private void createEntities() throws SQLException {
	Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/exp_to_b_del?useSSL=false", "root", "root123");
	PreparedStatement pStat = con.prepareStatement("SELECT base_table_name FROM book");
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
	Class<?> ocrWordEntityClass = new ByteBuddy().makeInterface(crudRepo).implement(OcrWordRepositoryTemplate.class).annotateType(new Repository() {

	    @Override
	    public Class<? extends Annotation> annotationType() {
		return Repository.class;
	    }

	    @Override
	    public String value() {
		return "";
	    }
	}).name(repositoryClassName).make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();

	System.err.println("***********" + ocrWordEntityClass);
    }

}
