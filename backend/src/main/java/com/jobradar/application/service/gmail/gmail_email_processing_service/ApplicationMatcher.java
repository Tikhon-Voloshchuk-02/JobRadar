package com.jobradar.application.service.gmail.gmail_email_processing_service;

import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationMatcher {
    private ApplicationRepository applicationRepository;

    public ApplicationMatcher(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public Optional<Application> findMatchingApplication(User user, GmailMessageDto email){
        String text = String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet()),
                safe(email.bodyText())
        ).toLowerCase();

        List<Application> applications = applicationRepository.findByUser(user);

        Optional<Application> companyMatch = applications.stream()
                .filter(application -> matchesCompany(application, text))
                .findFirst();

        if(companyMatch.isPresent()){ return companyMatch; }

        List<Application> positionMatches = applications.stream()
                .filter(application -> {
                    String postion = safe(application.getPosition()).toLowerCase();
                    return !postion.isBlank() && text.contains(postion);
                })
                .toList();

        if (positionMatches.size() == 1) {
            return Optional.of(positionMatches.get(0));
        }

        return Optional.empty();
    }


    private boolean matchesCompany(Application application, String text){
        String company = safe(application.getCompany()).toLowerCase();

        if (company.isBlank()) {
            return false;
        }

        if (text.contains(company)) {
            return true;
        }

        String[] companyWords = company.split("\\s+");

        return Arrays.stream(companyWords)
                .anyMatch(word ->
                        word.length() > 3 && text.contains(word)
                );
    }
}