package com.labresource.platform.common.web;

import com.labresource.platform.config.DemoDataSeeder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class DemoSeedController {

    private final DemoDataSeeder demoDataSeeder;

    public DemoSeedController(DemoDataSeeder demoDataSeeder) {
        this.demoDataSeeder = demoDataSeeder;
    }

    @PostMapping("/seed-demo")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> triggerDemoSeed() {
        DemoDataSeeder.DemoDataSummary summary = demoDataSeeder.seedAll();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Realistic Andhra Pradesh Demo Dataset verified and seeded successfully",
                "summary", summary
        ));
    }
}
