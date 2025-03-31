package com.eternify.backend.song.service;

import com.eternify.backend.song.model.Country;
import com.eternify.backend.song.model.Country;

import java.util.List;

public interface CountryService {
    void addCountry(String name);
    void deleteCountry(String id);
    Country getCountryById(String id);
    Country getCountryByName(String name);
    void addFavouriteCountry(List<String> countryIds);
}
