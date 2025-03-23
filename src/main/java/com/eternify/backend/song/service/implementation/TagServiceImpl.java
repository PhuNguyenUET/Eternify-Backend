package com.eternify.backend.song.service.implementation;

import com.eternify.backend.common.exception.BusinessException;
import com.eternify.backend.song.model.Tag;
import com.eternify.backend.song.repository.TagRepository;
import com.eternify.backend.song.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    @Override
    public void addTag(String name) {
        Tag tagCheck = tagRepository.findByName(name);

        if(tagCheck != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tag already exists");
        }

        if(!name.startsWith("#")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tags must start with #");
        }

        Tag tag = Tag.builder()
                .name(name)
                .build();

        tagRepository.save(tag);
    }

    @Override
    public void deleteTag(String id) {
        Tag tag = tagRepository.findById(id).orElse(null);

        if(tag == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Tag doesn't exist");
        }

        tagRepository.delete(tag);
    }

    @Override
    public Tag getTagById(String id) {
        Tag tag = tagRepository.findById(id).orElse(null);

        if(tag == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Tag doesn't exist");
        }

        return tag;
    }

    @Override
    public Tag getTagByName(String name) {
        Tag tag = tagRepository.findByName(name);

        if(tag == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Tag doesn't exist");
        }

        return tag;
    }
}
