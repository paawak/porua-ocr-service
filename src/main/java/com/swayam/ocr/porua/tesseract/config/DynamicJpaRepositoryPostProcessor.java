package com.swayam.ocr.porua.tesseract.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.annotations.VisibleForTesting;
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
public class DynamicJpaRepositoryPostProcessor implements EnvironmentPostProcessor {

    private static final String OCR_WORD_TABLE_SUFFIX = "_ocr_word";

    private static final String CORRECTED_WORD_TABLE_SUFFIX = "_corrected_word";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
	System.out.println("Start creating dynamic JPA Repos...");
	try {
	    createEntitiesAndRepos(environment.getProperty("spring.datasource.url"), environment.getProperty("spring.datasource.username"), environment.getProperty("spring.datasource.password"));
	} catch (SQLException | IOException e) {
	    throw new RuntimeException(e);
	}
    }

    @VisibleForTesting
    Optional<URI> getJarFilePath(URL url) {
	if (url.getProtocol().equals("file")) {
	    return Optional.empty();
	}

	if (!url.getProtocol().equals("jar")) {
	    throw new IllegalArgumentException("Unsupported protocol: " + url.getProtocol());
	}

	return Optional.of(URI.create(url.toString().split(".jar!")[0] + ".jar"));
    }

    private void createEntitiesAndRepos(String dbUrl, String dbUser, String dbPassword) throws SQLException, IOException {
	Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
	PreparedStatement pStat;
	try {
	    pStat = con.prepareStatement("SELECT base_table_name FROM book");
	} catch (SQLSyntaxErrorException e) {
	    System.err.println("Could not create dynamic JPA Repos: " + e.getMessage());
	    e.printStackTrace();
	    return;
	}
	ResultSet res = pStat.executeQuery();
	while (res.next()) {
	    String baseTableName = res.getString("base_table_name");
	    if (!StringUtils.hasText(baseTableName)) {
		System.err.println("Dynamic JPA Repo cannot be created as the *base_table_name* is empty");
		continue;
	    }
	    EntityClassDetails entityClassDetails = new EntityClassUtil().getEntityClassDetails(baseTableName);
	    createCorrectedWordEntity(baseTableName, entityClassDetails.getCorrectedWordEntity());
	    try {
		createOcrWordEntity(baseTableName, entityClassDetails.getOcrWordEntity(), entityClassDetails.getCorrectedWordEntity());
	    } catch (ClassNotFoundException | IOException e) {
		throw new RuntimeException(e);
	    }
	    try {
		createOcrWordRepository(entityClassDetails.getOcrWordEntityRepository(), entityClassDetails.getOcrWordEntity());
	    } catch (ClassNotFoundException | IOException e) {
		throw new RuntimeException(e);
	    }

	    try {
		createCorrectedWordRepository(entityClassDetails.getCorrectedWordEntityRepository(), entityClassDetails.getCorrectedWordEntity());
	    } catch (ClassNotFoundException | IOException e) {
		throw new RuntimeException(e);
	    }

	}
    }

    /**
     * Creates an instance of {@link CorrectedWordEntityTemplate} dynamically.
     * The generated class looks like
     * {@link DummyAuthorDummyBookCorrectedWordEntity} in the <em>test</em>
     * folder.
     */
    private void createCorrectedWordEntity(String baseTableName, String entityClassName) throws IOException {

	if (classFileExists(entityClassName)) {
	    System.out.println("The class " + entityClassName + " already exists, not creating a new one");
	    return;
	}

	System.out.println("Creating new class: " + entityClassName);

	Unloaded<?> generatedClass = new ByteBuddy().subclass(CorrectedWordEntityTemplate.class).annotateType(getEntityAnnotation(), getTableAnnotation(baseTableName + CORRECTED_WORD_TABLE_SUFFIX))
		.name(entityClassName).make();

	saveGeneratedClassAsFile(generatedClass);

    }

    /**
     * Creates an instance of {@link OcrWordEntityTemplate} dynamically. The
     * generated class looks like {@link DummyAuthorDummyBookOcrWordEntity} in
     * the <em>test</em> folder.
     */
    private void createOcrWordEntity(String baseTableName, String entityClassName, String correctedWordEntity) throws IOException, ClassNotFoundException {

	if (classFileExists(entityClassName)) {
	    System.out.println("The class " + entityClassName + " already exists, not creating a new one");
	    return;
	}

	System.out.println("Creating new class: " + entityClassName);

	Class<?> correctedEntityClass = Class.forName(correctedWordEntity);

	Unloaded<?> generatedClass = new ByteBuddy().subclass(OcrWordEntityTemplate.class).annotateType(getEntityAnnotation(), getTableAnnotation(baseTableName + OCR_WORD_TABLE_SUFFIX))
		.defineField("correctedWords", TypeDescription.Generic.Builder.parameterizedType(List.class, correctedEntityClass).build(), Modifier.PRIVATE)
		.annotateField(AnnotationDescription.Builder.ofType(OneToMany.class).define("fetch", FetchType.LAZY).define("mappedBy", "ocrWordId").build())
		.annotateField(AnnotationDescription.Builder.ofType(JsonIgnore.class).build())
		.defineMethod("getCorrectedWords", TypeDescription.Generic.Builder.parameterizedType(List.class, correctedEntityClass).build(), Modifier.PUBLIC)
		.intercept(FieldAccessor.ofBeanProperty()).name(entityClassName).make();

	saveGeneratedClassAsFile(generatedClass);

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
    private void createOcrWordRepository(String repositoryClassName, String entityClassName) throws IOException, ClassNotFoundException {
	if (classFileExists(repositoryClassName)) {
	    return;
	}
	Generic crudRepo = Generic.Builder.parameterizedType(CrudRepository.class, Class.forName(entityClassName), Long.class).build();

	Unloaded<?> generatedClass =
		new ByteBuddy().makeInterface(crudRepo).implement(OcrWordRepositoryTemplate.class).annotateType(getRepositoryAnnotation(repositoryClassName)).name(repositoryClassName).make();

	saveGeneratedClassAsFile(generatedClass);

    }

    /**
     * Creates a child interface of {@link CorrectedWordRepositoryTemplate}
     * dynamically. The generated class looks like
     * {@link DummyAuthorDummyBookCorrectedWordRepository} in the <em>test</em>
     * folder.
     */
    private void createCorrectedWordRepository(String repositoryClassName, String entityClassName) throws IOException, ClassNotFoundException {
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

	saveGeneratedClassAsFile(generatedClass);

    }

    private AnnotationDescription getRepositoryAnnotation(String repositoryClassName) {
	return AnnotationDescription.Builder.ofType(Repository.class).define("value", repositoryClassName).build();
    }

    private boolean classFileExists(String className) {
	try {
	    Class.forName(className);
	    return true;
	} catch (ClassNotFoundException e) {
	    return false;
	}
    }

    private void saveGeneratedClassAsFile(Unloaded<?> unloadedClass) throws IOException {

	Loaded<?> loadedClass = unloadedClass.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION);

	File baseLocation = new File("/dynamic-jpa-classes");

	loadedClass.saveIn(baseLocation);

    }

}
