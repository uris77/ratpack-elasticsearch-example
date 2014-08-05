package net.spantree.ratpack.elasticsearch

class SampleUserFieldSet extends ElasticsearchMappingFieldSet {
    String type = "user"
    Map<String, Class<? extends ElasticsearchMappingFieldSet>> nested = [:]
}
