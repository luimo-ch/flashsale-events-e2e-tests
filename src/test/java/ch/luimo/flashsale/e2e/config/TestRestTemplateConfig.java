package ch.luimo.flashsale.e2e.config;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRestTemplateConfig {
    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate();
    }
}
