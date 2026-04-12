package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;
    // Sécurité pour éviter les lancements multiples
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/start")
    public ResponseEntity<String> startSync() {
        if (isSyncing.get()) {
            return ResponseEntity.badRequest().body("⚠️ Une synchronisation est déjà en cours !");
        }

        // Lancement en arrière-plan
        new Thread(() -> {
            try {
                isSyncing.set(true);
                syncService.fullReindex();
            } finally {
                isSyncing.set(false);
            }
        }).start();

        return ResponseEntity.ok("🚀 [CIRT-ANTIC] Synchronisation massive lancée ! " +
                "Vérifie la console de ton terminal pour suivre la progression.");
    }
}