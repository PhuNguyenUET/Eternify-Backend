package com.eternify.backend.song.service;

import com.eternify.backend.song.dto.*;

import java.util.List;

public interface AlbumService {
    void createAlbum(AlbumAddDTO albumAddDTO);
    void deleteAlbum(String id);
    void updateAlbum(AlbumEditDTO albumEditDTO);
    AlbumDTO getAlbum(String id);
    void cloneAlbum(String id);

    void addSongToAlbum(AddRemoveSongDTO addRemoveSongDTO);
    void removeSongFromAlbum(AddRemoveSongDTO addRemoveSongDTO);
    void changeSongOrder(ChangeOrderSongDTO changeOrderSongDTO);

    void openAlbum(String id);
    void closeAlbum(String id);

    void favoriteAlbum(String id);
    void unfavoriteAlbum(String id);

    List<AlbumDTO> searchByName(String prefix);
    List<AlbumDTO> searchByArtist(String artistId);
    List<AlbumDTO> searchByCategory(String categoryId);
    List<AlbumDTO> searchByTag(List<String> tags);

    List<AlbumDTO> getAlbumRecommendations();
}
