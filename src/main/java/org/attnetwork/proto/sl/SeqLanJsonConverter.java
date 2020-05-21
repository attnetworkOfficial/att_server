package org.attnetwork.proto.sl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.attnetwork.exception.AException;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeqLanJsonConverter {
  private static final JsonFactory jsonFactory = new JsonFactory();

  static {
    jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
    jsonFactory.disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
  }

  public static String jsonToClass(String jsonString, String className) {
    try {
      JsonNode object = new ObjectMapper(jsonFactory).readValue(jsonString, JsonNode.class);
      JsonNode type = object.get("type");
      if (type != null && SeqLan.OBJECT.equals(type.asText())) {
        StringBuilder clazz = new StringBuilder();
        jsonToObject(clazz, 2, 0, className, object.get("fields"));
        return clazz.toString();
      } else {
        return null;
      }
    } catch (JsonProcessingException e) {
      throw AException.wrap(e);
    }
  }

  private static void jsonToObject(StringBuilder clazz, int ind, int depth, String className, JsonNode fields) {
    indentation(clazz, ind * depth);
    clazz.append("public ");
    if (depth > 0) {
      clazz.append("static ");
    }
    clazz.append("final class ").append(className).append(" extends AbstractSeqLanObject {\n");
    int fDepth = depth + 1;
    fields.fieldNames().forEachRemaining(
        fieldName -> jsonToField(clazz, ind, fDepth, snakeCase2CamelCase(fieldName), fields.get(fieldName)));
    indentation(clazz, ind * depth);
    clazz.append("}\n");
  }

  private static void jsonToField(StringBuilder clazz, int ind, int depth, String name, JsonNode field) {
    JsonNode type = field.get("type");
    if (type == null) {
      return;
    }
    indentation(clazz, ind * depth);
    clazz.append("public ");
    switch (type.asText()) {
      case SeqLan.RAW:
        clazz.append("byte[] ").append(name).append(";\n");
        break;
      case SeqLan.NUMBER:
        clazz.append("BigInteger ").append(name).append(";\n");
        break;
      case SeqLan.DECIMAL:
        clazz.append("BigDecimal ").append(name).append(";\n");
        break;
      case SeqLan.STRING:
        clazz.append("String ").append(name).append(";\n");
        break;
      case SeqLan.ARRAY:
        JsonNode element = field.get("element");
        String className = fieldJavaType(element.get("type").asText());
        if (className == null) {
          className = StringUtils.capitalize(name);
          clazz.append("List<").append(className).append("> ").append(name).append(";\n\n");
          jsonToObject(clazz, ind, depth, className, element.get("fields"));
        } else {
          clazz.append("List<").append(className).append("> ").append(name).append(";\n\n");
        }
        break;
      case SeqLan.OBJECT:
        className = StringUtils.capitalize(name);
        clazz.append(className).append(" ").append(name).append(";\n\n");
        jsonToObject(clazz, ind, depth, className, field.get("fields"));
        break;
    }
  }


  private static String fieldJavaType(String type) {
    switch (type) {
      case SeqLan.RAW:
        return "byte[]";
      case SeqLan.NUMBER:
        return "BigInteger";
      case SeqLan.DECIMAL:
        return "BigDecimal";
      case SeqLan.STRING:
        return "String";
      default:
        return null;
    }
  }

  private static void indentation(StringBuilder clazz, int i) {
    while (i-- > 0) {
      clazz.append(" ");
    }
  }

  private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("_([a-zA-Z])");

  private static String snakeCase2CamelCase(String snakeString) {
    Matcher m = SNAKE_CASE_PATTERN.matcher(snakeString);
    StringBuffer sb = new StringBuffer();

    while (m.find()) {
      m.appendReplacement(sb, m.group(1).toUpperCase());
    }

    m.appendTail(sb);
    return sb.toString();
  }
}
