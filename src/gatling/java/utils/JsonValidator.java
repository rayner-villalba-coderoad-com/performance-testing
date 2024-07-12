package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Set;

public class JsonValidator {
    final ObjectMapper objectMapper;
    final JsonSchema jsonSchema;
    final JsonNode jsonNode;

    private JsonValidator(JsonSchema jsonSchema, JsonNode jsonNode) {
        this(new ObjectMapper(), jsonSchema, jsonNode);
    }

    private JsonValidator(ObjectMapper objectMapper, JsonSchema jsonSchema, JsonNode jsonNode) {
        this.objectMapper = objectMapper;
        this.jsonSchema = jsonSchema;
        this.jsonNode = jsonNode;
    }

    public static JSONValidatorBuilder builder() {
        return builder(new ObjectMapper());
    }

    public static JSONValidatorBuilder builder(ObjectMapper objectMapper) {
        return new JSONValidatorBuilder(objectMapper);
    }

    public Set<ValidationMessage> validate() {
        return this.jsonSchema.validate(this.jsonNode);
    }

    public static final class JSONValidatorBuilder {
        final ObjectMapper objectMapper;
        JsonSchema jsonSchema;
        JsonNode jsonNode;

        private JSONValidatorBuilder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public JSONValidatorBuilder withJsonSchema(JsonSchema jsonSchema) {
            this.jsonSchema = jsonSchema;
            return this;
        }

        public JSONValidatorBuilder withJsonSchema(InputStream jsonSchemaInputStream) {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            this.jsonSchema = schemaFactory.getSchema(jsonSchemaInputStream);
            return this;
        }

        public JSONValidatorBuilder withJsonSchema(String jsonSchema) {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            this.jsonSchema = schemaFactory.getSchema(jsonSchema);
            return this;
        }

        public JSONValidatorBuilder withJsonSchema(JsonNode jsonSchema) {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            this.jsonSchema = schemaFactory.getSchema(jsonSchema);
            return this;
        }

        public JSONValidatorBuilder withJsonNode(JsonNode jsonNode) {
            this.jsonNode = jsonNode;
            return this;
        }

        public JSONValidatorBuilder withJsonNode(InputStream inputStream) throws IOException {
            this.jsonNode = this.objectMapper.readTree(inputStream);
            return this;
        }

        public JSONValidatorBuilder withJsonNode(Reader reader) throws IOException {
            this.jsonNode = this.objectMapper.readTree(reader);
            return this;
        }

        public JSONValidatorBuilder withJsonNode(String jsonNodeText) throws IOException {
            this.jsonNode = this.objectMapper.readTree(jsonNodeText);
            return this;
        }

        public JSONValidatorBuilder withJsonNode(byte[] jsonNodeBytes) throws IOException {
            this.jsonNode = this.objectMapper.readTree(jsonNodeBytes);
            return this;
        }

        public JsonValidator build() {
            return new JsonValidator(this.objectMapper, this.jsonSchema, this.jsonNode);
        }
    }
}