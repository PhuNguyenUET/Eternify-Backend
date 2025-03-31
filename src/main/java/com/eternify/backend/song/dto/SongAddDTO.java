package com.eternify.backend.song.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SongAddDTO {
    private String title;
    private String persistentCoverId;
    private String persistentSongId;
    private String status;

    private String categoryId;
    private String countryId;
    private List<String> tags;
}
