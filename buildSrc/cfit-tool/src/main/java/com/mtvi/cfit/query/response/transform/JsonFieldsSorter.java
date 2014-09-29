package com.mtvi.cfit.query.response.transform;

import com.mtvi.cfit.CfitException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Does sort of keys on JSON document.
 *
 * @author dubarenk
 */
public class JsonFieldsSorter implements ResponseTransformer {

    @Override
    public void transform(Reader input, Writer output) throws CfitException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory jsonFactory = new JsonFactory(mapper);
            Map obj = jsonFactory.createJsonParser(input).readValueAs(Map.class);
            JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(output);
            jsonGenerator.useDefaultPrettyPrinter();
            jsonGenerator.writeObject(orderFields(obj));
        } catch (Exception e) {
            throw new CfitException(e);
        }
    }

    private Map orderFields(Map<Object, Object> obj) {
        Map<Object, Object> result = new TreeMap<Object, Object>();
        for (Map.Entry<Object, Object> entry : obj.entrySet()) {
            result.put(entry.getKey(), orderFields(entry.getValue()));
        }
        return result;
    }

    private Object orderFields(Object value) {
        if (value instanceof Map) {
            return orderFields((Map) value);
        }
        if (value instanceof List) {
            return orderFields((List) value);
        }
        return value;
    }

    private List orderFields(List value) {
        List result = new ArrayList(value.size());
        for (Object o : value) {
            result.add(orderFields(o));
        }
        return result;
    }

}
