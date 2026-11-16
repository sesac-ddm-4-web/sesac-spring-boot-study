package com.example.personal_project.study;

import com.example.personal_project.application.ShortCodeGenerator;
import com.example.personal_project.global.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.global.exception.ShortLinkCodeConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialRegistrationService {
    private final MaterialCreationService creation;
    private final ShortCodeGenerator generator;
    public MaterialRegistrationService(MaterialCreationService creation, ShortCodeGenerator generator) {
        this.creation=creation; this.generator=generator;
    }
    @Transactional(propagation=Propagation.NOT_SUPPORTED)
    public long register(long sid,long uid,String title,String url,String requestedCode) {
        title=StudyValidation.text(title,150,"자료 제목"); url=StudyValidation.url(url);
        String custom=requestedCode==null?null:requestedCode.trim();
        if (custom!=null && (custom.length()<3 || custom.length()>40 || !custom.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")))
            throw new IllegalArgumentException("단축 코드 형식을 확인해주세요.");
        for(int attempt=0;attempt<(custom==null?5:1);attempt++) {
            String code=custom==null?generator.generate():custom;
            try { return creation.create(sid,uid,title,url,code); }
            catch(ShortLinkCodeConflictException e) {
                if(custom!=null) throw new DuplicateShortenUrlCodeException("이미 사용 중인 단축 주소입니다.");
            }
        }
        throw new LackOfShortenUrlKeyException("URL 생성에 실패했습니다.");
    }
}
