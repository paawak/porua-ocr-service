package com.swayam.ocr.porua.tesseract.service;

import java.util.Collection;
import java.util.List;

import com.swayam.ocr.porua.tesseract.model.Book;
import com.swayam.ocr.porua.tesseract.model.OcrWordEntityTemplate;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.PageImage;
import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.rest.train.dto.OcrWordOutputDto;

public interface OcrDataStoreService {

    Book addBook(Book book);

    Book getBook(long bookId);

    List<Book> getBooks();

    PageImage addPageImage(PageImage pageImage);

    PageImage getPageImage(long pageImageId);

    int getPageCount(long bookId);

    List<PageImage> getPages(long bookId);

    int getWordCount(long bookId, long rawImageId);

    Collection<OcrWordOutputDto> getWords(long bookId, long pageImageId, UserDetails userDetails);

    OcrWordEntityTemplate addOcrWord(OcrWordEntityTemplate rawOcrWord);

    int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user);

    OcrWordEntityTemplate getWord(OcrWordId ocrWordId);

    int markWordAsIgnored(OcrWordId ocrWordId, UserDetails user);

    int markPageAsIgnored(long pageImageId);

    int markPageAsCorrectionCompleted(long pageImageId);

}
