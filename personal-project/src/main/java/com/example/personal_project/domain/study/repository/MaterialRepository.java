package com.example.personal_project.domain.study.repository;

import com.example.personal_project.domain.study.model.Material;

public interface MaterialRepository {
    Material material(long id);
    long insertMaterial(long sessionId, long authorId, String title, String url);
    void updateMaterial(long id, String title, String url);
}
