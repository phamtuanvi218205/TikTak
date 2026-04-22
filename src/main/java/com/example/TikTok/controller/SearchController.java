package com.example.TikTok.controller;

import com.example.TikTok.dto.response.GlobalSearchResponse;
import com.example.TikTok.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    @GetMapping
    public ResponseEntity<GlobalSearchResponse> search(@RequestParam("keyword") String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        GlobalSearchResponse response = searchService.search(keyword.trim());
        return ResponseEntity.ok(response);
    }
}
