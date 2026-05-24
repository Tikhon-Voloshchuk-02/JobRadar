package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class ApplicationMatcher {
    private ApplicationRepository applicationRepository;

    public ApplicationMatcher(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    private String safe(String value){
        return value == null ? "" : value;
    }

    public Optional<Application> findMatchingApplication(User user, GmailMessageDto email){
        String text = String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet()),
                safe(email.bodyText())
        ).toLowerCase();

        return applicationRepository.findByUser(user).stream()
                .filter(application -> {
                    String company = safe(application.getCompany()).toLowerCase();
                    String position = safe(application.getPosition()).toLowerCase();

                    String[] companyWords = company.split("\\s+");

                    boolean companyMatch = Arrays.stream(companyWords)
                            .anyMatch(word ->
                                    word.length() > 3 && text.contains(word)
                            );

                    return companyMatch || text.contains(position);
                })
                .findFirst();
    }
}
