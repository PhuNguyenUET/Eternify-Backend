package com.eternify.backend.song.controller;

import com.eternify.backend.common.api.ApiResponse;
import com.eternify.backend.song.dto.SongDTO;
import com.eternify.backend.song.model.Country;
import com.eternify.backend.song.service.CountryService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "country")
@RequestMapping("/api/country/v1")
public class CountryController {
    @Value("${api.token}")
    private String apiToken;

    private final CountryService countryService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCountry(@RequestHeader("X-auth-token") String token,
                                                   @RequestBody String name) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            countryService.addCountry(name);
            return ResponseEntity.ok(ApiResponse.success("Add country success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteCountry(@RequestHeader("X-auth-token") String token,
                                                      @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            countryService.deleteCountry(id);
            return ResponseEntity.ok(ApiResponse.success("Delete country success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Country.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getCountryById(@RequestHeader("X-auth-token") String token,
                                                       @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get country by id success", countryService.getCountryById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Country.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getCountryByName(@RequestHeader("X-auth-token") String token,
                                                         @RequestParam String name) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get country by name success", countryService.getCountryByName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/add_favourite")
    public ResponseEntity<ApiResponse> addFavouriteCategories(@RequestBody List<String> countryIds) {
        try {
            countryService.addFavouriteCountry(countryIds);
            return ResponseEntity.ok(ApiResponse.success("Add favourite categories success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_all")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Country.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getAllCountries(@RequestHeader("X-auth-token") String token) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get all countries success", countryService.getAllCountries()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
