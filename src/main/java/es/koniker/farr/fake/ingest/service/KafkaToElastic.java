/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.koniker.farr.fake.ingest.service;

import es.koniker.farr.fake.ingest.config.ingestConsumerKafkaConfig;
import es.koniker.farr.fake.ingest.config.ingestElasticConfig;
import es.koniker.farr.fake.ingest.service.ElasticService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.Decoder;
import kafka.serializer.DefaultDecoder;
import kafka.utils.VerifiableProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jrodriguez
 */
@Slf4j
public class KafkaToElastic {

    @Autowired
    private ingestConsumerKafkaConfig ingestConsumerKafkaConfig;

    @Autowired
    private ElasticService elasticService;

    private List<StreamConsumer> consumerThreads;
    private ExecutorService executor;
    private ConsumerConnector consumerConnector;

    @PostConstruct
    public void init() {
        Properties config = new Properties();
        config.put("zookeeper.connect", ingestConsumerKafkaConfig.getZooKeeperConnect());
        config.put("group.id", ingestConsumerKafkaConfig.getGroup());

        this.consumerThreads = new Vector<>();
        this.executor = Executors.newFixedThreadPool(ingestConsumerKafkaConfig.getNumThreads());
        this.consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(config));

        log.info("Connecting to topic {} via ZooKeeper {}", ingestConsumerKafkaConfig.getTopic(), ingestConsumerKafkaConfig.getZooKeeperConnect());
        startConsumers();
    }

    private void startConsumers() {
        Map topicCountMap = new HashMap<>();
        topicCountMap.put(ingestConsumerKafkaConfig.getTopic(), ingestConsumerKafkaConfig.getNumThreads());
        Decoder defaultDecoder = new DefaultDecoder(new VerifiableProperties());
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap, defaultDecoder, defaultDecoder);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(ingestConsumerKafkaConfig.getTopic());

        // Objeto para consumir los mensajes
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            StreamConsumer consumerThread = new StreamConsumer(stream, new StreamConsumercontext(threadNumber), elasticService);
            executor.submit(consumerThread);
            consumerThreads.add(consumerThread);
            threadNumber++;
        }

        executor.shutdown();
    }

    private class StreamConsumer implements Runnable {

        @Autowired
        private ingestElasticConfig ingestElasticConfig;

        private KafkaStream stream;
        private StreamConsumercontext context;
        private ElasticService elasticservice;

        public StreamConsumer(KafkaStream stream, StreamConsumercontext streamConsumercontext, ElasticService elasticService) {
            this.stream = stream;
            this.context = streamConsumercontext;
            this.elasticservice = elasticService;
        }

        @Override
        public void run() {
            ConsumerIterator it = stream.iterator();

            while (it.hasNext()) {
                MessageAndMetadata<byte[], byte[]> data = it.next();
                String jsonString = new String(data.message());
                log.info("Received messaged {} by thread:{}, topic:{}, partition:{}, offset:{} ",
                        jsonString,
                        context.getThreadId(),
                        data.topic(),
                        data.partition(),
                        data.offset());

                // saves into elastic
                elasticService.saveToelastic(ingestElasticConfig.getIndex(), ingestElasticConfig.getType(), jsonString);
                log.info("Saved into {}", ingestElasticConfig.getIndex());
            }

            log.debug("Shutting down Kafka consumer Thread:{}" + context.getThreadId());

        }

    }

    @Data
    private class StreamConsumercontext {

        private final int threadId;

        public StreamConsumercontext(int threadNumber) {
            this.threadId = threadNumber;
        }
    }
}
