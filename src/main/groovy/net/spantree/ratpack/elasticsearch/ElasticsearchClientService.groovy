package net.spantree.ratpack.elasticsearch

import com.google.inject.Inject
import org.elasticsearch.client.Client
import org.elasticsearch.client.Requests

import javax.annotation.PreDestroy

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder
import static org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.elasticsearch.node.Node

class ElasticsearchClientService {
    Node node
    Client client
    ElasticsearchConfig elasticsearchConfig

    @Inject
    ElasticsearchClientService(ElasticsearchConfig config) {
        String clusterName = elasticsearchConfig.props.cluster.name
        List<String> hosts = elasticsearchConfig.props.cluster.hosts

        def settings = settingsBuilder()
            .put([
                'cluster.name': clusterName,
                'discovery.zen.ping.unicast.hosts': hosts.join(',')
            ])

        node = nodeBuilder()
            .settings(settings)
            .client(true)
            .node()

        client = node.client()

        //TODO: add proper logging
        println "Node created on cluster ${clusterName} connecting to hosts ${hosts}"

    }

    @PreDestroy
    void destroy() {
        println "Closing client: $client"
        client?.close()

        println "Closing node: $node"
        node?.close()
    }

    boolean indexExists(String indexname) {
        client.admin().indices().exists(Requests.indicesExistsRequest(indexname)).get().exists
    }


}
