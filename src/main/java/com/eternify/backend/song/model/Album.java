package com.eternify.backend.song.model;

import com.eternify.backend.user.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("album")
public class Album {
    @Id
    private String id;

    private String name;
    private String description;
    private String ownerId;
    private List<String> songs;
    private Map<String, Date> songAdditionTime;
    private String coverPath;
    private String albumType;
    private String status;

    private Map<String, Integer> categoryFrequency;
    private Map<String, Integer> tagFrequency;

    private String mainCategory = "";
    private String mainTag = "";

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date createdDate;

    @LastModifiedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private Date modifiedDate;
}
