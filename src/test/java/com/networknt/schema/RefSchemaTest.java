package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Set;

/**
 * Created by ekilmer on 2021-8-19.
 */
public class RefSchemaTest extends BaseJsonSchemaValidatorTest {
    static final String phoneNumberJsonValue = "{\n" +
            "    \"country-code\": \"XYZ\",\n" +
            "    \"number\": \"123\"\n" +
            "}\n";
    static final String addressJsonValue = "{\n" +
            "    \"street_address\": \"123 Main\",\n" +
            "    \"city\": \"Nowhere\",\n" +
            "    \"state\": \"Somewhere\",\n" +
            "    \"phone_number\": " + phoneNumberJsonValue +
            "}\n";
    static final String topJsonValue = "{\n" +
            "    \"property1\": \"a\",\n" +
            "    \"property3\": " + addressJsonValue +
            "}\n";

    @Test()
    public void testRefSchema() throws IOException {
        JsonSchema schema = getJsonSchemaFromClasspath("schema/refSchema.json");
        JsonSchema propsSchema = schema.getRefSchema("#/definitions/phone-number");
        Set<ValidationMessage> errors = propsSchema.validate(getJsonNodeFromStringContent(phoneNumberJsonValue));
        assertEquals(0, errors.size());
    }

    @Test()
    public void testRefSchemaWithRefs() throws IOException {
        JsonSchema schema = getJsonSchemaFromClasspath("schema/refSchema.json");
        JsonSchema propsSchema = schema.getRefSchema("#/definitions/address");
        JsonSchema propsSchema2 = schema.getRefSchema("#/definitions/address");
        // Same heap object
        assertEquals(propsSchema, propsSchema2);
        JsonNode contentNode = getJsonNodeFromStringContent(addressJsonValue);
        Set<ValidationMessage> errors = propsSchema.validate(contentNode);
        assertEquals(0, errors.size());
        errors = propsSchema2.validate(contentNode);
        assertEquals(0, errors.size());
    }

    @Test()
    public void testRefSchemaRoot() throws IOException {
        JsonSchema schema = getJsonSchemaFromClasspath("schema/refSchema.json");
        JsonSchema propsSchema = schema.getRefSchema("#");
        Set<ValidationMessage> errors = propsSchema.validate(getJsonNodeFromStringContent(topJsonValue));
        assertEquals(0, errors.size());
    }
}
