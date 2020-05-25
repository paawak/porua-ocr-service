package com.swayam.ocr.porua.tesseract.rest;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
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

import com.swayam.ocr.porua.tesseract.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.Book;
import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.PageImage;
import com.swayam.ocr.porua.tesseract.service.FileSystemUtil;
import com.swayam.ocr.porua.tesseract.service.OcrDataStoreService;
import com.swayam.ocr.porua.tesseract.service.TesseractOcrWordAnalyser;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/train")
public class OCRTrainingController {

    private static final Logger LOG = LoggerFactory.getLogger(OCRTrainingController.class);

    private final OcrDataStoreService ocrDataStoreService;
    private final FileSystemUtil fileSystemUtil;

    public OCRTrainingController(OcrDataStoreService ocrDataStoreService, FileSystemUtil fileSystemUtil) {
	this.ocrDataStoreService = ocrDataStoreService;
	this.fileSystemUtil = fileSystemUtil;

    }

    @GetMapping(value = "/book", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Book> getBooks() {
	return Flux.fromIterable(ocrDataStoreService.getBooks());
    }

    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PageImage> getPages(@RequestParam("bookId") final long bookId) {
	return Flux.fromIterable(ocrDataStoreService.getPages(bookId));
    }

    @GetMapping(value = "/word", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<OcrWord> getOcrWords(@RequestParam("bookId") final long bookId, @RequestParam("pageImageId") final long pageImageId) {
	LOG.info("Retrieving OCR Words for Book Id {} and PageId {}", bookId, pageImageId);
	return Flux.fromIterable(ocrDataStoreService.getWords(bookId, pageImageId));
    }

    @PostMapping(value = "/word", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<OcrWord> submitPageAndAnalyzeWords(@RequestPart("bookId") final long bookId, @RequestPart("pageNumber") final int pageNumber, @RequestPart("image") final FilePart image)
	    throws IOException, URISyntaxException {

	LOG.info("Uploaded fileName: {}", image.filename());

	Book book = ocrDataStoreService.getBook(bookId);

	String imageFileName = image.filename();
	Path savedImagePath = fileSystemUtil.saveMultipartFileAsImage(image);

	PageImage newPageImage = new PageImage();
	newPageImage.setName(imageFileName);
	newPageImage.setPageNumber(pageNumber);
	long imageFileId = ocrDataStoreService.addPageImage(newPageImage).getId();

	return Flux.create((FluxSink<OcrWord> fluxSink) -> {
	    new TesseractOcrWordAnalyser(savedImagePath, book.getLanguage()).extractWordsFromImage(fluxSink, (wordSequenceId) -> new OcrWordId(bookId, imageFileId, wordSequenceId));
	}).map(rawText -> ocrDataStoreService.addOcrWord(rawText));

    }

    @GetMapping(value = "/word/image")
    public Mono<ResponseEntity<byte[]>> getOcrWordImage(@RequestParam("bookId") final long bookId, @RequestParam("pageImageId") final long rawImageId,
	    @RequestParam("wordSequenceId") int wordSequenceId, @RequestParam("imagePath") Path imagePath) throws IOException {
	LOG.trace("serving ocr word image for id: {}", wordSequenceId);
	OcrWord ocrText = ocrDataStoreService.getWord(new OcrWordId(bookId, rawImageId, wordSequenceId));
	BufferedImage fullImage = ImageIO.read(Files.newInputStream(imagePath));
	Rectangle wordArea = TesseractOcrWordAnalyser.getWordArea(ocrText);
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
