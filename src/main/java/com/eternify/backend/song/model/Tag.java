package com.eternify.backend.song.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Builder
@NoArgsConstructor
@Document("tag")
public class Tag {
}
