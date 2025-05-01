package com.eternify.backend.song.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

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
    @Builder.Default
    private List<String> songs = new ArrayList<>();
    @Builder.Default
    private Map<String, Date> songAdditionTime = new HashMap<>();
    private String persistentCoverId;
    private String albumType;
    private String status;

    @Builder.Default
    private Map<String, Integer> categoryFrequency = new HashMap<>();
    @Builder.Default
    private Map<String, Integer> countryFrequency = new HashMap<>();
    @Builder.Default
    private Map<String, Integer> tagFrequency = new HashMap<>();

    private String mainCategory = "";
    private String mainCountry = "";
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
