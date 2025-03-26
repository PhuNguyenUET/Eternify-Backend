package com.eternify.backend.song.dto;

import com.eternify.backend.song.model.Status;
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
    private String coverPath;
    private String persistentPathSong;
    private String status;

    private String categoryId;
    private List<String> tags;
}
