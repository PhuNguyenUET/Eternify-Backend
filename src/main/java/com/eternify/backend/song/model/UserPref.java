package com.eternify.backend.song.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPref {
    private Map<String, Integer> tagFrequency = new HashMap<>();
    private Map<String, Integer> categoryFrequency = new HashMap<>();

    private List<String> songHistory;
    private List<String> favoriteSongs;
    private List<String> favoriteAlbums;
}
