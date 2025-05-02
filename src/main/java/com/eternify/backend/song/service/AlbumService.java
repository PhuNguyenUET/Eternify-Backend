package com.eternify.backend.song.service;

import com.eternify.backend.song.dto.*;
import com.eternify.backend.song.model.AlbumType;

import java.util.List;

public interface AlbumService {
    void createAlbum(AlbumAddDTO albumAddDTO);
    void deleteAlbum(String id);
    void updateAlbum(AlbumEditDTO albumEditDTO);
    AlbumDTO getAlbum(String id);
    void cloneAlbum(String id);

    void addSongToAlbum(AddRemoveSongDTO addRemoveSongDTO);
    void addSongBatchToAlbum(AddRemoveSongBatchDTO addRemoveSongBatchDTO);
    void removeSongBatchFromAlbum(AddRemoveSongBatchDTO addRemoveSongBatchDTO);
    void removeSongFromAlbum(AddRemoveSongDTO addRemoveSongDTO);
    void changeSongOrder(ChangeOrderSongDTO changeOrderSongDTO);

    void openAlbum(String id);
    void closeAlbum(String id);

    void favoriteAlbum(String id);
    void unfavoriteAlbum(String id);

    List<AlbumDTO> getFavorites(int limit);

    List<AlbumDTO> searchByName(String prefix, String albumType, int limit);
    List<AlbumDTO> searchByArtist(String artistId, String albumType, int limit);
    List<AlbumDTO> searchByCategory(String categoryId, String albumType, int limit);
    List<AlbumDTO> searchByCountry(String countryId, String albumType, int limit);
    List<AlbumDTO> searchByTag(List<String> tags, String albumType, int limit);

    List<AlbumDTO> getAlbumRecommendations(int limit);
}
