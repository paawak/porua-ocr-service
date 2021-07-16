package com.swayam.ocr.porua.tesseract.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.stereotype.Service;

import com.swayam.ocr.porua.tesseract.model.CorrectedWord;
import com.swayam.ocr.porua.tesseract.model.CorrectedWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.UserRole;
import com.swayam.ocr.porua.tesseract.repo.BookRepository;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.repo.OcrWordRepositoryTemplate;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;
import com.swayam.ocr.porua.tesseract.service.EntityClassUtil.EntityClassDetails;

@Service
public class OcrWordServiceImpl implements OcrWordService {

    private final BookRepository bookRepository;
    private final ApplicationContext applicationContext;

    public OcrWordServiceImpl(BookRepository bookRepository, ApplicationContext applicationContext) {
	this.bookRepository = bookRepository;
	this.applicationContext = applicationContext;
    }

    @Override
    public int getWordCount(long bookId, long pageImageId) {
	return getOcrWordRepositoryTemplate(bookId).countByOcrWordIdBookIdAndOcrWordIdPageImageId(bookId, pageImageId);
    }

    @Override
    public Collection<OcrWordOutputDto> getWords(long bookId, long pageImageId, UserDetails userDetails) {
	Collection<OcrWord> entities = getOcrWordRepositoryTemplate(bookId).findByOcrWordIdBookIdAndOcrWordIdPageImageIdOrderByOcrWordIdWordSequenceId(bookId, pageImageId);
	return entities.stream().map(entity -> {
	    OcrWordOutputDto dto = new OcrWordOutputDto();
	    BeanUtils.copyProperties(entity, dto);
	    List<? extends CorrectedWord> correctedWords = entity.getCorrectedWords();
	    if (correctedWords.size() > 0) {

		boolean isIgnored =
			correctedWords.stream().filter(correctedWord -> (correctedWord.getUser().getRole() == UserRole.ADMIN_ROLE) || (correctedWord.getUser().getId() == userDetails.getId()))
				.anyMatch(CorrectedWord::isIgnored);

		if (isIgnored) {
		    dto.setIgnored(true);
		} else {
		    Optional<? extends CorrectedWord> correctedWordWithText = correctedWords.stream().filter(correctedWord -> correctedWord.getCorrectedText() != null).findFirst();
		    if (correctedWordWithText.isPresent()) {
			dto.setCorrectedText(correctedWordWithText.get().getCorrectedText());
		    }
		}

	    }
	    return dto;
	}).filter(correctedWord -> !correctedWord.isIgnored()).collect(Collectors.toList());
    }

    @Override
    public OcrWord getWord(OcrWordId ocrWordId) {
	return getOcrWordRepositoryTemplate(ocrWordId.getBookId()).findByOcrWordId(ocrWordId).get();
    }

    @Transactional
    @Override
    public int markWordAsIgnored(OcrWordId ocrWordId, UserDetails user) {
	OcrWord ocrWord = getWord(ocrWordId);

	CorrectedWordRepositoryTemplate correctedWordRepositoryTemplate = getCorrectedWordRepositoryTemplate(ocrWordId.getBookId());
	Optional<CorrectedWord> existingCorrection = correctedWordRepositoryTemplate.findByOcrWordIdAndUser(ocrWord.getId(), user);

	if (existingCorrection.isPresent()) {
	    return correctedWordRepositoryTemplate.markAsIgnored(ocrWord.getId(), user);
	}

	correctedWordRepositoryTemplate.save(toEntity(Optional.empty(), ocrWord, user));

	return 1;
    }

    @Override
    public OcrWord addOcrWord(OcrWord ocrWord) {
	return getOcrWordRepositoryTemplate(ocrWord.getOcrWordId().getBookId()).save(toEntity(ocrWord));
    }

    @Transactional
    @Override
    public int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user) {

	OcrWord ocrWord = getWord(ocrWordId);

	CorrectedWordRepositoryTemplate correctedWordRepositoryTemplate = getCorrectedWordRepositoryTemplate(ocrWordId.getBookId());
	Optional<CorrectedWord> existingCorrection = correctedWordRepositoryTemplate.findByOcrWordIdAndUser(ocrWord.getId(), user);

	if (existingCorrection.isPresent()) {
	    return correctedWordRepositoryTemplate.updateCorrectedText(ocrWord.getId(), correctedText, user);
	}

	correctedWordRepositoryTemplate.save(toEntity(Optional.of(correctedText), ocrWord, user));

	return 1;
    }

    private OcrWordRepositoryTemplate getOcrWordRepositoryTemplate(long bookId) {
	EntityClassDetails entityClassDetails = getEntityClassDetails(bookId);

	JpaRepositoryFactoryBean bean = getJpaFactoryBean(entityClassDetails.getOcrWordEntityRepository());

	return (OcrWordRepositoryTemplate) bean.getObject();
    }

    private CorrectedWordRepositoryTemplate getCorrectedWordRepositoryTemplate(long bookId) {
	EntityClassDetails entityClassDetails = getEntityClassDetails(bookId);

	JpaRepositoryFactoryBean bean = getJpaFactoryBean(entityClassDetails.getCorrectedWordEntityRepository());

	return (CorrectedWordRepositoryTemplate) bean.getObject();
    }

    private JpaRepositoryFactoryBean getJpaFactoryBean(String className) {
	JpaRepositoryFactoryBean bean;
	try {
	    bean = new JpaRepositoryFactoryBean(Class.forName(className));
	    EntityManager em = applicationContext.getBean(EntityManager.class);
	    // EntityMananger should be injected
	    bean.setEntityManager(em);
	    ObjectProvider<EntityPathResolver> op = new ObjectProvider<EntityPathResolver>() {

		@Override
		public EntityPathResolver getObject() throws BeansException {
		    return SimpleEntityPathResolver.INSTANCE;
		}

		@Override
		public EntityPathResolver getObject(Object... args) throws BeansException {
		    return getObject();
		}

		@Override
		public EntityPathResolver getIfAvailable() throws BeansException {
		    return getObject();
		}

		@Override
		public EntityPathResolver getIfUnique() throws BeansException {
		    return getObject();
		}
	    };
	    bean.setEntityPathResolver(op);
	    bean.afterPropertiesSet();
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
	return bean;
    }

    private EntityClassDetails getEntityClassDetails(long bookId) {
	String baseTableName = bookRepository.findById(bookId).get().getBaseTableName();
	return new EntityClassUtil().getEntityClassDetails(baseTableName);
    }

    private OcrWord toEntity(OcrWord ocrWord) {
	EntityClassDetails entityClassDetails = getEntityClassDetails(ocrWord.getOcrWordId().getBookId());
	OcrWord entity;
	try {
	    entity = (OcrWord) Class.forName(entityClassDetails.getOcrWordEntity()).getDeclaredConstructor().newInstance();
	} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
	    throw new RuntimeException(e);
	}
	BeanUtils.copyProperties(ocrWord, entity);
	return entity;
    }

    private CorrectedWord toEntity(Optional<String> correctedText, OcrWord ocrWord, UserDetails user) {
	EntityClassDetails entityClassDetails = getEntityClassDetails(ocrWord.getOcrWordId().getBookId());
	CorrectedWordEntityTemplate correctedWord = new CorrectedWordEntityTemplate();
	if (correctedText.isPresent()) {
	    correctedWord.setCorrectedText(correctedText.get());
	    correctedWord.setIgnored(false);
	} else {
	    correctedWord.setIgnored(true);
	}
	correctedWord.setOcrWordId(ocrWord.getId());
	correctedWord.setUser(user);
	CorrectedWord entity;
	try {
	    entity = (CorrectedWord) Class.forName(entityClassDetails.getCorrectedWordEntity()).getDeclaredConstructor().newInstance();
	} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
	    throw new RuntimeException(e);
	}
	BeanUtils.copyProperties(correctedWord, entity);
	return entity;
    }

}
