package net.spantree.ratpack.elasticsearch

class ElasticsearchMappingManager {
    Map source

    /**
     * Creates a navigator around an empty Elasticsearch Mapping source
     */
    ElasticsearchMappingManager() {
        this.source = [:]
    }

    /**
     * Creates a navigator pre-instantiated with the provided type
     *
     * @param type The name of the type to instantiate
     */
    ElasticsearchMappingManager(String type) {
        this()
        source[type] = [properties: [:]]
    }

    /**
     * Creates a navigator around an existing mapping source
     *
     * @param mappingSource An existing root mapping element, e.g. `[properties: [message: [type: "string", store: true]]]`
     */
    ElasticsearchMappingManager(Map mappingSource) {
        this.source = mappingSource
    }

    /**
     * Navigates the mapping tree looking for a field mapping, instantiating an empty
     * field mapping if one does not already exist.
     *
     * @param type The name of the type mapping to use
     * @param fieldName The dot-notated path to the field, e.g. `person.name.first_name`
     * @return The existing or newly-instantiated mapping for the field
     */
    Map navigateToField(String type, String fieldName) {
        String[] nodeNames = fieldName.split(/\./)
        // try to find an existing mapping for the given type
        Map typeMapping = source[type]
        // if not mapping is found, create an empty one
        if(type == null) {
            source[type] = [properties: [:]]
        }
        // start with the type's root object node as the source
        Map props = source[type]['properties']

        int i = 0
        for(String nodeName : nodeNames) {
            // try to find an existing node
            Map node = props[nodeName]
            // if an existing node doesn't exist, set it to an empty map
            if(node == null) {
                node = [:]
                props[nodeName] = node
            }
            // if we're not yet at the leaf node
            if(nodeNames.size() > ++i) {
                // try to find the properties node for this branch
                props = node['properties']
                // if a properties node doesn't yet exist
                if(props == null) {
                    // set the current properties node to an empty map
                    props = [:]
                    // mark the mapping as an object type
                    node['type'] = 'object'
                    // set the properties node for this branch to that same empty map
                    node['properties'] = props
                }
                // if we're at the leaf node
            } else {
                // exit the loop and return the leaf node
                return props[nodeName]
            }
        }
    }

    /**
     * Adds or updates a field mapping with the provided values
     *
     * @param type The name of the type mapping to use
     * @param fieldName The dot-notated path to the field, e.g. `person.name.first_name`
     * @param newMapping The mapping to add or overlay, e.g. `[type: 'string', store: false]`
     * @return The added or updated mapping for the specified field
     */
    Map putMapping(String type, String fieldName, Map mappingOverlay) {
        Map fieldMapping = navigateToField(type, fieldName)
        fieldMapping.putAll(mappingOverlay)
        return fieldMapping
    }

    Map addComplexFieldMapping(String type, String mappingType, String fieldName, Class<? extends ElasticsearchMappingFieldSet> fieldSetClass) {
        ElasticsearchMappingFieldSet fieldSet = fieldSetClass.newInstance()
        def overlay = [type: mappingType, properties: fieldSet.propertiesMap]
        if(mappingType == 'nested') {
            overlay['include_in_parent'] = true
            overlay['include_in_root'] = true
        }
        putMapping(type, fieldName, overlay)
    }
}
