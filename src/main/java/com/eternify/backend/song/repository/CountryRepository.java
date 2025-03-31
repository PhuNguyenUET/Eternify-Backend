package com.eternify.backend.song.repository;

import com.eternify.backend.song.model.Category;
import com.eternify.backend.song.model.Country;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CountryRepository extends MongoRepository<Country, String> {
    Country findByName(String name);
}
