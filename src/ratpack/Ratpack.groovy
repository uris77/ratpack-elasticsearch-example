import net.spantree.ratpack.elasticsearch.ElasticsearchModule

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {

    bindings {
        add new ElasticsearchModule(new File("./config", "EsConfig.groovy"))
    }

    handlers {
    get {
      render groovyTemplate("index.html", title: "My Ratpack App")
    }

    assets "public"
    }
}
