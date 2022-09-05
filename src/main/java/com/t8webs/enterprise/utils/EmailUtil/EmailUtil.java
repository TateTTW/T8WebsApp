package com.t8webs.enterprise.utils.EmailUtil;

import com.t8webs.enterprise.T8WebsApplication;
import com.t8webs.enterprise.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Component
@Profile("dev")
public class EmailUtil implements IEmailUtil {
    @Autowired
    private JavaMailSender emailSender;

    private static final Properties PROPERTIES;
    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String DOMAIN_NAME = PROPERTIES.getProperty("domainName");
    private static final String FROM_EMAIL = PROPERTIES.getProperty("notificationEmail");
    private static final String TO_EMAIL = PROPERTIES.getProperty("notificationEmail");

    @Override
    public void notifyAccessRequest(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(TO_EMAIL);
        message.setSubject(user.getName() + " is requesting access.");
        message.setText(user.toString() + " is requesting access to " + DOMAIN_NAME);
        emailSender.send(message);
    }
}
