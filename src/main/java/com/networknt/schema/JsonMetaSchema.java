/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonMetaSchema {


    private static final Logger logger = LoggerFactory
            .getLogger(JsonMetaSchema.class);
    private static Map<String, String> UNKNOWN_KEYWORDS = new ConcurrentHashMap<String, String>();

    static PatternFormat pattern(String name, String regex) {
        return new PatternFormat(name, regex);
    }

    public static final List<Format> COMMON_BUILTIN_FORMATS = new ArrayList<Format>();

    // this section contains formats that is common for all specification versions.
    static {
        COMMON_BUILTIN_FORMATS.add(pattern("time", "^\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?$"));
        COMMON_BUILTIN_FORMATS.add(pattern("ip-address",
                "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
        COMMON_BUILTIN_FORMATS.add(pattern("ipv4", "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$"));
        COMMON_BUILTIN_FORMATS.add(pattern("ipv6",
                "^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$"));

        // From RFC 3986
        // ALPHA [A-Za-z]
        // DIGIT [0-9]
        // scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        //   => [A-Za-z][A-Za-z0-9+.-]*
        // unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
        //   => [A-Za-z0-9._~\-]
        // gen-delims [:/?#\[\]@]
        // sub-delims [!$&'()*+,;=]
        // reserved = = gen-delims / sub-delims
        //   => [:/?#\[\]@!$&'()*+,;=]
        // pct-encoded   = "%" HEXDIG HEXDIG
        //   => [A-Za-z0-9%] (approximation)
        // pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
        //   => [A-Za-z0-9._~\-%!$&'()*+,;=:@]
        // userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
        //   => [A-Za-z0-9._~\-%!$&'()*+,;=:]*
        // host = IP-literal / IPv4address / reg-name
        //   => [A-Za-z0-9._~\-!$&'()*+,;=%:\[\]]* (approximation)
        // port = *DIGiT
        //   => [0-9]*
        // authority = [ userinfo "@" ] host [ ":" port ]
        //   => ([A-Za-z0-9._~\-%!$&'()*+,;=:]*@)?[A-Za-z0-9._~\-!$&'()*+,;=%:\[\]]*(:[0-9]*)?
        // hier-part  = "//" authority path-abempty
        //                 / path-absolute
        //                 / path-rootless
        //                 / path-empty
        //  => (\/\/([A-Za-z0-9._~\-%!$&'()*+,;=:]*@)?[A-Za-z0-9._~\-!$&'()*+,;=%:\[\]]*(:[0-9]*)?)?[A-Za-z0-9._~\-%!$&'()*+,;=:@\/]* (approximation)
        // query = *( pchar / "/" / "?" )
        //  => [A-Za-z0-9._~\-%!$&'()*+,;=:@\/?]*
        // fragment = *( pchar / "/" / "?" )
        // => [A-Za-z0-9._~\-%!$&'()*+,;=:@\/?]*
        // uri = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
        COMMON_BUILTIN_FORMATS.add(pattern("uri",
            "^[A-Za-z][A-Za-z0-9+.-]*:(\\/\\/([A-Za-z0-9._~\\-%!$&'()*+,;=:]*@)?[A-Za-z0-9._~\\-!$&'()*+,;=%:\\[\\]]*(:[0-9]*)?)?[A-Za-z0-9._~\\-%!$&'()*+,;=:@\\/]*([?][A-Za-z0-9._~\\-%!$&'()*+,;=:@\\/?]*)?([#][A-Za-z0-9._~\\-%!$&'()*+,;=:@\\/?]*)?"));
        COMMON_BUILTIN_FORMATS.add(pattern("color",
                "(#?([0-9A-Fa-f]{3,6})\\b)|(aqua)|(black)|(blue)|(fuchsia)|(gray)|(green)|(lime)|(maroon)|(navy)|(olive)|(orange)|(purple)|(red)|(silver)|(teal)|(white)|(yellow)|(rgb\\(\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*\\))|(rgb\\(\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*\\))"));
        COMMON_BUILTIN_FORMATS.add(pattern("hostname",
                "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$"));
        COMMON_BUILTIN_FORMATS.add(pattern("alpha", "^[a-zA-Z]+$"));
        COMMON_BUILTIN_FORMATS.add(pattern("alphanumeric", "^[a-zA-Z0-9]+$"));
        COMMON_BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
        COMMON_BUILTIN_FORMATS.add(pattern("utc-millisec", "^[0-9]+(\\.?[0-9]+)?$"));
        COMMON_BUILTIN_FORMATS.add(pattern("style", "\\s*(.+?):\\s*([^;]+);?"));
    }

    public static class Builder {
        private Map<String, Keyword> keywords = new HashMap<String, Keyword>();
        private Map<String, Format> formats = new HashMap<String, Format>();
        private String uri;
        private String idKeyword = "id";

        public Builder(String uri) {
            this.uri = uri;
        }

        private static Map<String, Keyword> createKeywordsMap(Map<String, Keyword> kwords, Map<String, Format> formats) {
            final Map<String, Keyword> map = new HashMap<String, Keyword>();
            for (Map.Entry<String, Keyword> type : kwords.entrySet()) {
                String keywordName = type.getKey();
                Keyword keyword = type.getValue();
                if (ValidatorTypeCode.FORMAT.getValue().equals(keywordName)) {
                    if (!(keyword instanceof FormatKeyword)) {
                        throw new IllegalArgumentException("Overriding the keyword 'format' is not supported");
                    }
                    // ignore - format keyword will be created again below.
                } else {
                    map.put(keyword.getValue(), keyword);
                }
            }
            final FormatKeyword formatKeyword = new FormatKeyword(ValidatorTypeCode.FORMAT, formats);
            map.put(formatKeyword.getValue(), formatKeyword);
            return Collections.unmodifiableMap(map);
        }

        public Builder addKeyword(Keyword keyword) {
            this.keywords.put(keyword.getValue(), keyword);
            return this;
        }

        public Builder addKeywords(Collection<? extends Keyword> keywords) {
            for (Keyword keyword : keywords) {
                this.keywords.put(keyword.getValue(), keyword);
            }
            return this;
        }

        public Builder addFormat(Format format) {
            this.formats.put(format.getName(), format);
            return this;
        }

        public Builder addFormats(Collection<? extends Format> formats) {
            for (Format format : formats) {
                addFormat(format);
            }
            return this;
        }


        public Builder idKeyword(String idKeyword) {
            this.idKeyword = idKeyword;
            return this;
        }

        public JsonMetaSchema build() {
            // create builtin keywords with (custom) formats.
            final Map<String, Keyword> kwords = createKeywordsMap(keywords, formats);
            return new JsonMetaSchema(uri, idKeyword, kwords);
        }
    }

    private final String uri;
    private final String idKeyword;
    private final Map<String, Keyword> keywords;

    private JsonMetaSchema(String uri, String idKeyword, Map<String, Keyword> keywords) {
        if (StringUtils.isBlank(uri)) {
            throw new IllegalArgumentException("uri must not be null or blank");
        }
        if (StringUtils.isBlank(idKeyword)) {
            throw new IllegalArgumentException("idKeyword must not be null or blank");
        }
        if (keywords == null) {
            throw new IllegalArgumentException("keywords must not be null ");
        }

        this.uri = uri;
        this.idKeyword = idKeyword;
        this.keywords = keywords;
    }

    public static JsonMetaSchema getV4() {
        return new Version4().getInstance();
    }

    public static JsonMetaSchema getV6() {
        return new Version6().getInstance();
    }

    public static JsonMetaSchema getV7() {
        return new Version7().getInstance();
    }

    public static JsonMetaSchema getV201909() {
        return new Version201909().getInstance();
    }

    public static JsonMetaSchema getV202012() {
        return new Version202012().getInstance();
    }

    /**
     * Builder without keywords or formats.
     * <p>
     * Use {@link #getV4()} for the Draft 4 Metaschema, or if you need a builder based on Draft4, use
     *
     * <code>
     * JsonMetaSchema.builder("http://your-metaschema-uri", JsonSchemaFactory.getDraftV4()).build();
     * </code>
     *
     * @param uri the URI of the metaschema that will be defined via this builder.
     * @return a builder instance without any keywords or formats - usually not what one needs.
     */
    public static Builder builder(String uri) {
        return new Builder(uri);
    }

    /**
     * @param uri       the URI of your new JsonMetaSchema that will be defined via this builder.
     * @param blueprint the JsonMetaSchema to base your custom JsonMetaSchema on.
     * @return a builder instance preconfigured to be the same as blueprint, but with a different uri.
     */
    public static Builder builder(String uri, JsonMetaSchema blueprint) {
        FormatKeyword formatKeyword = (FormatKeyword) blueprint.keywords.get(ValidatorTypeCode.FORMAT.getValue());
        if (formatKeyword == null) {
            throw new IllegalArgumentException("The formatKeyword did not exist - blueprint is invalid.");
        }
        return builder(uri)
                .idKeyword(blueprint.idKeyword)
                .addKeywords(blueprint.keywords.values())
                .addFormats(formatKeyword.getFormats());
    }

    public String readId(JsonNode schemaNode) {
        return readText(schemaNode, idKeyword);
    }

    public JsonNode getNodeByFragmentRef(String ref, JsonNode node) {
        boolean supportsAnchor = keywords.containsKey("$anchor");
        String refName = supportsAnchor ? ref.substring(1) : ref;
        String fieldToRead = supportsAnchor ? "$anchor" : idKeyword;

        boolean nodeContainsRef = refName.equals(readText(node, fieldToRead));
        if (nodeContainsRef) {
            return node;
        } else {
            Iterator<JsonNode> children = node.elements();
            while (children.hasNext()) {
                JsonNode refNode = getNodeByFragmentRef(ref, children.next());
                if (refNode != null) {
                    return refNode;
                }
            }
        }
        return null;
    }

    private String readText(JsonNode node, String field) {
        JsonNode idNode = node.get(field);
        if (idNode == null || !idNode.isTextual()) {
            return null;
        }
        return idNode.textValue();
    }

    public String getUri() {
        return uri;
    }


    public JsonValidator newValidator(ValidationContext validationContext, String schemaPath, String keyword /* keyword */, JsonNode schemaNode,
                                      JsonSchema parentSchema, String customMessage) {

        try {
            Keyword kw = keywords.get(keyword);
            if (kw == null) {
                if (UNKNOWN_KEYWORDS.put(keyword, keyword) == null) {
                    logger.warn("Unknown keyword " + keyword + " - you should define your own Meta Schema. If the keyword is irrelevant for validation, just use a NonValidationKeyword");
                }
                return null;
            }
            kw.setCustomMessage(customMessage);
            return kw.newValidator(schemaPath, schemaNode, parentSchema, validationContext);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof JsonSchemaException) {
                logger.error("Error:", e);
                throw (JsonSchemaException) e.getTargetException();
            } else {
                logger.warn("Could not load validator " + keyword);
                throw new JsonSchemaException(e.getTargetException());
            }
        } catch (JsonSchemaException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Could not load validator " + keyword);
            throw new JsonSchemaException(e);
        }
    }


}
