package net.spantree.ratpack.elasticsearch

class ElasticsearchConfig {
    private static ConfigObject props

    public ElasticsearchConfig(File cfg) {
        props = new ConfigSlurper().parse(cfg.text).elasticsearch
    }
}
