package com.swayam.ocr.porua.tesseract.repo.dynamic;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.swayam.ocr.porua.tesseract.model.UserDetails;
import com.swayam.ocr.porua.tesseract.model.dynamic.RajshekharBasuMahabharatBanglaCorrectedWordEntity;
import com.swayam.ocr.porua.tesseract.repo.CorrectedWordRepositoryTemplate;

//@Repository // ("com.swayam.ocr.porua.tesseract.repo.dynamic.RajshekharBasuMahabharatBanglaCorrectedWordRepository")
public interface RajshekharBasuMahabharatBanglaCorrectedWordRepository extends CrudRepository<RajshekharBasuMahabharatBanglaCorrectedWordEntity, Long>, CorrectedWordRepositoryTemplate {

    @Override
    @Modifying
    @Query("update RajshekharBasuMahabharatBanglaCorrectedWordEntity set correctedText = :correctedText where ocrWordId = :ocrWordId and user = :user")
    int updateCorrectedText(long ocrWordId, String correctedText, UserDetails user);

    @Override
    @Modifying
    @Query("update RajshekharBasuMahabharatBanglaCorrectedWordEntity set ignored = TRUE where ocrWordId = :ocrWordId and user = :user")
    int markAsIgnored(long ocrWordId, UserDetails user);

}
