import net.spantree.ratpack.elasticsearch.ElasticsearchIndexingService
import net.spantree.ratpack.elasticsearch.ElasticsearchModule

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {

    bindings {
        add new ElasticsearchModule(new File("./config", "EsConfig.groovy"))

        init { ElasticsearchIndexingService elasticsearchIndexingService ->
            elasticsearchIndexingService.init()
        }

    }

    handlers {
        get {
          render groovyTemplate("index.html", title: "My Ratpack App")
        }

        assets "public"
    }
}
