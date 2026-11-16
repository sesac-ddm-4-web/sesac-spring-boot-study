package com.example.personal_project.application.study;

import com.example.personal_project.domain.study.exception.StudyException;
import com.example.personal_project.domain.study.model.StudyValidation;
import com.example.personal_project.domain.study.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialService {
    private final MaterialRepository materials;
    public MaterialService(MaterialRepository materials) { this.materials = materials; }
    @Transactional
    public void update(long id, long userId, String title, String url) {
        StudyValidation.positive(id);
        var material = materials.material(id);
        if (material == null) throw StudyException.missing();
        material.requireAuthor(userId);
        materials.updateMaterial(id, StudyValidation.text(title, 150, "자료 제목"), StudyValidation.url(url));
    }
}
