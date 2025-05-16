package com.eternify.backend.song.service.implementation;

import com.eternify.backend.common.exception.BusinessException;
import com.eternify.backend.song.dto.*;
import com.eternify.backend.song.model.Album;
import com.eternify.backend.song.model.AlbumType;
import com.eternify.backend.song.model.Song;
import com.eternify.backend.song.model.Status;
import com.eternify.backend.song.repository.CategoryRepository;
import com.eternify.backend.song.repository.CountryRepository;
import com.eternify.backend.song.repository.TagRepository;
import com.eternify.backend.song.service.AlbumService;
import com.eternify.backend.user.model.Role;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private ModelMapper modelMapper;

    @PostConstruct
    private void setupModelMapper() {
        modelMapper = new ModelMapper();

        modelMapper.createTypeMap(Album.class, AlbumDTO.class).setConverter(context -> {
            Album album = context.getSource();

            AlbumDTO albumDTO = AlbumDTO.builder()
                    .id(album.getId())
                    .name(album.getName())
                    .description(album.getDescription())
                    .owner(userRepository.findById(album.getOwnerId()).orElse(null))
                    .persistentCoverId(album.getPersistentCoverId())
                    .status(album.getStatus())
                    .albumType(album.getAlbumType())
                    .createdDate(album.getCreatedDate())
                    .modifiedDate(album.getModifiedDate())
                    .build();

            List<Song> songs = album.getSongs().stream().map(songId -> mongoTemplate.findById(songId, Song.class)).toList();

            List<SongAlbumDTO> songAlbumDTOs = songs.stream().map(song -> {
                if (song == null) {
                    return null;
                }

                return SongAlbumDTO.builder()
                        .id(song.getId())
                        .title(song.getTitle())
                        .artist(userRepository.findById(song.getArtistId()).orElse(null))
                        .persistentSongId(song.getPersistentSongId())
                        .category(categoryRepository.findById(song.getCategoryId()).orElse(null))
                        .country(countryRepository.findById(song.getCountryId()).orElse(null))
                        .length(song.getLength())
                        .tags(song.getTags().stream().map(tagId -> tagRepository.findById(tagId).orElse(null)).toList())
                        .persistentCoverId(song.getPersistentCoverId())
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
                .persistentCoverId(albumAddDTO.getPersistentCoverId())
                .status(albumAddDTO.getStatus().equals(Status.PUBLIC.toString()) ? Status.PUBLIC.toString() : Status.PRIVATE.toString())
                .build();

        album = mongoTemplate.save(album);

        if (AuthenticationUtils.getCurrentUser().getRole().equals(Role.ARTIST.toString())) {
            album.setAlbumType(albumAddDTO.getAlbumType().equals(AlbumType.ARTIST_ALBUM.toString()) ? AlbumType.ARTIST_ALBUM.toString() : AlbumType.PLAYLIST.toString());
        } else {
            album.setAlbumType(AlbumType.PLAYLIST.toString());
        }

        for (String songId : albumAddDTO.getSongs()) {
            Song song = mongoTemplate.findById(songId, Song.class);

            if (song == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
            }

            album.getSongs().add(songId);
            album.getSongAdditionTime().put(songId, new Date());

            album.getCategoryFrequency().put(song.getCategoryId(), album.getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 1);

            if (album.getCategoryFrequency().get(song.getCategoryId()) > album.getCategoryFrequency().getOrDefault(album.getMainCategory(), 0)) {
                album.setMainCategory(song.getCategoryId());
            }

            album.getCountryFrequency().put(song.getCountryId(), album.getCountryFrequency().getOrDefault(song.getCountryId(), 0) + 1);

            if (album.getCountryFrequency().get(song.getCountryId()) > album.getCountryFrequency().getOrDefault(album.getMainCountry(), 0)) {
                album.setMainCountry(song.getCountryId());
            }

            for (String tagId : song.getTags()) {
                album.getTagFrequency().put(tagId, album.getTagFrequency().getOrDefault(tagId, 0) + 1);

                if (album.getTagFrequency().get(tagId) > album.getTagFrequency().getOrDefault(album.getMainTag(), 0)) {
                    album.setMainTag(tagId);
                }
            }
        }

        mongoTemplate.save(album);
    }

    @Override
    public void deleteAlbum(String albumId) {
        Album album = mongoTemplate.findById(albumId, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        mongoTemplate.remove(album);
    }

    @Override
    public void updateAlbum(AlbumEditDTO albumEditDTO) {
        Album album = mongoTemplate.findById(albumEditDTO.getId(), Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        album.setName(albumEditDTO.getName());
        album.setDescription(albumEditDTO.getDescription());
        album.setPersistentCoverId(albumEditDTO.getPersistentCoverId());
        album.setStatus(albumEditDTO.getStatus().equals(Status.PUBLIC.toString()) ? Status.PUBLIC.toString() : Status.PRIVATE.toString());

        if (AuthenticationUtils.getCurrentUser().getRole().equals(Role.ARTIST.toString())) {
            album.setAlbumType(albumEditDTO.getAlbumType().equals(AlbumType.ARTIST_ALBUM.toString()) ? AlbumType.ARTIST_ALBUM.toString() : AlbumType.PLAYLIST.toString());
        } else {
            album.setAlbumType(AlbumType.PLAYLIST.toString());
        }

        mongoTemplate.save(album);
    }

    @Override
    public AlbumDTO getAlbum(String albumId) {
        Album album = mongoTemplate.findById(albumId, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (album.getStatus().equals(Status.PRIVATE.toString()) && !album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this private album");
        }

        return modelMapper.map(album, AlbumDTO.class);
    }

    @Override
    public void cloneAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (album.getStatus().equals(Status.PRIVATE.toString()) && !album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this private album");
        }

        Album cloneAlbum = Album.builder()
                .name(album.getName())
                .description(album.getDescription())
                .ownerId(AuthenticationUtils.getCurrentUser().getId())
                .persistentCoverId(album.getPersistentCoverId())
                .status(album.getStatus())
                .songs(album.getSongs())
                .albumType(AlbumType.PLAYLIST.toString())
                .songAdditionTime(album.getSongAdditionTime())
                .categoryFrequency(album.getCategoryFrequency())
                .tagFrequency(album.getTagFrequency())
                .countryFrequency(album.getCountryFrequency())
                .mainCategory(album.getMainCategory())
                .mainTag(album.getMainTag())
                .mainCountry(album.getMainCountry())
                .build();

        mongoTemplate.save(cloneAlbum);
    }

    @Override
    public void addSongToAlbum(AddRemoveSongDTO addRemoveSongDTO) {
        String albumId = addRemoveSongDTO.getAlbumId();
        String songId = addRemoveSongDTO.getSongId();

        Album album = mongoTemplate.findById(albumId, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        if (album.getSongs().contains(songId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Song already exists in the album");
        }

        Song song = mongoTemplate.findById(songId, Song.class);

        if (song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        album.getSongs().add(songId);
        album.getSongAdditionTime().put(songId, new Date());

        album.getCategoryFrequency().put(song.getCategoryId(), album.getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 1);

        if (album.getCategoryFrequency().get(song.getCategoryId()) > album.getCategoryFrequency().getOrDefault(album.getMainCategory(), 0)) {
            album.setMainCategory(song.getCategoryId());
        }

        album.getCountryFrequency().put(song.getCountryId(), album.getCountryFrequency().getOrDefault(song.getCountryId(), 0) + 1);

        if (album.getCountryFrequency().get(song.getCountryId()) > album.getCountryFrequency().getOrDefault(album.getMainCountry(), 0)) {
            album.setMainCountry(song.getCountryId());
        }

        for (String tagId : song.getTags()) {
            album.getTagFrequency().put(tagId, album.getTagFrequency().getOrDefault(tagId, 0) + 1);

            if (album.getTagFrequency().get(tagId) > album.getTagFrequency().getOrDefault(album.getMainTag(), 0)) {
                album.setMainTag(tagId);
            }
        }

        mongoTemplate.save(album);
    }

    @Override
    public void addSongBatchToAlbum(AddRemoveSongBatchDTO dto) {
        String albumId = dto.getAlbumId();
        List<String> songs = dto.getSongs();

        Album album = mongoTemplate.findById(albumId, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }
        for (String songId : songs) {
            if (album.getSongs().contains(songId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Song already exists in the album");
            }

            Song song = mongoTemplate.findById(songId, Song.class);

            if (song == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
            }

            album.getSongs().add(songId);
            album.getSongAdditionTime().put(songId, new Date());

            album.getCategoryFrequency().put(song.getCategoryId(), album.getCategoryFrequency().getOrDefault(song.getCategoryId(), 0) + 1);

            if (album.getCategoryFrequency().get(song.getCategoryId()) > album.getCategoryFrequency().getOrDefault(album.getMainCategory(), 0)) {
                album.setMainCategory(song.getCategoryId());
            }

            album.getCountryFrequency().put(song.getCountryId(), album.getCountryFrequency().getOrDefault(song.getCountryId(), 0) + 1);

            if (album.getCountryFrequency().get(song.getCountryId()) > album.getCountryFrequency().getOrDefault(album.getMainCountry(), 0)) {
                album.setMainCountry(song.getCountryId());
            }

            for (String tagId : song.getTags()) {
                album.getTagFrequency().put(tagId, album.getTagFrequency().getOrDefault(tagId, 0) + 1);

                if (album.getTagFrequency().get(tagId) > album.getTagFrequency().getOrDefault(album.getMainTag(), 0)) {
                    album.setMainTag(tagId);
                }
            }
        }

        mongoTemplate.save(album);
    }

    @Override
    public void removeSongBatchFromAlbum(AddRemoveSongBatchDTO dto) {
        String albumId = dto.getAlbumId();
        List<String> songs = dto.getSongs();

        Album album = mongoTemplate.findById(albumId, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        for (String songId : songs) {
            if (!album.getSongs().contains(songId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Song doesn't exist in the album");
            }

            Song song = mongoTemplate.findById(songId, Song.class);

            if (song == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
            }

            album.getSongs().remove(songId);
            album.getSongAdditionTime().remove(songId);

            album.getCategoryFrequency().put(song.getCategoryId(), album.getCategoryFrequency().get(song.getCategoryId()) - 1);

            if (album.getCategoryFrequency().get(song.getCategoryId()) == 0) {
                album.getCategoryFrequency().remove(song.getCategoryId());
            }

            if (song.getCategoryId().equals(album.getMainCategory())) {
                album.setMainCategory("");

                for (String categoryId : album.getCategoryFrequency().keySet()) {
                    if (album.getCategoryFrequency().get(categoryId) > album.getCategoryFrequency().getOrDefault(album.getMainCategory(), 0)) {
                        album.setMainCategory(categoryId);
                    }
                }
            }

            album.getCountryFrequency().put(song.getCountryId(), album.getCountryFrequency().get(song.getCountryId()) - 1);

            if (album.getCountryFrequency().get(song.getCountryId()) == 0) {
                album.getCountryFrequency().remove(song.getCountryId());
            }

            if (song.getCountryId().equals(album.getMainCountry())) {
                album.setMainCountry("");

                for (String countryId : album.getCountryFrequency().keySet()) {
                    if (album.getCountryFrequency().get(countryId) > album.getCountryFrequency().getOrDefault(album.getMainCountry(), 0)) {
                        album.setMainCountry(countryId);
                    }
                }
            }

            for (String tagId : song.getTags()) {
                album.getTagFrequency().put(tagId, album.getTagFrequency().get(tagId) - 1);

                if (album.getTagFrequency().get(tagId) == 0) {
                    album.getTagFrequency().remove(tagId);
                }

                if (tagId.equals(album.getMainTag())) {
                    album.setMainTag("");

                    for (String tagIdEntry : album.getTagFrequency().keySet()) {
                        if (album.getTagFrequency().get(tagIdEntry) > album.getTagFrequency().getOrDefault(album.getMainTag(), 0)) {
                            album.setMainTag(tagIdEntry);
                        }
                    }
                }
            }
        }

        mongoTemplate.save(album);
    }

    @Override
    public void removeSongFromAlbum(AddRemoveSongDTO dto) {
        String albumId = dto.getAlbumId();
        String songId = dto.getSongId();

        Album album = mongoTemplate.findById(albumId, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        if (!album.getSongs().contains(songId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Song doesn't exist in the album");
        }

        Song song = mongoTemplate.findById(songId, Song.class);

        if (song == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist");
        }

        album.getSongs().remove(songId);
        album.getSongAdditionTime().remove(songId);

        album.getCategoryFrequency().put(song.getCategoryId(), album.getCategoryFrequency().get(song.getCategoryId()) - 1);

        if (album.getCategoryFrequency().get(song.getCategoryId()) == 0) {
            album.getCategoryFrequency().remove(song.getCategoryId());
        }

        if (song.getCategoryId().equals(album.getMainCategory())) {
            album.setMainCategory("");

            for (String categoryId : album.getCategoryFrequency().keySet()) {
                if (album.getCategoryFrequency().get(categoryId) > album.getCategoryFrequency().getOrDefault(album.getMainCategory(), 0)) {
                    album.setMainCategory(categoryId);
                }
            }
        }

        album.getCountryFrequency().put(song.getCountryId(), album.getCountryFrequency().get(song.getCountryId()) - 1);

        if (album.getCountryFrequency().get(song.getCountryId()) == 0) {
            album.getCountryFrequency().remove(song.getCountryId());
        }

        if (song.getCountryId().equals(album.getMainCountry())) {
            album.setMainCountry("");

            for (String countryId : album.getCountryFrequency().keySet()) {
                if (album.getCountryFrequency().get(countryId) > album.getCountryFrequency().getOrDefault(album.getMainCountry(), 0)) {
                    album.setMainCountry(countryId);
                }
            }
        }

        for (String tagId : song.getTags()) {
            album.getTagFrequency().put(tagId, album.getTagFrequency().get(tagId) - 1);

            if (album.getTagFrequency().get(tagId) == 0) {
                album.getTagFrequency().remove(tagId);
            }

            if (tagId.equals(album.getMainTag())) {
                album.setMainTag("");

                for (String tagIdEntry : album.getTagFrequency().keySet()) {
                    if (album.getTagFrequency().get(tagIdEntry) > album.getTagFrequency().getOrDefault(album.getMainTag(), 0)) {
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

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        if (!album.getSongs().contains(changeOrderSongDTO.getSongId())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Song doesn't exist in the album");
        }

        if (changeOrderSongDTO.getOrder() < 0 || changeOrderSongDTO.getOrder() >= album.getSongs().size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid order");
        }

        album.getSongs().remove(changeOrderSongDTO.getSongId());
        album.getSongs().add(changeOrderSongDTO.getOrder(), changeOrderSongDTO.getSongId());

        mongoTemplate.save(album);
    }

    @Override
    public void openAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        album.setStatus(Status.PUBLIC.toString());

        mongoTemplate.save(album);
    }

    @Override
    public void closeAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        if (!album.getOwnerId().equals(AuthenticationUtils.getCurrentUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not the owner of this album");
        }

        album.setStatus(Status.PRIVATE.toString());

        mongoTemplate.save(album);
    }

    @Override
    public void favoriteAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        if (!currentUser.getUserPref().getFavoriteAlbums().contains(id)) {
            currentUser.getUserPref().getFavoriteAlbums().add(id);
        }

        mongoTemplate.save(currentUser);
    }

    @Override
    public List<AlbumDTO> getFavorites(String albumType, int limit) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        List<AlbumDTO> allAlbums = currentUser.getUserPref().getFavoriteAlbums().stream().map(albumId -> modelMapper.map(mongoTemplate.findById(albumId, Album.class), AlbumDTO.class)).toList();

        if(albumType.equals(AlbumType.ARTIST_ALBUM.toString())) {
            allAlbums = allAlbums.stream().filter(album -> album.getAlbumType().equals(AlbumType.ARTIST_ALBUM.toString())).toList();
        } else if(albumType.equals(AlbumType.PLAYLIST.toString())) {
            allAlbums = allAlbums.stream().filter(album -> album.getAlbumType().equals(AlbumType.PLAYLIST.toString())).toList();
        }
        if(limit <= 0) {
            return allAlbums;
        } else {
            return allAlbums.stream().limit(limit).collect(Collectors.toList());
        }
    }

    @Override
    public void unfavoriteAlbum(String id) {
        Album album = mongoTemplate.findById(id, Album.class);

        if (album == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Album doesn't exist");
        }

        User currentUser = AuthenticationUtils.getCurrentUser();

        currentUser.getUserPref().getFavoriteAlbums().remove(id);

        mongoTemplate.save(currentUser);
    }

    @Override
    public List<AlbumDTO> searchByName(String prefix, String albumType, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex(".*" + Pattern.quote(prefix) + ".*", "i"));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));
        if (albumType.equals(AlbumType.ARTIST_ALBUM.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()));
        } else if (albumType.equals(AlbumType.PLAYLIST.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString()));
        } else {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()).orOperator(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString())));
        }

        if (limit <= 0) {
            return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Album.class).stream().limit(limit).map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        }
    }

    @Override
    public List<AlbumDTO> searchByArtist(String artistId, String albumType, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ownerId").is(artistId));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if (albumType.equals(AlbumType.ARTIST_ALBUM.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()));
        } else if (albumType.equals(AlbumType.PLAYLIST.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString()));
        } else {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()).orOperator(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString())));
        }

        if (limit <= 0) {
            return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Album.class).stream().limit(limit).map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        }
    }

    @Override
    public List<AlbumDTO> searchByCategory(String categoryId, String albumType, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mainCategory").is(categoryId));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if (albumType.equals(AlbumType.ARTIST_ALBUM.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()));
        } else if (albumType.equals(AlbumType.PLAYLIST.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString()));
        } else {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()).orOperator(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString())));
        }

        if (limit <= 0) {
            return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Album.class).stream().limit(limit).map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        }
    }

    @Override
    public List<AlbumDTO> searchByCountry(String countryId, String albumType, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mainCountry").is(countryId));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if (albumType.equals(AlbumType.ARTIST_ALBUM.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()));
        } else if (albumType.equals(AlbumType.PLAYLIST.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString()));
        } else {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()).orOperator(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString())));
        }

        if (limit <= 0) {
            return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Album.class).stream().limit(limit).map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        }
    }

    @Override
    public List<AlbumDTO> searchByTag(List<String> tags, String albumType, int limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mainTag").in(tags));
        query.addCriteria(Criteria.where("status").is(Status.PUBLIC.toString()));

        if (albumType.equals(AlbumType.ARTIST_ALBUM.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()));
        } else if (albumType.equals(AlbumType.PLAYLIST.toString())) {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString()));
        } else {
            query.addCriteria(Criteria.where("albumType").is(AlbumType.ARTIST_ALBUM.toString()).orOperator(Criteria.where("albumType").is(AlbumType.PLAYLIST.toString())));
        }

        if (limit <= 0) {
            return mongoTemplate.find(query, Album.class).stream().map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        } else {
            return mongoTemplate.find(query, Album.class).stream().limit(limit).map(album -> modelMapper.map(album, AlbumDTO.class)).toList();
        }
    }

    @Override
    public List<AlbumDTO> getAlbumRecommendations(int limit) {
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

        List<String> topCountries = currentUser.getUserPref().getCountryFrequency().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        Set<AlbumDTO> rawRecommendations = new HashSet<>();

        rawRecommendations.addAll(searchByTag(topTags, AlbumType.NONE.toString(), 0));
        for (String categoryId : topCategories) {
            rawRecommendations.addAll(searchByCategory(categoryId, AlbumType.NONE.toString(), 0));
        }
        for (String countryId : topCountries) {
            rawRecommendations.addAll(searchByCountry(countryId, AlbumType.NONE.toString(), 0));
        }

        if (rawRecommendations.size() < 10) {
            rawRecommendations.addAll(searchByCategory(categoryRepository.findByName("Pop").getId(), AlbumType.NONE.toString(), 0));
        }

        rawRecommendations.removeIf(albumDTO -> albumDTO.getStatus().equals(Status.PRIVATE.toString()));

        if (limit <= 0) {
            return rawRecommendations.stream().toList();
        } else {
            return rawRecommendations.stream().limit(limit).toList();
        }
    }
}
