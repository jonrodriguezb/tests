/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.koniker.farr.fake.ingest.service;

import lombok.NoArgsConstructor;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jrodriguez
 */
@Service
@NoArgsConstructor
public class ElasticService {
    
    @Autowired
    private Client client;
    
    public void saveToelastic(String index, String type, String json) {
        client.prepareIndex(index, type).setSource(json).get();
    }
    
    
}
