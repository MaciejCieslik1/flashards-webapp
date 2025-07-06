package com.PAP_team_21.flashcards.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {
    @Value("${spring.mail.username}")
    private String emailUsername;

    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${spring.mail.host}")
    private String emailHost;

    @Value("${spring.mail.port}")
    private int emailPort;

    @Value("${thread.queue.capacity}")
    private int threadQueueCapacity;

    @Value("${thread.core.pool.size}")
    private int coreThreadPoolSize;

    @Value("${thread.max.pool.size}")
    private int maxThreadPoolSize;

    @Value("${jwt.secret-key}")
    private String secretKeyString;

    @Bean
    public SecretKey secretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);
        mailSender.setHost(emailHost);
        mailSender.setPort(emailPort);


        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS
        props.put("mail.debug", "true");                // extensive logs for debug
        props.put("mail.transport.protocol", "smtp");   // set protocol
        props.put("mail.smtp.auth", "true");            // enable smtp auth


        return mailSender;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor()
    {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setQueueCapacity(threadQueueCapacity);
        e.setThreadNamePrefix("async-thread-");
        e.setCorePoolSize(coreThreadPoolSize);
        e.setMaxPoolSize(maxThreadPoolSize);
        e.initialize();
        return e;
    }
}
