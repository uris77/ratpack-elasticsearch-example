elasticsearch {
    indexSettings {
        filter {
            alpha_space {
                type = 'pattern_replace'
                pattern = '[^a-zA-Z0-9\\s]'
                replacement = ''
            }
            word_delimited {
                type = 'word_delimiter'
                generate_word_parts = true
                catenate_all = true
                preserve_original = true
                stem_english_possessive = true
            }
            word_delimited_catenate_only {
                type = 'word_delimiter'
                generate_word_parts = false
                catenate_all = true
                preserve_original = true
                stem_english_possessive = false
            }
            edge_left {
                type = 'edgeNGram'
                side = 'front'
                min_gram = 1
                max_gram = 20
            }
            email {
                type = 'pattern_capture'
                preserve_original = 1
                patterns = [
                    "(\\w+)",
                    "(\\p{L}+)",
                    "(\\d+)",
                    "@(.+)"
                ]
            }
            phone_number_replace {
                type = 'pattern_replace'
                pattern = '(^1|\\D+)'
                replace = ''
            }
        }
        analyzer {
            edge_left {
                type = 'custom'
                filter = ['asciifolding', 'lowercase']
                tokenizer = 'edge_left'
            }
            sorted {
                type ='custom'
                filter = ['asciifolding', 'lowercase', 'alpha_space']
                tokenizer = 'keyword'
            }
            alpha_space_only{
                type = 'custom'
                filter = ['standard', 'asciifolding', 'lowercase', 'stop', 'alpha_space']
                tokenizer = 'standard'
            }
            word_delimited{
                type = 'custom'
                filter = ['lowercase', 'word_delimited_catenate_only', 'edge_left']
                tokenizer = 'whitespace'
            }
            stemmed {
                type = 'snowball'
                language = 'English'
            }
            email_tokenized {
                type = 'custom'
                tokenizer = 'uax_url_email'
                filter = ['email', 'lowercase', 'unique']
            }
            email_strict {
                type = 'custom'
                tokenizer = 'uax_url_email'
                filter = ['lowercase', 'asciifolding']
            }
            phone_number_digits {
                type = 'custom'
                tokenizer = 'keyword'
                filter = ['phone_number_replace']
            }
            phone_number_edge {
                type = 'custom'
                tokenizer = 'keyword'
                filter = ['phone_number_replace', 'edge_left']
            }
        }
        tokenizer {
            edge_left {
                type = 'edgeNGram'
                side = 'front'
                min_gram = 1
                max_gram = 20
            }
        }
    }
    cluster {
        name = "elasticsearch_rguerra"
        hosts = ['localhost:9300']
    }
}