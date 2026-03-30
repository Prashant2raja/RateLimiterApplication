package com.demo.controller;

import com.demo.dto.TierAssignRequest;
import com.demo.model.Tier;
import com.demo.service.TierService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tier")
public class TierController {

    private final TierService tierService;

    public TierController(TierService tierService) {
        this.tierService = tierService;
    }

    @PostMapping("/assign")
    public String assignTier(@RequestBody TierAssignRequest request) {
        tierService.assignTier(
                request.getIdentifier(),
                request.getTier(),
                request.getExpiresAtEpochSeconds()
        );
        return "Tier assigned successfully";
    }

    @GetMapping("/{identifier}")
    public Tier getTier(@PathVariable String identifier) {
        return tierService.getTier(identifier);
    }
}
