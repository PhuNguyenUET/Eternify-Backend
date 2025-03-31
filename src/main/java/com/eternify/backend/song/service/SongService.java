package com.eternify.backend.song.service;

import com.eternify.backend.song.dto.SongAddDTO;
import com.eternify.backend.song.dto.SongDTO;
import com.eternify.backend.song.dto.SongEditDTO;

import java.util.List;

public interface SongService {
    void addSong(SongAddDTO songAddDTO);
    void editSong(SongEditDTO songEditDTO);
    void deleteSong(String id);
    SongDTO getSong(String id);

    void openSong(String id);
    void closeSong(String id);

    void favoriteSong(String id);
    void unfavoriteSong(String id);

    List<SongDTO> searchByName(String prefix, int limit);
    List<SongDTO> searchByArtist(String artistId, int limit);
    List<SongDTO> searchByCategory(String categoryId, int limit);
    List<SongDTO> searchByCountry(String countryId, int limit);
    List<SongDTO> searchByTag(List<String> tags, int limit);

    List<SongDTO> getUserRecommendations(int limit);
    List<SongDTO> getAlbumRecommendations(String albumId, int limit);
    List<SongDTO> getUserHistory(int limit);
    List<SongDTO> getFavorites(int limit);

    void songListened(String id);
    void updateFavouriteArtistForRecommendations(List<String> artistIds);
}
