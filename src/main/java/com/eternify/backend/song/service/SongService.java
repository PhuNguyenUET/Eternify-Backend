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

    List<SongDTO> searchByName(String prefix);
    List<SongDTO> searchByArtist(String artistId);
    List<SongDTO> searchByCategory(String categoryId);
    List<SongDTO> searchByTag(List<String> tags);

    List<SongDTO> getUserRecommendations();
    List<SongDTO> getAlbumRecommendations(String albumId);
    List<SongDTO> getUserHistory();
    List<SongDTO> getFavorites();

    void songListened(String id);
}
