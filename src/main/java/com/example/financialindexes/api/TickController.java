package com.example.financialindexes.api;

import com.example.financialindexes.app.TickApplicationService;
import com.example.financialindexes.domain.Tick;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TickController {

    private final TickApplicationService tickService;

    @PostMapping("/ticks")
    public ResponseEntity<Void> receiveTick(@RequestBody Tick tick) {
        var success = tickService.receiveTick(tick);
        return ResponseEntity
                .status(success ? HttpStatus.CREATED : HttpStatus.NO_CONTENT)
                .build();
    }
}
