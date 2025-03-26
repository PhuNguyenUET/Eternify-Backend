package com.eternify.backend.song.dto;

import com.eternify.backend.song.model.AlbumType;
import com.eternify.backend.song.model.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumAddDTO {
    private String name;
    private String description;
    private String coverPath;
    private String status;
    private String albumType;
}
