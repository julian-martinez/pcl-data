package me.julianmartinez.pcliga.web.scraper.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import me.julianmartinez.pcliga.web.scraper.application.LoanService;
import me.julianmartinez.pcliga.web.scraper.domain.model.DivisionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping(value = "loans/categories/{category}")
    public ResponseEntity<List<DivisionDto>> getLoanDestinationsByCategory(@PathVariable final Integer category) {
        return ResponseEntity.ok(this.loanService.getLoanDestinationsByCategory(category));
    }

}
