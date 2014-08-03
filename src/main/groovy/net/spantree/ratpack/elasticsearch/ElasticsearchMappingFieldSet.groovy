package net.spantree.ratpack.elasticsearch

abstract class ElasticsearchMappingFieldSet {
    abstract String getType()

    Set<String> idFields = []
    Set<String> textFields = []
    Set<String> facetedTextFields = []
    Set<String> dateFields = []
    Set<String> integerFields = []
    Set<String> floatFields = []
    Set<String> phoneNumberFields = []
    Set<String> geopointFields = []
    Set<String> emailFields = []
    Set<String> booleanFields = []
    Map<String, Map<String, String>> customFields = [:]
    Map<String, Class<? extends ElasticsearchMappingFieldSet>> embeddedFields = [:]
    Map<String, Class<? extends ElasticsearchMappingFieldSet>> nestedFields = [:]
    Map<String, Closure> syntheticFields = [:]

    /**
     * Builds and returns a mapping source as map from the fields
     *
     * @return Mapping source as map
     */
    Map getSource() {
        def manager = new ElasticsearchMappingManager(type)

        textFields.addAll(facetedTextFields)

        getIdFields().each { String fieldName ->
            manager.putMapping(type, fieldName, [type: 'string', index: 'not_analyzed', include_in_all: false])
        }

        getTextFields().each { String fieldName ->
            def fields = [:]
            fields[fieldName] = [type: 'string', index: 'analyzed', include_in_all: 'true']
            fields['edge'] = [type: 'string', index_analyzer: 'edge_left', search_analyzer: 'standard', index: 'analyzed', include_in_all: true]
            fields['alphanum'] = [type: 'string', index_analyzer: 'alpha_space_only', search_analyzer: 'alpha_space_only', index: 'analyzed', include_in_all: true]
            fields['stemmed'] = [type: 'string', index_analyzer: 'stemmed', search_analyzer: 'stemmed', index: 'analyzed']
            fields['word_delimited'] = [type: 'string', index_analyzer: 'word_delimited', search_analyzer: 'standard', index: 'analyzed']
//            fields['synonym'] = [type: 'string', index_analyzer: 'all_synonyms', search_analyzer: 'all_synonyms', index: 'analyzed', include_in_all: true]
            if (fieldName in facetedTextFields) {
                fields['facet'] = [type: 'string', index: 'not_analyzed', include_in_all: false]
            }
            fields['sort'] = [type: 'string', analyzer: 'sorted', include_in_all: false]
            def mapping = [type: 'multi_field', fields: fields]
            manager.putMapping(type, fieldName, mapping)
        }

        getPhoneNumberFields().each { String fieldName ->
            def fields = [:]
            fields[fieldName] = [type: 'string', analyzer: 'phone_number_digits', include_in_all: true]
            fields['edge'] = [type: 'string', index_analyzer: 'phone_number_edge', include_in_all: false]
            def mapping = [type: 'multi_field', fields: fields]
            manager.putMapping(type, fieldName, mapping)
        }

        getEmailFields().each { String fieldName ->
            def fields = [:]
            fields[fieldName] = [type: 'string', analyzer: 'email_tokenized', include_in_all: 'true']
            fields['strict'] = [type: 'string', analyzer: 'email_strict', include_in_all: false]
            fields['edge'] = [type: 'string', index_analyzer: 'edge_left', search_analyzer: 'standard', index: 'analyzed', include_in_all: true]
            fields['alphanum'] = [type: 'string', index_analyzer: 'alpha_space_only', search_analyzer: 'alpha_space_only', index: 'analyzed', include_in_all: true]
            fields['sort'] = [type: 'string', analyzer: 'sorted', include_in_all: false]
            def mapping = [type: 'multi_field', fields: fields]
            manager.putMapping(type, fieldName, mapping)
        }

        getIntegerFields().each { String fieldName ->
            manager.putMapping(type, fieldName, [type: 'integer'])
        }

        getFloatFields().each { String fieldName ->
            manager.putMapping(type, fieldName, [type: 'float'])
        }

        getBooleanFields().each { String fieldName ->
            manager.putMapping(type, fieldName, [type: 'boolean'])
        }

        getGeopointFields().each { String fieldName ->
            manager.putMapping(type, fieldName, [type: 'geo_point'])
        }

        getDateFields().each { String fieldName ->
            manager.putMapping(type, fieldName, [type: 'date'])
        }

        getCustomFields().each { String fieldName, Map mapping ->
            manager.putMapping(type, fieldName, mapping)
        }

        getEmbeddedFields().each { String fieldName, Class fieldSetClass ->
            manager.addComplexFieldMapping(type, 'object', fieldName, fieldSetClass)
        }

        getNestedFields().each { String fieldName, Class fieldSetClass ->
            manager.addComplexFieldMapping(type, 'nested', fieldName, fieldSetClass)
        }

        manager.source
    }

    Map getPropertiesMap() {
        getSource()[type]['properties']
    }

}
