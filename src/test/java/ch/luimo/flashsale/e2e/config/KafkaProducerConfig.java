package ch.luimo.flashsale.e2e.config;

import ch.luimo.flashsale.e2e.eventservice.avro.AvroFlashSaleEvent;
import ch.luimode.flashsale.AvroPurchaseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@TestConfiguration
public class KafkaProducerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Bean
    public ProducerFactory<String, AvroPurchaseRequest> purchaseRequestProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }
    @Bean
    public KafkaTemplate<String, AvroPurchaseRequest> purchaseRequestKafkaTemplate(ProducerFactory<String, AvroPurchaseRequest> purchaseRequestProducerFactory) {
        return new KafkaTemplate<>(purchaseRequestProducerFactory);
    }

    @Bean
    public ProducerFactory<String, AvroFlashSaleEvent> flashSaleEventProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, AvroFlashSaleEvent> flashSaleEventKafkaTemplate(ProducerFactory<String, AvroFlashSaleEvent> flashSaleEventProducerFactory) {
        return new KafkaTemplate<>(flashSaleEventProducerFactory);
    }

}
