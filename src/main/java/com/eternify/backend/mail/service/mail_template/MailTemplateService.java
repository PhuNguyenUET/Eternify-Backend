package com.eternify.backend.mail.service.mail_template;

import com.eternify.backend.mail.model.MailType;

public interface MailTemplateService {
    void loadTemplate(MailType mailType, String fileName);

    String getTemplate(MailType mailType);
}
