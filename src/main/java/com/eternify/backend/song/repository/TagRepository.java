package com.eternify.backend.song.repository;

import com.eternify.backend.song.model.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TagRepository extends MongoRepository<Tag, String> {
    Tag findByName(String name);
}
