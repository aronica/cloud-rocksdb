package cloud.rocksdb.server.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * @author jie.huang
 *         Date: 16/7/8
 *         Time: 下午4:32
 */
public class JacksonUtil {
    private final static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.setSerializationInclusion(NON_NULL);
    }

    public static <T> byte[] toJsonAsBytes(T o) throws IOException {
        return mapper.writeValueAsBytes(o);
    }

    public static <T> String toJson(T o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    public static <T> T toObject(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }

    public static <T> T toObject(byte[] json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }

    public static <T> List<T> toObjectList(String json, Class<T> clazz) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        return mapper.readValue(json, javaType);
    }

    public static <T> Set<T> toObjectSet(String json, Class<T> clazz) throws IOException {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(Set.class, clazz);
        return mapper.readValue(json, javaType);
    }

    public static ObjectNode createNode() {
        return mapper.createObjectNode();
    }
}
