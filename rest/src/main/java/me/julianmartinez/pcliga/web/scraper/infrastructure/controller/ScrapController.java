package me.julianmartinez.pcliga.web.scraper.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.julianmartinez.pcliga.web.scraper.application.SaveClubService;
import me.julianmartinez.pcliga.web.scraper.application.SaveDivisionService;
import me.julianmartinez.pcliga.web.scraper.domain.model.SeedResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScrapController {

    private final SaveDivisionService saveDivisionService;
    private final SaveClubService saveClubService;

    @PostMapping(value = "/divisions")
    public ResponseEntity<SeedResult> seedDivisions() {
        final SeedResult result = this.saveDivisionService.saveCountriesAndDivisions().block();
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/divisions/clubs")
    public ResponseEntity<Void> updateClubs() {
        this.saveClubService.updateAllClubData()
            .doOnSubscribe(s -> log.info("Actualización de clubes iniciada"))
            .doOnComplete(() -> log.info("Actualización de clubes completada"))
            .doOnError(e -> log.error("Actualización de clubes fallida: {}", e.getMessage()))
            .subscribe();

        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/divisions/{division}/clubs")
    public ResponseEntity<Void> updateClubsByCategory(@PathVariable final Integer division) {
        this.saveClubService.updateClubDataByCategory(division)
            .doOnSubscribe(s -> log.info("Actualización de clubes categoría {} iniciada", division))
            .doOnComplete(() -> log.info("Actualización de clubes categoría {} completada", division))
            .doOnError(e -> log.error("Actualización de clubes categoría {} fallida: {}", division, e.getMessage()))
            .subscribe();

        return ResponseEntity.accepted().build();
    }

}
