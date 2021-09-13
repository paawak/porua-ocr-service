package com.swayam.ocr.porua.tesseract.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.service.EntityClassUtil;
import com.swayam.ocr.porua.tesseract.service.EntityClassUtil.EntityClassDetails;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Loaded;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatchers;

@Order(Ordered.LOWEST_PRECEDENCE)
@Configuration
public class DynamicJpaRepositoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicJpaRepositoryPostProcessor.class);

    private static final String OCR_WORD_TABLE_SUFFIX = "_ocr_word";

    private static final String CORRECTED_WORD_TABLE_SUFFIX = "_corrected_word";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;

	Function<String, Class<?>> createClass = className -> {
	    try {
		return Class.forName(className);
	    } catch (ClassNotFoundException e) {
		throw new RuntimeException(e);
	    }
	};

	List<EntityClassDetails> dynamicJpaRepoClasses;
	try {
	    dynamicJpaRepoClasses = createEntitiesAndRepos(beanFactory.getBean(ConfigurableEnvironment.class));
	} catch (SQLException | IOException e) {
	    throw new RuntimeException(e);
	}

	dynamicJpaRepoClasses.stream().forEach(entityClassDetails -> {
	    registerJpaRepositoryFactoryBean(createClass.apply(entityClassDetails.getOcrWordEntityRepository()), defaultListableBeanFactory);
	    registerJpaRepositoryFactoryBean(createClass.apply(entityClassDetails.getCorrectedWordEntityRepository()), defaultListableBeanFactory);
	});

    }

    private void registerJpaRepositoryFactoryBean(Class<?> jpaRepositoryClass, DefaultListableBeanFactory defaultListableBeanFactory) {
	String beanName = jpaRepositoryClass.getName();
	if (defaultListableBeanFactory.containsBean(beanName)) {
	    LOG.warn("A bean with name {} already exists, skipping creating JPAFactoryBean dynamically", beanName);
	    return;
	}
	BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(JpaRepositoryFactoryBean.class).addConstructorArgValue(jpaRepositoryClass);
	defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

    private List<EntityClassDetails> createEntitiesAndRepos(ConfigurableEnvironment environment) throws SQLException, IOException {
	String dbUrl = environment.getProperty("spring.datasource.url");
	String dbUser = environment.getProperty("spring.datasource.username");
	String dbPassword = environment.getProperty("spring.datasource.password");
	Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
	PreparedStatement pStat;
	try {
	    pStat = con.prepareStatement("SELECT base_table_name FROM book");
	} catch (SQLSyntaxErrorException e) {
	    throw new RuntimeException(e);
	}
	ResultSet res = pStat.executeQuery();
	List<EntityClassDetails> entityDetails = new ArrayList<>();
	while (res.next()) {
	    String baseTableName = res.getString("base_table_name");
	    if (!StringUtils.hasText(baseTableName)) {
		LOG.error("Dynamic JPA Repo cannot be created as the *base_table_name* is empty");
		continue;
	    }
	    EntityClassDetails entityClassDetails = new EntityClassUtil().getEntityClassDetails(baseTableName);
	    createCorrectedWordEntity(baseTableName, entityClassDetails.getCorrectedWordEntity(), environment);
	    try {
		createOcrWordEntity(baseTableName, entityClassDetails.getOcrWordEntity(), entityClassDetails.getCorrectedWordEntity(), environment);
	    } catch (ClassNotFoundException | IOException e) {
		throw new RuntimeException(e);
	    }
	    try {
		createOcrWordRepository(entityClassDetails.getOcrWordEntityRepository(), entityClassDetails.getOcrWordEntity(), environment);
	    } catch (ClassNotFoundException | IOException e) {
		throw new RuntimeException(e);
	    }

	    try {
		createCorrectedWordRepository(entityClassDetails.getCorrectedWordEntityRepository(), entityClassDetails.getCorrectedWordEntity(), environment);
	    } catch (ClassNotFoundException | IOException e) {
		throw new RuntimeException(e);
	    }

	    entityDetails.add(entityClassDetails);

	}

	return entityDetails;
    }

    /**
     * Creates an instance of {@link CorrectedWordEntityTemplate} dynamically.
     * The generated class looks like
     * {@link DummyAuthorDummyBookCorrectedWordEntity} in the <em>test</em>
     * folder.
     */
    private void createCorrectedWordEntity(String baseTableName, String entityClassName, ConfigurableEnvironment environment) throws IOException {

	if (classFileExists(entityClassName)) {
	    LOG.info("The class " + entityClassName + " already exists, not creating a new one");
	    return;
	}

	LOG.info("Creating new class: " + entityClassName);

	Unloaded<?> generatedClass = new ByteBuddy().subclass(CorrectedWordEntityTemplate.class).annotateType(getEntityAnnotation(), getTableAnnotation(baseTableName + CORRECTED_WORD_TABLE_SUFFIX))
		.name(entityClassName).make();

	saveGeneratedClassAsFile(generatedClass, environment);

    }

    /**
     * Creates an instance of {@link OcrWordEntityTemplate} dynamically. The
     * generated class looks like {@link DummyAuthorDummyBookOcrWordEntity} in
     * the <em>test</em> folder.
     */
    private void createOcrWordEntity(String baseTableName, String entityClassName, String correctedWordEntity, ConfigurableEnvironment environment) throws IOException, ClassNotFoundException {

	if (classFileExists(entityClassName)) {
	    LOG.info("The class " + entityClassName + " already exists, not creating a new one");
	    return;
	}

	LOG.info("Creating new class: " + entityClassName);

	Class<?> correctedEntityClass = Class.forName(correctedWordEntity);

	Unloaded<?> generatedClass = new ByteBuddy().subclass(OcrWordEntityTemplate.class).annotateType(getEntityAnnotation(), getTableAnnotation(baseTableName + OCR_WORD_TABLE_SUFFIX))
		.defineField("correctedWords", TypeDescription.Generic.Builder.parameterizedType(List.class, correctedEntityClass).build(), Modifier.PRIVATE)
		.annotateField(AnnotationDescription.Builder.ofType(OneToMany.class).define("fetch", FetchType.LAZY).define("mappedBy", "ocrWordId").build())
		.annotateField(AnnotationDescription.Builder.ofType(JsonIgnore.class).build())
		.defineMethod("getCorrectedWords", TypeDescription.Generic.Builder.parameterizedType(List.class, correctedEntityClass).build(), Modifier.PUBLIC)
		.intercept(FieldAccessor.ofBeanProperty()).name(entityClassName).make();

	saveGeneratedClassAsFile(generatedClass, environment);

    }

    private AnnotationDescription getEntityAnnotation() {
	return AnnotationDescription.Builder.ofType(Entity.class).build();
    }

    private AnnotationDescription getTableAnnotation(String tableName) {
	return AnnotationDescription.Builder.ofType(Table.class).define("name", tableName).build();
    }

    /**
     * Creates a child interface of {@link OcrWordRepositoryTemplate}
     * dynamically. The generated class looks like
     * {@link DummyAuthorDummyBookOcrWordRepository} in the <em>test</em>
     * folder.
     */
    private void createOcrWordRepository(String repositoryClassName, String entityClassName, ConfigurableEnvironment environment) throws IOException, ClassNotFoundException {
	if (classFileExists(repositoryClassName)) {
	    return;
	}
	Generic crudRepo = Generic.Builder.parameterizedType(CrudRepository.class, Class.forName(entityClassName), Long.class).build();

	Unloaded<?> generatedClass =
		new ByteBuddy().makeInterface(crudRepo).implement(OcrWordRepositoryTemplate.class).annotateType(getRepositoryAnnotation(repositoryClassName)).name(repositoryClassName).make();

	saveGeneratedClassAsFile(generatedClass, environment);

    }

    /**
     * Creates a child interface of {@link CorrectedWordRepositoryTemplate}
     * dynamically. The generated class looks like
     * {@link DummyAuthorDummyBookCorrectedWordRepository} in the <em>test</em>
     * folder.
     */
    private void createCorrectedWordRepository(String repositoryClassName, String entityClassName, ConfigurableEnvironment environment) throws IOException, ClassNotFoundException {
	if (classFileExists(repositoryClassName)) {
	    return;
	}
	Generic crudRepo = Generic.Builder.parameterizedType(CrudRepository.class, Class.forName(entityClassName), Long.class).build();

	Unloaded<?> generatedClass = new ByteBuddy().makeInterface(crudRepo).implement(CorrectedWordRepositoryTemplate.class).annotateType(getRepositoryAnnotation(repositoryClassName))
		.method(ElementMatchers.named("updateCorrectedText")).withoutCode().annotateMethod(AnnotationDescription.Builder.ofType(Modifying.class).build())
		.annotateMethod(AnnotationDescription.Builder.ofType(Query.class)
			.define("value", "update " + entityClassName + " set correctedText = :correctedText where ocrWordId = :ocrWordId and user = :user").build())
		.method(ElementMatchers.named("markAsIgnored")).withoutCode().annotateMethod(AnnotationDescription.Builder.ofType(Modifying.class).build())
		.annotateMethod(
			AnnotationDescription.Builder.ofType(Query.class).define("value", "update " + entityClassName + " set ignored = TRUE where ocrWordId = :ocrWordId and user = :user").build())
		.name(repositoryClassName).make();

	saveGeneratedClassAsFile(generatedClass, environment);

    }

    private AnnotationDescription getRepositoryAnnotation(String repositoryClassName) {
	return AnnotationDescription.Builder.ofType(org.springframework.stereotype.Repository.class).define("value", repositoryClassName).build();
    }

    private boolean classFileExists(String className) {
	try {
	    Class.forName(className);
	    return true;
	} catch (ClassNotFoundException e) {
	    return false;
	}
    }

    private void saveGeneratedClassAsFile(Unloaded<?> unloadedClass, ConfigurableEnvironment environment) throws IOException {

	Loaded<?> loadedClass = unloadedClass.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION);

	File baseLocation = new File(environment.getProperty("app.config.dynamic-jpa-write-directory"));

	loadedClass.saveIn(baseLocation);

    }

}
