package com.mannaregen.controller;

import com.mannaregen.model.ActionRequest;
import com.mannaregen.model.ActionResponse;
import com.mannaregen.service.CalculationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GameController {

    private final CalculationService calculationService;

    public GameController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping("/action")
    public ActionResponse performAction(@RequestBody ActionRequest request) {
        return calculationService.performAction(request);
    }
}
