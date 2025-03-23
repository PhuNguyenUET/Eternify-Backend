package com.eternify.backend.song.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeOrderSongDTO {
    String albumId;
    String songId;
    int order;
}
