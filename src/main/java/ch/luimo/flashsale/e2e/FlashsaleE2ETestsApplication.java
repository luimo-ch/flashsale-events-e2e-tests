package ch.luimo.flashsale.e2e;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class FlashsaleE2ETestsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashsaleE2ETestsApplication.class, args);
    }
}
