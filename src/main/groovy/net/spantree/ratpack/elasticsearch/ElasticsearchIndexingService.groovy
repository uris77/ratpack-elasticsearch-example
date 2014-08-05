package net.spantree.ratpack.elasticsearch

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.elasticsearch.action.index.IndexRequestBuilder

import javax.annotation.PostConstruct

@Slf4j
class ElasticsearchIndexingService {

    private ElasticsearchClientService elasticsearchClientService
    private ElasticsearchMapperService elasticsearchMapperService
    private Map typeMapping

    Map columnMappings = [
        first_name: 'first_name',
        last_name: 'last_name',
        email: 'email',
        gigya_account_uid: 'gigya_account_uid',
        password_hash: 'password_hash',
        password_algorithm: 'password_algorithm',
        password_salt: 'password_salt',
        password_rounds: 'password_rounds',
        email_verified: 'email_verified'
    ]

    def columnDelimiters = [
        category: ';'
    ]

    String indexName = 'users_sample'

    @Inject
    ElasticsearchIndexingService(ElasticsearchClientService elasticsearchClientService, ElasticsearchMapperService elasticsearchMapperService) {
        this.elasticsearchClientService = elasticsearchClientService
        this.elasticsearchMapperService = elasticsearchMapperService
    }

    @PostConstruct
    void init() {
        elasticsearchMapperService.initializeIndexMappings(indexName, [user: SampleUserFieldSet])
    }

    Map pruneNulls(Map map) {
        Map newMap = [:]
        map.each { k, v ->
            if(v instanceof Map) {
                newMap[k] = pruneNulls(v)
            } else if(v instanceof String && v && !v.isAllWhitespace() && v != '0') {
                newMap[k] = v
            } else if(v instanceof List) {
                newMap[k] = v.findAll { v != '0' }
            } else if(!(v instanceof String)) {
                newMap[k] = v
            }
        }
        newMap
    }

    Map mapColumns(Map mapping, Map row) {
        def out = [:]
        mapping.each { k, v ->
            if(v instanceof Map) {
                out[k] = mapColumns(v, row)
            } else if(columnDelimiters[k]) {
                out[k] = row[v]?.tokenize(columnDelimiters[k]).collect { it.trim() }
            } else {
                out[k] = row[v]
            }
        }
        out
    }

    private Map getRowSource(Map row, Map mappings){
        mapColumns(mappings, row)
    }


//    void indexUsers(clearIndex = true){
//        if(clearIndex) {
//            elasticsearchMapperService.deleteIndex(indexName)
//        }
//        elasticsearchMapperService.initializeIndexMappings(indexName,  typeMapping)
//
//        int rowNumber = 0
//        int totalUsers = totalUsers()
//        int resultMaxSize = 20
//        def totalPages = totalPages(totalUsers, resultMaxSize)
//        for(page in 1..totalPages){
//            def users = userSearchDaoService.list([page: page, limit: resultMaxSize])
//            for(user in users){
//                indexRow(user, rowNumber)
//                rowNumber++
//            }
//        }
//    }
//
//    Integer totalUsers(){
//        userSearchDaoService.total()
//    }

//    def totalPages(int totalRows, int resultMaxSize){
//        Math.ceil(totalRows/resultMaxSize)
//    }

    void indexRow(user, rowNumber){
        def client = elasticsearchClientService.client
        def source = getRowSource(user, columnMappings)
        IndexRequestBuilder indexBuilder = client.prepareIndex(indexName, 'users', "user_${rowNumber}")
        log.info "Indexing user ${user}: ${source.first_name}/${source.last_name}/${source.email}"
        indexBuilder.setSource(source)
            .execute()
            .actionGet()
    }
}
