package com.eternify.backend.song.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddRemoveSongBatchDTO {
    String albumId;
    List<String> songs;
}
