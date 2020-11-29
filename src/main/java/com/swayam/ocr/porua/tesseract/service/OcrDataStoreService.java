package com.swayam.ocr.porua.tesseract.service;

import java.util.Collection;
import java.util.List;

import com.swayam.ocr.porua.tesseract.model.Book;
import com.swayam.ocr.porua.tesseract.model.OcrWord;
import com.swayam.ocr.porua.tesseract.model.OcrWordId;
import com.swayam.ocr.porua.tesseract.model.PageImage;
import com.swayam.ocr.porua.tesseract.model.UserDetails;

public interface OcrDataStoreService {

    Book addBook(Book book);

    Book getBook(long bookId);

    List<Book> getBooks();

    PageImage addPageImage(PageImage pageImage);

    PageImage getPageImage(long pageImageId);

    int getPageCount(long bookId);

    List<PageImage> getPages(long bookId);

    int getWordCount(long bookId, long rawImageId);

    Collection<OcrWord> getWords(long bookId, long pageImageId);

    OcrWord addOcrWord(OcrWord rawOcrWord);

    int updateCorrectTextInOcrWord(OcrWordId ocrWordId, String correctedText, UserDetails user);

    OcrWord getWord(OcrWordId ocrWordId);

    void removeWord(OcrWordId ocrWordId);

    int markWordAsIgnored(OcrWordId ocrWordId);

    int markPageAsIgnored(long pageImageId);

    int markPageAsCorrectionCompleted(long pageImageId);

}
