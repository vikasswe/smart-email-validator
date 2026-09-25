package com.email.validator.smart_email_validator.config;


import com.email.validator.smart_email_validator.provider.DisposableDomainProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final DisposableDomainProvider disposableDomainProvider;

    public SchedulerConfig (DisposableDomainProvider disposableDomainProvider){
        this.disposableDomainProvider = disposableDomainProvider;
    }

    // ✅ Runs daily at 03:00 UTC
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    public void reloadDisposableDomains(){
        try {
            System.out.println("Scheduler triggered: Reloading domains...");
            disposableDomainProvider.reload();
        }catch (Exception e){
            System.err.println("Failed to reload domains: " + e.getMessage());
        }
    }
}