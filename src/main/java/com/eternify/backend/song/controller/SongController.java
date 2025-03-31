package com.eternify.backend.song.controller;

import com.eternify.backend.common.api.ApiResponse;
import com.eternify.backend.song.dto.AlbumDTO;
import com.eternify.backend.song.dto.SongAddDTO;
import com.eternify.backend.song.dto.SongDTO;
import com.eternify.backend.song.dto.SongEditDTO;
import com.eternify.backend.song.service.SongService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "song")
@RequestMapping("/api/song/v1")
public class SongController {
    @Value("${api.token}")
    private String apiToken;

    private final SongService songService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addSong(@RequestHeader("X-auth-token") String token,
                                                   @RequestBody SongAddDTO songAddDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.addSong(songAddDTO);
            return ResponseEntity.ok(ApiResponse.success("Add song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editSong(@RequestHeader("X-auth-token") String token,
                                                @RequestBody SongEditDTO songEditDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.editSong(songEditDTO);
            return ResponseEntity.ok(ApiResponse.success("Edit song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteSong(@RequestHeader("X-auth-token") String token,
                                                  @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.deleteSong(id);
            return ResponseEntity.ok(ApiResponse.success("Delete song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getSong(@RequestHeader("X-auth-token") String token,
                                               @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get song success", songService.getSong(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/open")
    public ResponseEntity<ApiResponse> openSong(@RequestHeader("X-auth-token") String token,
                                                @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.openSong(id);
            return ResponseEntity.ok(ApiResponse.success("Open song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/close")
    public ResponseEntity<ApiResponse> closeSong(@RequestHeader("X-auth-token") String token,
                                                 @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.closeSong(id);
            return ResponseEntity.ok(ApiResponse.success("Close song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/favorite")
    public ResponseEntity<ApiResponse> favoriteSong(@RequestHeader("X-auth-token") String token,
                                                    @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.favoriteSong(id);
            return ResponseEntity.ok(ApiResponse.success("Favorite song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/unfavorite")
    public ResponseEntity<ApiResponse> unfavoriteSong(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.unfavoriteSong(id);
            return ResponseEntity.ok(ApiResponse.success("Unfavorite song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchSongByName(@RequestHeader("X-auth-token") String token,
                                                        @RequestParam String name,
                                                        @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search song by name success", songService.searchByName(name, limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_artist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchSongByArtist(@RequestHeader("X-auth-token") String token,
                                                          @RequestParam String artist,
                                                          @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search song by artist success", songService.searchByArtist(artist, limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_category")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchSongByCategory(@RequestHeader("X-auth-token") String token,
                                                            @RequestParam String category,
                                                            @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search song by category success", songService.searchByCategory(category, limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_country")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchSongByCountry(@RequestHeader("X-auth-token") String token,
                                                           @RequestParam String country,
                                                           @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search song by country success", songService.searchByCountry(country, limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_tag")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchSongByTag(@RequestHeader("X-auth-token") String token,
                                                       @RequestParam List<String> tags,
                                                       @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search song by tag success", songService.searchByTag(tags, limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_user_recommendations")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getUserRecommendations(@RequestHeader("X-auth-token") String token,
                                                              @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get user recommendations success", songService.getUserRecommendations(limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_album_recommendations")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAlbumRecommendations(@RequestHeader("X-auth-token") String token,
                                                               @RequestParam String albumId,
                                                               @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get album recommendations success", songService.getAlbumRecommendations(albumId, limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_user_history")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getUserHistory(@RequestHeader("X-auth-token") String token,
                                                      @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get user history success", songService.getUserHistory(limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_user_favorites")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = SongDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getUserFavorites(@RequestHeader("X-auth-token") String token,
                                                        @RequestParam(defaultValue = "0") int limit) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get user favorites success", songService.getFavorites(limit)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/listen")
    public ResponseEntity<ApiResponse> listenSong(@RequestHeader("X-auth-token") String token,
                                                  @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.songListened(id);
            return ResponseEntity.ok(ApiResponse.success("Listen song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/update_favourite_artist")
    public ResponseEntity<ApiResponse> updateFavouriteArtistForRecommendations(@RequestHeader("X-auth-token") String token,
                                                                              @RequestBody List<String> artistIds) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            songService.updateFavouriteArtistForRecommendations(artistIds);
            return ResponseEntity.ok(ApiResponse.success("Update favourite artist for recommendations success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
