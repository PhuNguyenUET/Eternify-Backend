package com.eternify.backend.song.dto;

import com.eternify.backend.song.model.Song;
import com.eternify.backend.song.model.Status;
import com.eternify.backend.user.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AlbumDTO {
    private String id;

    private String name;
    private String description;
    private User owner;
    private List<SongAlbumDTO> songs;
    private String coverPath;
    private String status;
    private String albumType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date createdDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date modifiedDate;
}
