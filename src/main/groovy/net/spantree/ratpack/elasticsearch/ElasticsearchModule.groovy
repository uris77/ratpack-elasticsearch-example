package net.spantree.ratpack.elasticsearch

import com.google.inject.AbstractModule
import com.google.inject.Scopes

class ElasticsearchModule extends AbstractModule {

    private File cfg

    ElasticsearchModule(File cfg) {
        this.cfg = cfg
    }

    @Override
    protected void configure() {
        bind(ElasticsearchConfig.class).toInstance(new ElasticsearchConfig(this.cfg))
        bind(ElasticsearchClientService.class).in(Scopes.SINGLETON)
        bind(ElasticsearchMapperService.class).in(Scopes.SINGLETON)
        bind(ElasticsearchIndexingService).in(Scopes.SINGLETON)
    }

}
