package com.swayam.ocr.porua.tesseract.rest.train;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrCorrectionDto;
import com.swayam.ocr.porua.tesseract.service.OcrDataStoreService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ocr/train/correction")
public class OCRCorrectionController {

    private final OcrDataStoreService ocrDataStoreService;

    public OCRCorrectionController(OcrDataStoreService ocrDataStoreService) {
	this.ocrDataStoreService = ocrDataStoreService;
    }

    @PutMapping(value = "/page/ignore/{pageImageId}")
    public ResponseEntity<Integer> markPageAsIgnored(@PathVariable("pageImageId") final long pageImageId) {
	int rowsAffected = ocrDataStoreService.markPageAsIgnored(pageImageId);
	return ResponseEntity.ok(rowsAffected);
    }

    @PutMapping(value = "/page/complete/{pageImageId}")
    public ResponseEntity<Integer> markPageAsCorrectionCompleted(@PathVariable("pageImageId") final long pageImageId) {
	int rowsAffected = ocrDataStoreService.markPageAsCorrectionCompleted(pageImageId);
	return ResponseEntity.ok(rowsAffected);
    }

    @PostMapping(value = "/word", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Integer> applyCorrectionToOcrWords(@RequestBody final List<OcrCorrectionDto> ocrWordsForCorrection) {
	return Flux.fromIterable(ocrWordsForCorrection)
		.map(ocrWordForCorrection -> ocrDataStoreService.updateCorrectTextInOcrWord(ocrWordForCorrection.getOcrWordId(), ocrWordForCorrection.getCorrectedText()));
    }

    @PostMapping(value = "/word/ignore", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Integer> markOcrWordAsIgnored(@RequestBody final List<OcrWordId> wordsToIgnore) {
	return Flux.fromIterable(wordsToIgnore).map(ocrDataStoreService::markWordAsIgnored);
    }

}
