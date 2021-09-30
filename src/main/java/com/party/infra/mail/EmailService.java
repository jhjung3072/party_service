package com.party.infra.mail;

import org.springframework.scheduling.annotation.Async;


public interface EmailService {

    @Async
    void sendEmail(EmailMessage emailMessage);
}
