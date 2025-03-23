package com.eternify.backend.song.service.implementation;

import com.eternify.backend.common.exception.BusinessException;
import com.eternify.backend.song.dto.SongAddDTO;
import com.eternify.backend.song.dto.SongDTO;
import com.eternify.backend.song.dto.SongEditDTO;
import com.eternify.backend.song.model.Album;
import com.eternify.backend.song.model.Song;
import com.eternify.backend.song.model.Status;
import com.eternify.backend.song.repository.CategoryRepository;
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

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
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
                           .tags(source.getTags().stream().map(tagId -> tagRepository.findById(tagId).orElse(null)).toList())
                           .coverPath(source.getCoverPath())
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
                .persistentPathSong(songAddDTO.getPersistentPathSong())
                .categoryId(songAddDTO.getCategoryId())
                .tags(songAddDTO.getTags())
                .coverPath(songAddDTO.getCoverPath())
                .status(songAddDTO.getStatus())
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
        song.setCoverPath(songEditDTO.getCoverPath());
        song.setStatus(songEditDTO.getStatus());
        song.setCategoryId(songEditDTO.getCategoryId());
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

        song.setStatus(Status.PUBLIC);

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

        song.setStatus(Status.PRIVATE);

        mongoTemplate.save(song);
    }

    @Override
    public void favoriteSong(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        currentUser.getUserPref().getFavoriteSongs().add(songId);

        for(String tagId : song.getTags()) {
            currentUser.getUserPref().getTagFrequency().put(tagId, currentUser.getUserPref().getTagFrequency().getOrDefault(tagId, 0) + 10);
        }

        currentUser.getUserPref().getCategoryFrequency().put(song.getCategoryId(), currentUser.getUserPref().getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 10);

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

        mongoTemplate.save(AuthenticationUtils.getCurrentUser());
    }

    @Override
    public List<SongDTO> searchByName(String prefix) {
        Query query = new Query();
        query.addCriteria(Criteria.where("title").regex("^" + prefix));

        return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
    }

    @Override
    public List<SongDTO> searchByCategory(String categoryId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("categoryId").is(categoryId));

        return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
    }

    @Override
    public List<SongDTO> searchByArtist(String artistId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("artistId").is(artistId));

        return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
    }

    @Override
    public List<SongDTO> searchByTag(List<String> tagNames) {
        List<String> tagIds = tagNames.stream().map(tagName -> tagRepository.findByName(tagName).getId()).toList();

        if(tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(tagIds));

        return mongoTemplate.find(query, Song.class).stream().map(song -> modelMapper.map(song, SongDTO.class)).toList();
    }

    @Override
    public List<SongDTO> getUserRecommendations() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<String> topTags = currentUser.getUserPref().getTagFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        Set<SongDTO> recommendationsRaw = new HashSet<>(searchByTag(topTags));

        List<String> topCategories = currentUser.getUserPref().getCategoryFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        recommendationsRaw.addAll(searchByCategory(topCategories.get(0)));
        recommendationsRaw.addAll(searchByCategory(topCategories.get(1)));

        return recommendationsRaw.stream().limit(30).toList();
    }

    @Override
    public List<SongDTO> getUserHistory() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<String> songIds = new ArrayList<>(currentUser.getUserPref().getSongHistory());
        Collections.shuffle(songIds);

        return songIds.stream().limit(30).map(songId -> modelMapper.map(mongoTemplate.findById(songId, Song.class), SongDTO.class)).toList();
    }

    @Override
    public List<SongDTO> getFavorites() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        return currentUser.getUserPref().getFavoriteSongs().stream().map(songId -> modelMapper.map(mongoTemplate.findById(songId, Song.class), SongDTO.class)).toList();
    }

    @Override
    public List<SongDTO> getAlbumRecommendations(String albumId) {
        Album album = mongoTemplate.findById(albumId, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        Set<SongDTO> recommendationsRaw = new HashSet<>(searchByCategory(album.getMainCategory()));

        List<String> albumTags = new ArrayList<>();
        albumTags.add(album.getMainTag());

        recommendationsRaw.addAll(searchByTag(albumTags));

        return recommendationsRaw.stream().limit(30).toList();
    }

    @Override
    public void songListened(String songId) {
        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        currentUser.getUserPref().getSongHistory().add(songId);

        for(String tagId : song.getTags()) {
            currentUser.getUserPref().getTagFrequency().put(tagId, currentUser.getUserPref().getTagFrequency().getOrDefault(tagId, 0) + 1);
        }

        currentUser.getUserPref().getCategoryFrequency().put(song.getCategoryId(), currentUser.getUserPref().getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 1);

        mongoTemplate.save(currentUser);
    }
}
