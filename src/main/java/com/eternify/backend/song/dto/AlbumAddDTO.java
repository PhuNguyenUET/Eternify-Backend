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
public class AlbumAddDTO {
    private String name;
    private String description;
    private String persistentCoverId;
    private String status;
    private String albumType;
    private List<AddRemoveSongDTO> songs;
}
