package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.service.SyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/start")
    public String startSync() {
        // On lance dans un nouveau thread pour ne pas bloquer le navigateur
        new Thread(syncService::fullReindex).start();
        return "🚀 Synchronisation lancée en arrière-plan ! Surveille la console de ton IDE.";
    }
}