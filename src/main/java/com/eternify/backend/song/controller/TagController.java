package com.eternify.backend.song.controller;

import com.eternify.backend.common.api.ApiResponse;
import com.eternify.backend.song.service.TagService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "tag")
@RequestMapping("/api/tag/v1")
public class TagController {
    @Value("${api.token}")
    private String apiToken;

    private final TagService tagService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addTag(@RequestHeader("X-auth-token") String token,
                                              @RequestBody String name) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            tagService.addTag(name);
            return ResponseEntity.ok(ApiResponse.success("Add tag success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteTag(@RequestHeader("X-auth-token") String token,
                                                 @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            tagService.deleteTag(id);
            return ResponseEntity.ok(ApiResponse.success("Delete tag success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = com.eternify.backend.song.model.Tag.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getTagById(@RequestHeader("X-auth-token") String token,
                                                 @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get tag by id success", tagService.getTagById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = com.eternify.backend.song.model.Tag.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getTagByName(@RequestHeader("X-auth-token") String token,
                                                 @RequestParam String name) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get tag by name success", tagService.getTagByName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_all")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = com.eternify.backend.song.model.Tag.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllTags(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get all tags success", tagService.getAllTags()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
