package com.swayam.ocr.porua.tesseract.rest;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.swayam.ocr.porua.tesseract.model.CachedOcrText;
import com.swayam.ocr.porua.tesseract.model.Language;
import com.swayam.ocr.porua.tesseract.model.RawOcrWord;
import com.swayam.ocr.porua.tesseract.service.FileSystemUtil;
import com.swayam.ocr.porua.tesseract.service.TesseractOcrWordAnalyser;
import com.swayam.ocr.porua.tesseract.service.WordCache;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/train")
public class OCRTrainingController {

    private static final Logger LOG = LoggerFactory.getLogger(OCRTrainingController.class);

    private final WordCache wordCache;
    private final FileSystemUtil fileSystemUtil;

    public OCRTrainingController(WordCache wordCache, FileSystemUtil fileSystemUtil) {
	this.wordCache = wordCache;
	this.fileSystemUtil = fileSystemUtil;

    }

    @PostMapping(value = "/word", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<RawOcrWord> getDetectedText(@RequestPart("language") String languageAsString, @RequestPart("image") FilePart image) {
	Language language = Language.valueOf(languageAsString);

	String imageFileName = image.filename();
	Path savedImagePath = fileSystemUtil.saveMultipartFileAsImage(image);

	if (wordCache.getWordCount(imageFileName) == 0) {
	    int imageFileId = wordCache.storeImageFile(imageFileName, Language.ben);
	    return Flux.create((FluxSink<RawOcrWord> fluxSink) -> {
		new TesseractOcrWordAnalyser(savedImagePath, language).extractWordsFromImage(fluxSink);
	    }).doOnNext(rawText -> wordCache.storeRawOcrWord(imageFileId, rawText));
	}

	LOG.warn("Entries already present for {}: using existing entries", imageFileName);

	return Flux.fromIterable(wordCache.getWords(imageFileName)).map(CachedOcrText::getRawOcrText);

    }

    @GetMapping(value = "/word/image")
    public Mono<ResponseEntity<byte[]>> getOcrWordImage(@RequestParam("wordId") int wordId, @RequestParam("imagePath") Path imagePath) throws IOException {
	LOG.info("serving ocr word image for id: {}", wordId);
	CachedOcrText ocrText = wordCache.getWord(wordId);
	BufferedImage fullImage = ImageIO.read(Files.newInputStream(imagePath));
	Rectangle wordArea = TesseractOcrWordAnalyser.getWordArea(ocrText.getRawOcrText());
	BufferedImage wordImage = fullImage.getSubimage(wordArea.x, wordArea.y, wordArea.width, wordArea.height);
	String extension = imagePath.toFile().getName().split("\\.")[1];
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	ImageIO.write(wordImage, extension, bos);
	bos.flush();
	HttpHeaders responseHeaders = new HttpHeaders();
	responseHeaders.set("content-type", "image/" + extension);
	return Mono.just(new ResponseEntity<byte[]>(bos.toByteArray(), responseHeaders, HttpStatus.OK));
    }

}
