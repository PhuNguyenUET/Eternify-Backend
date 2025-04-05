package com.eternify.backend.song.dto;

import com.eternify.backend.song.model.Category;
import com.eternify.backend.song.model.Country;
import com.eternify.backend.song.model.Tag;
import com.eternify.backend.user.model.User;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongAlbumDTO {
    private String id;

    private String title;
    private User artist;
    private String persistentSongId;
    private int length;

    private Category category;
    private Country country;
    private List<Tag> tags;

    private String persistentCoverId;

    private String status;

    private Date additionTime;
}
