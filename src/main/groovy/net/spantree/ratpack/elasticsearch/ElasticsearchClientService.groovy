package net.spantree.ratpack.elasticsearch

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.elasticsearch.client.Requests
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import javax.annotation.PreDestroy
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder

@Slf4j
class ElasticsearchClientService {
    TransportClient client
    ElasticsearchConfig elasticsearchConfig

    @Inject
    ElasticsearchClientService(ElasticsearchConfig conf) {
        this.elasticsearchConfig = conf
        String clusterName = elasticsearchConfig.props.cluster.name

        def settings = settingsBuilder().put('cluster.name', clusterName)
        client = new TransportClient(settings)
        client.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300))

        log.info "Client connected to cluster $clusterName"

    }

    @PreDestroy
    void destroy() {
        log.info "Closing client: $client"
        client?.close()
    }

    boolean indexExists(String indexname) {
        client.admin().indices().exists(Requests.indicesExistsRequest(indexname)).get().exists
    }


}
