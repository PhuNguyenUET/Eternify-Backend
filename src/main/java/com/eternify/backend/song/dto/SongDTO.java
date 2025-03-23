package com.eternify.backend.song.dto;

import com.eternify.backend.song.model.Category;
import com.eternify.backend.song.model.Status;
import com.eternify.backend.song.model.Tag;
import com.eternify.backend.user.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongDTO {
    private String id;

    private String title;
    private User artist;
    private String persistentPathSong;

    private Category category;
    private List<Tag> tags;

    private String coverPath;

    private Status status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date createdDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date modifiedDate;
}
