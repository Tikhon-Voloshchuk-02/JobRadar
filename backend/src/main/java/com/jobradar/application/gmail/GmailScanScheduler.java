package com.jobradar.application.gmail;

import com.jobradar.application.service.gmail.GmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GmailScanScheduler {
    private final GmailService gmailService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public GmailScanScheduler(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    @Scheduled(fixedDelay = 120000)
    public void scanEmails(){
        if(!running.compareAndSet(false, true)){
            return;
        }
        try{
            gmailService.scanAllActiveConnections();
        } finally{
            running.set(false);
        }
    }
}
