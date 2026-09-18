package com.budaos.business.notice.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 文件key反序列化 - 简化版
 */
@Slf4j
public class AttachmentKeyVoDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        try {
            if (node.isArray()) {
                // 如果是数组，返回逗号分隔的字符串
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < node.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(node.get(i).asText());
                }
                return sb.toString();
            } else if (node.isObject()) {
                // 如果是对象，尝试获取 fileKey 字段
                JsonNode fileKeyNode = node.get("fileKey");
                if (fileKeyNode != null) {
                    return fileKeyNode.asText();
                }
            }
            return node.asText();
        } catch (Exception e) {
            log.error("文件Key反序列化失败", e);
            return "";
        }
    }
}
