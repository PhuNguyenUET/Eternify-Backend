package com.eternify.backend.song.service.implementation;

import com.eternify.backend.common.exception.BusinessException;
import com.eternify.backend.song.dto.*;
import com.eternify.backend.song.model.Album;
import com.eternify.backend.song.model.Song;
import com.eternify.backend.song.model.Status;
import com.eternify.backend.song.repository.CategoryRepository;
import com.eternify.backend.song.repository.TagRepository;
import com.eternify.backend.song.service.AlbumService;
import com.eternify.backend.user.model.User;
import com.eternify.backend.user.repository.UserRepository;
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
public class AlbumServiceImpl implements AlbumService {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Album.class, AlbumDTO.class).setConverter( context -> {
            Album album = context.getSource();

            AlbumDTO albumDTO = AlbumDTO.builder()
                    .id(album.getId())
                    .name(album.getName())
                    .description(album.getDescription())
                    .owner(userRepository.findById(album.getOwnerId()).orElse(null))
                    .coverPath(album.getCoverPath())
                    .status(album.getStatus())
                    .createdDate(album.getCreatedDate())
                    .modifiedDate(album.getModifiedDate())
                    .build();

            List<Song> songs = album.getSongs().stream().map(songId -> mongoTemplate.findById(songId, Song.class)).toList();

            List<SongAlbumDTO> songAlbumDTOs = songs.stream().map(song -> {
                if(song == null) {
                    return null;
                }

                return SongAlbumDTO.builder()
                        .id(song.getId())
                        .title(song.getTitle())
                        .artist(userRepository.findById(song.getArtistId()).orElse(null))
                        .persistentPathSong(song.getPersistentPathSong())
                        .category(categoryRepository.findById(song.getCategoryId()).orElse(null))
                        .tags(song.getTags().stream().map(tagId -> tagRepository.findById(tagId).orElse(null)).toList())
                        .coverPath(song.getCoverPath())
                        .status(song.getStatus())
                        .additionTime(album.getSongAdditionTime().getOrDefault(song.getId(), null))
                        .build();
            }).toList();

            albumDTO.setSongs(songAlbumDTOs);
            return albumDTO;
        });
    }

    @Override
    public void createAlbum(AlbumAddDTO albumAddDTO) {
        Album album = Album.builder()
                .name(albumAddDTO.getName())
                .description(albumAddDTO.getDescription())
                .ownerId(AuthenticationUtils.getCurrentUser().getId())
                .coverPath(albumAddDTO.getCoverPath())
                .status(albumAddDTO.getStatus())
                .build();

        mongoTemplate.save(album);
    }

    @Override
    public void deleteAlbum(String albumId) {
        Album album = mongoTemplate.findById(albumId, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        mongoTemplate.remove(album);
    }

    @Override
    public void updateAlbum(AlbumEditDTO albumEditDTO) {
        Album album = mongoTemplate.findById(albumEditDTO.getId(), Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        album.setName(albumEditDTO.getName());
        album.setDescription(albumEditDTO.getDescription());
        album.setCoverPath(albumEditDTO.getCoverPath());
        album.setStatus(albumEditDTO.getStatus());

        mongoTemplate.save(album);
    }

    @Override
    public AlbumDTO getAlbum(String albumId) {
        Album album = mongoTemplate.findById(albumId, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        return modelMapper.map(album, AlbumDTO.class);
    }

    @Override
    public void cloneAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        Album cloneAlbum = Album.builder()
                .name(album.getName())
                .description(album.getDescription())
                .ownerId(AuthenticationUtils.getCurrentUser().getId())
                .coverPath(album.getCoverPath())
                .status(album.getStatus())
                .songs(album.getSongs())
                .songAdditionTime(album.getSongAdditionTime())
                .categoryFrequency(album.getCategoryFrequency())
                .tagFrequency(album.getTagFrequency())
                .mainCategory(album.getMainCategory())
                .mainTag(album.getMainTag())
                .build();

        mongoTemplate.save(cloneAlbum);
    }

    @Override
    public void addSongToAlbum(AddRemoveSongDTO addRemoveSongDTO) {
        String albumId = addRemoveSongDTO.getAlbumId();
        String songId = addRemoveSongDTO.getSongId();

        Album album = mongoTemplate.findById(albumId, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        album.getSongs().add(songId);
        album.getSongAdditionTime().put(songId, new Date());

        album.getCategoryFrequency().put(song.getCategoryId(), album.getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 1);

        if(album.getCategoryFrequency().get(song.getCategoryId()) > album.getCategoryFrequency().getOrDefault(album.getMainCategory(), 0)) {
            album.setMainCategory(song.getCategoryId());
        }

        for(String tagId : song.getTags()) {
            album.getTagFrequency().put(tagId, album.getTagFrequency().getOrDefault(tagId, 0) + 1);

            if(album.getTagFrequency().get(tagId) > album.getTagFrequency().getOrDefault(album.getMainTag(), 0)) {
                album.setMainTag(tagId);
            }
        }

        mongoTemplate.save(album);
    }

    @Override
    public void removeSongFromAlbum(AddRemoveSongDTO dto) {
        String albumId = dto.getAlbumId();
        String songId = dto.getSongId();

        Album album = mongoTemplate.findById(albumId, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        Song song = mongoTemplate.findById(songId, Song.class);

        if(song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        album.getSongs().remove(songId);
        album.getSongAdditionTime().remove(songId);

        album.getCategoryFrequency().put(song.getCategoryId(), album.getCategoryFrequency().get(song.getCategoryId()) - 1);

        if(album.getCategoryFrequency().get(song.getCategoryId()) == 0) {
            album.getCategoryFrequency().remove(song.getCategoryId());
        }

        if(song.getCategoryId().equals(album.getMainCategory())) {
            album.setMainCategory("");

            for(String categoryId : album.getCategoryFrequency().keySet()) {
                if(album.getCategoryFrequency().get(categoryId) > album.getCategoryFrequency().getOrDefault(album.getMainCategory(), 0)) {
                    album.setMainCategory(categoryId);
                }
            }
        }

        for(String tagId : song.getTags()) {
            album.getTagFrequency().put(tagId, album.getTagFrequency().get(tagId) - 1);

            if(album.getTagFrequency().get(tagId) == 0) {
                album.getTagFrequency().remove(tagId);
            }

            if(tagId.equals(album.getMainTag())) {
                album.setMainTag("");

                for(String tagIdEntry : album.getTagFrequency().keySet()) {
                    if(album.getTagFrequency().get(tagIdEntry) > album.getTagFrequency().getOrDefault(album.getMainTag(), 0)) {
                        album.setMainTag(tagIdEntry);
                    }
                }
            }
        }

        mongoTemplate.save(album);
    }

    @Override
    public void changeSongOrder(ChangeOrderSongDTO changeOrderSongDTO) {
        Album album = mongoTemplate.findById(changeOrderSongDTO.getAlbumId(), Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        if(!album.getSongs().contains(changeOrderSongDTO.getSongId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist in the album");
        }

        if(changeOrderSongDTO.getOrder() < 0 || changeOrderSongDTO.getOrder() >= album.getSongs().size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid order");
        }

        album.getSongs().remove(changeOrderSongDTO.getSongId());
        album.getSongs().add(changeOrderSongDTO.getOrder(), changeOrderSongDTO.getSongId());

        mongoTemplate.save(album);
    }

    @Override
    public void openAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        album.setStatus(Status.PUBLIC);

        mongoTemplate.save(album);
    }

    @Override
    public void closeAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if(!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        album.setStatus(Status.PRIVATE);

        mongoTemplate.save(album);
    }

    @Override
    public void favoriteAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        if(!currentUser.getUserPref().getFavoriteAlbums().contains(id)) {
            currentUser.getUserPref().getFavoriteAlbums().add(id);
        }

        mongoTemplate.save(currentUser);
    }

    @Override
    public void unfavoriteAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if(album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        currentUser.getUserPref().getFavoriteAlbums().remove(id);

        mongoTemplate.save(currentUser);
    }

    @Override
    public List<AlbumDTO> searchByName(String prefix) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex("^" + prefix));

        return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
    }

    @Override
    public List<AlbumDTO> searchByArtist(String artistId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(artistId));

        return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
    }

    @Override
    public List<AlbumDTO> searchByCategory(String categoryId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mainCategory").is(categoryId));

        return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
    }

    @Override
    public List<AlbumDTO> searchByTag(List<String> tags) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mainTag").in(tags));

        return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
    }

    @Override
    public List<AlbumDTO> getAlbumRecommendations() {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<String> topTags = currentUser.getUserPref().getTagFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        List<String> topCategories = currentUser.getUserPref().getCategoryFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        Set<AlbumDTO> rawRecommendations = new HashSet<>();

        rawRecommendations.addAll(searchByTag(topTags));
        rawRecommendations.addAll(searchByCategory(topCategories.get(0)));
        rawRecommendations.addAll(searchByCategory(topCategories.get(1)));

        if(rawRecommendations.size() < 10) {
            rawRecommendations.addAll(searchByCategory(categoryRepository.findByName("Pop").getId()));
        }

        return rawRecommendations.stream().limit(10).toList();
    }
}
