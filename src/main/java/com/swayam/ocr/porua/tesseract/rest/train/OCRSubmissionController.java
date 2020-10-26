package com.swayam.ocr.porua.tesseract.rest.train;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.service.FileSystemUtil;
import com.swayam.ocr.porua.tesseract.service.ImageProcessor;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ocr/train/submit")
public class OCRSubmissionController {

    private static final Logger LOG = LoggerFactory.getLogger(OCRSubmissionController.class);

    private final ImageProcessor imageProcessor;
    private final FileSystemUtil fileSystemUtil;

    public OCRSubmissionController(ImageProcessor imageProcessor, FileSystemUtil fileSystemUtil) {
	this.imageProcessor = imageProcessor;
	this.fileSystemUtil = fileSystemUtil;
    }

    @PostMapping(value = "/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<OcrWord> submitPageAndAnalyzeWords(@RequestPart("bookId") final String bookIdString, @RequestPart("pageNumber") final String pageNumberString,
	    @RequestPart("image") final FilePart image) throws IOException, URISyntaxException {

	LOG.info("BookId: {}, PageNumber: {}, Uploaded fileName: {}", bookIdString, pageNumberString, image.filename());

	String imageFileName = image.filename();
	Path savedImagePath = fileSystemUtil.saveMultipartFileAsImage(image);

	return imageProcessor.submitPageForAnalysis(Long.valueOf(bookIdString), Integer.parseInt(pageNumberString), imageFileName, savedImagePath);

    }

    @PostMapping(value = "/pdf", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadEBookInPdfFormat(@RequestPart("bookId") final String bookIdAsString, @RequestPart("image") FilePart eBookAsPdf) throws IOException {

	String eBookName = eBookAsPdf.filename();
	MediaType mediaType = eBookAsPdf.headers().getContentType();

	LOG.info("bookId: {}", bookIdAsString);
	LOG.info("FileName: {}, ContentType: {}, Size: {}", eBookName, mediaType, eBookAsPdf.headers().getContentLength());

	if (!MediaType.APPLICATION_PDF.equals(mediaType)) {
	    return ResponseEntity.badRequest().body("Only PDF docs supported. Unsupported content-type: " + mediaType);
	}

	Path savedEBookPath = fileSystemUtil.saveMultipartFileAsImage(eBookAsPdf);

	int extractedPageCount = imageProcessor.processEBookInPdf(Long.valueOf(bookIdAsString), eBookName, savedEBookPath);

	return ResponseEntity.ok(Integer.toString(extractedPageCount));
    }

}
