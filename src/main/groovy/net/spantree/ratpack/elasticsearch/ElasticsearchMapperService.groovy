package net.spantree.ratpack.elasticsearch

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.client.Client
import org.elasticsearch.client.IndicesAdminClient
import org.elasticsearch.client.Requests

@Slf4j
class ElasticsearchMapperService {
    ElasticsearchClientService elasticsearchClientService
    ElasticsearchConfig elasticsearchConfig

    @Inject
    ElasticsearchMapperService(ElasticsearchClientService elasticsearchClientService, ElasticsearchConfig elasticsearchConfig) {
        this.elasticsearchClientService = elasticsearchClientService
        this.elasticsearchConfig = elasticsearchConfig
    }

    Client getClient() {
        elasticsearchClientService.client
    }

    IndicesAdminClient getIndices() {
        client.admin().indices()
    }

    boolean createIndex(String indexname) {
        log.info("Creating index $indexname")
        def settings = elasticsearchConfig.props.indexSettings
        CreateIndexRequest req = Requests.createIndexRequest(indexname)
            .settings([analysis: settings])
        indices.create(req).get().acknowledged
    }

    boolean updateIndex(String indexname) {
        assert indices.prepareClose(indexname).execute().get().acknowledged
        def settings = elasticsearchConfig.props.indexSettings
        assert indices.prepareUpdateSettings(indexname)
            .setSettings([analysis: settings])
            .execute().get().acknowledged

        indices.prepareOpen(indexname).execute().get().acknowledged
    }

    ActionResponse deleteIndex(String indexname) {
        if(elasticsearchClientService.indexExists(indexname)) {
            indices.prepareDelete(indexname).setListenerThreaded(false).execute().actionGet()
        }
    }

    ActionResponse refreshIndex(String indexname) {
        indices.prepareRefresh(indexname).execute().actionGet()
    }

    Closure<Boolean> putTypeMapping = { String indexName, String type, Class<? extends ElasticsearchMappingFieldSet> fieldSetClass ->
        def fieldSet = fieldSetClass.newInstance()
        def req = Requests.putMappingRequest(indexName)
            .type(type)
            .source(fieldSet.source)

        indices.putMapping(req).get().acknowledged
    }

    void initializeIndexMappings(String indexname, Map typeMapping) {
        if(!elasticsearchClientService.indexExists(indexname)) {
            createIndex(indexname)
        } else {
            updateIndex(indexname)
        }
        typeMapping.each putTypeMapping.curry(indexname)
    }



}
