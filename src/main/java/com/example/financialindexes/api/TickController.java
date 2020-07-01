package com.example.financialindexes.api;

import com.example.financialindexes.domain.Tick;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TickController {

    @PostMapping("/ticks")
    @ResponseStatus(HttpStatus.CREATED)
    public void receiveTick(@RequestBody Tick tick) {
    }
}
