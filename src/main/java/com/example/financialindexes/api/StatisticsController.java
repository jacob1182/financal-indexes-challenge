package com.example.financialindexes.api;

import com.example.financialindexes.api.dto.StatisticsDto;
import com.example.financialindexes.app.IndexApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

    private final IndexApplicationService applicationService;

    @GetMapping
    public StatisticsDto getStatistics() {
        return StatisticsDto.from(applicationService.getStatistics());
    }
}
