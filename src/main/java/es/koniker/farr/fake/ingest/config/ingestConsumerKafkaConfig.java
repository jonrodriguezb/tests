/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.koniker.farr.fake.ingest.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jrodriguez
 */
@Configuration
@NoArgsConstructor
@Data
public class ingestConsumerKafkaConfig {
    @Value("${kafka.topic}")
    private String topic;
    
    @Value("${kafka.group}")
    private String group;
    
    @Value("${kafka.numThreads}")
    private int numThreads;
    
    @Value("${kafka.zooKeeperConnect}")
    private String zooKeeperConnect;
    
}
