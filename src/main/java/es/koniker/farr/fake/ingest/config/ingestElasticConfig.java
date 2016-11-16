/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.koniker.farr.fake.ingest.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jrodriguez
 */
@Configuration
@Data
@Slf4j
public class ingestElasticConfig {
    @Value("${ingest.db.elastic.host}")
    private String host;

    @Value("${ingest.db.elastic.port}")
    private int port;

    @Value("${ingest.db.elastic.index}")
    private String index;
    
    @Value("${ingest.db.elastic.type}")
    private String type;
    
    @Bean
    public Client client() throws UnknownHostException {
        log.info("Attemping to connect Elasticsearch in host {} and port {}", host, port);
        return TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }

}
