package com.eternify.backend.song.service.implementation;

import com.eternify.backend.common.exception.BusinessException;
import com.eternify.backend.song.dto.SongAddDTO;
import com.eternify.backend.song.dto.SongDTO;
import com.eternify.backend.song.dto.SongEditDTO;
import com.eternify.backend.song.model.Album;
import com.eternify.backend.song.model.Song;
import com.eternify.backend.song.model.Status;
import com.eternify.backend.song.repository.CategoryRepository;
import com.eternify.backend.song.repository.CountryRepository;
import com.eternify.backend.song.repository.TagRepository;
import com.eternify.backend.song.service.SongService;
import com.eternify.backend.user.repository.UserRepository;
import com.eternify.backend.user.model.Role;
import com.eternify.backend.user.model.User;
import com.eternify.backend.util.AuthenticationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Song.class, SongDTO.class)
                .setConverter(context -> {
                   Song source = context.getSource();

                   return SongDTO.builder()
                           .id(source.getId())
                           .title(source.getTitle())
                           .artist(userRepository.findById(source.getArtistId()).orElse(null))
                           .persistentSongId(source.getPersistentSongId())
                           .category(categoryRepository.findById(source.getCategoryId()).orElse(null))
                           .country(countryRepository.findById(source.getCountryId()).orElse(null))
                           .tags(source.getTags().stream().map(tagId -> tagRepository.findById(tagId).orElse(null)).toList())
                           .persistentCoverId(source.getPersistentCoverId())
                           .length(source.getLength())
                           .status(source.getStatus())
                           .createdDate(source.getCreatedDate())
                           .modifiedDate(source.getModifiedDate())
                           .build();
                });
    }

    @Override
    public void addSong(SongAddDTO songAddDTO) {
        if(!AuthenticationUtils.getCurrentUser().getRole().equals(Role.ARTIST.toString())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You are not allowed to add song");
        }

        Song song = Song.builder()
                .title(songAddDTO.getTitle())
                .artistId(AuthenticationUtils.getCurrentUser().getId())
                .persistentSongId(songAddDTO.getPersistentSongId())
                .categoryId(songAddDTO.getCategoryId())
                .countryId(songAddDTO.getCountryId())
                .tags(songAddDTO.getTags())
                .length(songAddDTO.getLength())
                .persistentCoverId(songAddDTO.getPersistentCoverId())
                .status(songAddDTO.getStatus().equals(Status.PUBLIC.toString()) ? Status.PUBLIC.toString() : Status.PRIVATE.toString())
                .build();

        mongoTemplate.save(song);
    }

    @Override
    public void editSong(SongEditDTO songEditDTO) {
        Song song = mongoTemplate.findById(songEditDTO.getId(), Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        if(!song.getArtistId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You are not allowed to edit this song");
        }

        song.setTitle(songEditDTO.getTitle());
        song.setPersistentCoverId(songEditDTO.getPersistentCoverId());
        song.setStatus(songEditDTO.getStatus().equals(Status.PUBLIC.toString()) ? Status.PUBLIC.toString() : Status.PRIVATE.toString());
        song.setCategoryId(songEditDTO.getCategoryId());
        song.setLength(songEditDTO.getLength());
        song.setCountryId(songEditDTO.getCountryId());
        song.setTags(songEditDTO.getTags());

        mongoTemplate.save(song);
    }

    @Override
    public void deleteSong(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        if(!song.getArtistId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You are not allowed to delete this song");
        }

        mongoTemplate.remove(song);
    }

    @Override
    public SongDTO getSong(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        if(song.getStatus().equals(Status.PRIVATE.toString()) && !song.getArtistId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You are not allowed to view this song");
        }

        return modelMapper.map(song, SongDTO.class);
    }

    @Override
    public void openSong(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        if(!song.getArtistId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You are not allowed to open this song");
        }

        song.setStatus(Status.PUBLIC.toString());

        mongoTemplate.save(song);
    }

    @Override
    public void closeSong(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        if(!song.getArtistId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You are not allowed to close this song");
        }

        song.setStatus(Status.PRIVATE.toString());

        mongoTemplate.save(song);
    }

    @Override
    public void favoriteSong(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        currentUser.getUserPref().getFavoriteSongs().remove(songId);
        currentUser.getUserPref().getFavoriteSongs().add(songId);

        for(String tagId : song.getTags()) {
            currentUser.getUserPref().getTagFrequency().put(tagId, currentUser.getUserPref().getTagFrequency().getOrDefault(tagId, 0) + 10);
        }

        currentUser.getUserPref().getCategoryFrequency().put(song.getCategoryId(), currentUser.getUserPref().getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 10);
        currentUser.getUserPref().getCountryFrequency().put(song.getCountryId(), currentUser.getUserPref().getCountryFrequency().getOrDefault(song.getCountryId(), 0) + 10);

        mongoTemplate.save(AuthenticationUtils.getCurrentUser());
    }

    @Override
    public void unfavoriteSong(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        if(!currentUser.getUserPref().getFavoriteSongs().contains(songId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Song is not in your favorite list");
        }

        currentUser.getUserPref().getFavoriteSongs().remove(songId);

        for(String tagId : song.getTags()) {
            currentUser.getUserPref().getTagFrequency().put(tagId, currentUser.getUserPref().getTagFrequency().getOrDefault(tagId, 0) - 10);
        }

        currentUser.getUserPref().getCategoryFrequency().put(song.getCategoryId(), currentUser.getUserPref().getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) - 10);
        currentUser.getUserPref().getCountryFrequency().put(song.getCountryId(), currentUser.getUserPref().getCountryFrequency().getOrDefault(song.getCountryId(), 0) - 10);

        mongoTemplate.save(AuthenticationUtils.getCurrentUser());
    }

    @Override
    public List<SongDTO> searchByName(String prefix, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("title").regex(".*" + Pattern.quote(prefix) + ".*", "i"));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if(limit <= 0) {
            return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Song.class).stream().limit(limit).map(song -> modelMapper.map(song, SongDTO.class)).toList();
        }
    }

    @Override
    public List<SongDTO> searchByCategory(String categoryId, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("categoryId").is(categoryId));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if(limit <= 0) {
            return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Song.class).stream().limit(limit).map(song -> modelMapper.map(song, SongDTO.class)).toList();
        }
    }

    @Override
    public List<SongDTO> searchByCountry(String countryId, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("countryId").is(countryId));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if(limit <= 0) {
            return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Song.class).stream().limit(limit).map(song -> modelMapper.map(song, SongDTO.class)).toList();
        }
    }

    @Override
    public List<SongDTO> searchByArtist(String artistId, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("artistId").is(artistId));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if(limit <= 0) {
            return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Song.class).stream().limit(limit).map(song -> modelMapper.map(song, SongDTO.class)).toList();
        }
    }

    @Override
    public List<SongDTO> searchByTag(List<String> tagIds, int limit) {
        if(tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(tagIds));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if(limit <= 0) {
            return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Song.class).stream().limit(limit).map(song -> modelMapper.map(song, SongDTO.class)).toList();
        }
    }

    @Override
    public List<SongDTO> getUserRecommendations(int limit) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<String> topTags = currentUser.getUserPref().getTagFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        Set<SongDTO> recommendationsRaw = new HashSet<>(searchByTag(topTags, 0));

        List<String> topCategories = currentUser.getUserPref().getCategoryFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        for (String categoryId : topCategories) {
            recommendationsRaw.addAll(searchByCategory(categoryId, 0));
        }

        List<String> topCountries = currentUser.getUserPref().getCountryFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        for(String countryId : topCountries) {
            recommendationsRaw.addAll(searchByCountry(countryId, 0));
        }

        if(recommendationsRaw.size() < 30) {
            recommendationsRaw.addAll(searchByCategory(categoryRepository.findByName("Pop").getId(), 0));
        }

        recommendationsRaw.removeIf(songDto -> songDto.getStatus().equals(Status.PRIVATE.toString()) );

        if(limit <= 0) {
            return recommendationsRaw.stream().toList();
        } else {
            return recommendationsRaw.stream().limit(limit).toList();
        }
    }

    @Override
    public List<SongDTO> getUserHistory(int limit) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<String> songIds = new ArrayList<>(currentUser.getUserPref().getSongHistory());

        if(limit <= 0) {
            return songIds.stream().map(songId -> modelMapper.map(mongoTemplate.findById(songId, Song.class), SongDTO.class)).toList();
        } else {
            return songIds.stream().limit(limit).map(songId -> modelMapper.map(mongoTemplate.findById(songId, Song.class), SongDTO.class)).toList();
        }
    }

    @Override
    public List<SongDTO> getFavorites(int limit) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        if(limit <= 0) {
            return currentUser.getUserPref().getFavoriteSongs().stream().map(songId -> modelMapper.map(mongoTemplate.findById(songId, Song.class), SongDTO.class)).toList();
        } else {
            return currentUser.getUserPref().getFavoriteSongs().stream().limit(limit).map(songId -> modelMapper.map(mongoTemplate.findById(songId, Song.class), SongDTO.class)).toList();
        }
    }

    @Override
    public List<SongDTO> getAlbumRecommendations(String albumId, int limit) {
        Album album = mongoTemplate.findById(albumId, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        Set<SongDTO> recommendationsRaw = new HashSet<>(searchByCategory(album.getMainCategory(), 0));

        List<String> albumTags = new ArrayList<>();
        albumTags.add(album.getMainTag());

        recommendationsRaw.addAll(searchByTag(albumTags, 0));

        recommendationsRaw.addAll(searchByCountry(album.getMainCountry(), 0));

        recommendationsRaw.removeIf(songDto -> songDto.getStatus().equals(Status.PRIVATE.toString()));

        if(limit <= 0) {
            return recommendationsRaw.stream().toList();
        } else {
            return recommendationsRaw.stream().limit(limit).toList();
        }
    }

    @Override
    public void songListened(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        List<String> history = currentUser.getUserPref().getSongHistory();
        history.remove(songId);
        currentUser.getUserPref().getSongHistory().add(songId);

        for(String tagId : song.getTags()) {
            currentUser.getUserPref().getTagFrequency().put(tagId, currentUser.getUserPref().getTagFrequency().getOrDefault(tagId, 0) + 1);
        }

        currentUser.getUserPref().getCategoryFrequency().put(song.getCategoryId(), currentUser.getUserPref().getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 1);
        currentUser.getUserPref().getCountryFrequency().put(song.getCountryId(), currentUser.getUserPref().getCountryFrequency().getOrDefault(song.getCountryId(), 0) + 1);

        mongoTemplate.save(currentUser);
    }

    @Override
    public void updateFavouriteArtistForRecommendations(List<String> artistIds) {
        for(String artistId : artistIds) {
            List<SongDTO> artistSongs = searchByArtist(artistId, 0);

            for(SongDTO song : artistSongs) {
                songListened(song.getId());
            }
        }
    }
}
