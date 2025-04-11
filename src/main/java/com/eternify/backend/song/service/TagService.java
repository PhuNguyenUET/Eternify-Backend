package com.eternify.backend.song.service;

import com.eternify.backend.song.model.Tag;

import java.util.List;

public interface TagService {
    void addTag(String name);
    void deleteTag(String id);
    Tag getTagById(String id);
    Tag getTagByName(String name);
    List<Tag> getAllTags();
}
