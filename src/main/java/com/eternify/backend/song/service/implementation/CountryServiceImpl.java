package com.eternify.backend.song.service.implementation;

import com.eternify.backend.common.exception.BusinessException;
import com.eternify.backend.song.model.Country;
import com.eternify.backend.song.repository.CountryRepository;
import com.eternify.backend.song.service.CountryService;
import com.eternify.backend.user.model.User;
import com.eternify.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;

    @Override
    public void addCountry(String name) {
        Country countryCheck = countryRepository.findByName(name);

        if(countryCheck != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Country already exists");
        }

        Country country = Country.builder()
                .name(name)
                .build();

        countryRepository.save(country);
    }

    @Override
    public void deleteCountry(String id) {
        Country country = countryRepository.findById(id).orElse(null);

        if(country == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Country doesn't exist");
        }

        countryRepository.delete(country);
    }

    @Override
    public Country getCountryById(String id) {
        Country country = countryRepository.findById(id).orElse(null);

        if(country == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Country doesn't exist");
        }

        return country;
    }

    @Override
    public Country getCountryByName(String name) {
        Country country = countryRepository.findByName(name);

        if(country == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Country doesn't exist");
        }

        return country;
    }

    @Override
    public void addFavouriteCountry(List<String> countryIds) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        for(String countryId : countryIds) {
            currentUser.getUserPref().getCountryFrequency().put(countryId, currentUser.getUserPref().getCountryFrequency().getOrDefault(countryId, 0) + 10);
        }
    }
}
