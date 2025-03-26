package com.eternify.backend.song.controller;

import com.eternify.backend.common.api.ApiResponse;
import com.eternify.backend.song.dto.*;
import com.eternify.backend.song.service.AlbumService;
import com.eternify.backend.user.model.User;
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
@Tag(name = "album")
@RequestMapping("/api/album/v1")
public class AlbumController {
    @Value("${api.token}")
    private String apiToken;

    private final AlbumService albumService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createAlbum(@RequestHeader("X-auth-token") String token,
                                               @RequestBody AlbumAddDTO albumAddDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.createAlbum(albumAddDTO);
            return ResponseEntity.ok(ApiResponse.success("Add song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteAlbum(@RequestHeader("X-auth-token") String token,
                                               @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.deleteAlbum(id);
            return ResponseEntity.ok(ApiResponse.success("Delete album success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateAlbum(@RequestHeader("X-auth-token") String token,
                                               @RequestBody AlbumEditDTO albumEditDTO) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.updateAlbum(albumEditDTO);
            return ResponseEntity.ok(ApiResponse.success("Update album success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAlbum(@RequestHeader("X-auth-token") String token,
                                               @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get album success", albumService.getAlbum(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/clone")
    public ResponseEntity<ApiResponse> cloneAlbum(@RequestHeader("X-auth-token") String token,
                                               @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.cloneAlbum(id);
            return ResponseEntity.ok(ApiResponse.success("Clone album success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/add_song")
    public ResponseEntity<ApiResponse> addSongToAlbum(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody AddRemoveSongDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.addSongToAlbum(dto);
            return ResponseEntity.ok(ApiResponse.success("Add song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/remove_song")
    public ResponseEntity<ApiResponse> removeSongFromAlbum(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody AddRemoveSongDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.removeSongFromAlbum(dto);
            return ResponseEntity.ok(ApiResponse.success("Remove song success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/change_song_order")
    public ResponseEntity<ApiResponse> changeSongOrder(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody ChangeOrderSongDTO dto) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.changeSongOrder(dto);
            return ResponseEntity.ok(ApiResponse.success("Change song order success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/open")
    public ResponseEntity<ApiResponse> openAlbum(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.openAlbum(id);
            return ResponseEntity.ok(ApiResponse.success("Open album success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/close")
    public ResponseEntity<ApiResponse> closeAlbum(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.closeAlbum(id);
            return ResponseEntity.ok(ApiResponse.success("Close album success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/favorite")
    public ResponseEntity<ApiResponse> favoriteAlbum(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.favoriteAlbum(id);
            return ResponseEntity.ok(ApiResponse.success("Favorite album success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/unfavorite")
    public ResponseEntity<ApiResponse> unfavoriteAlbum(@RequestHeader("X-auth-token") String token,
                                                      @RequestBody String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            albumService.unfavoriteAlbum(id);
            return ResponseEntity.ok(ApiResponse.success("Unfavorite album success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchByName(@RequestHeader("X-auth-token") String token,
                                                      @RequestParam String prefix, @RequestParam String albumType) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search by name success", albumService.searchByName(prefix, albumType)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_artist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchByArtist(@RequestHeader("X-auth-token") String token,
                                                      @RequestParam String artistId, @RequestParam String albumType) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search by artist success", albumService.searchByArtist(artistId, albumType)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_category")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchByCategory(@RequestHeader("X-auth-token") String token,
                                                      @RequestParam String categoryId, @RequestParam String albumType) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search by category success", albumService.searchByCategory(categoryId, albumType)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/search_by_tag")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> searchByTag(@RequestHeader("X-auth-token") String token,
                                                      @RequestParam List<String> tags, @RequestParam String albumType) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Search by tag success", albumService.searchByTag(tags, albumType)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_recommendations")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AlbumDTO.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAlbumRecommendations(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get album recommendations success", albumService.getAlbumRecommendations()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
