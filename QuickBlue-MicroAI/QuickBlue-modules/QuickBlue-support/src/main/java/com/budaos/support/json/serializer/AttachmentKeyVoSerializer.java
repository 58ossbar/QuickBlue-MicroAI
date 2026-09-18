package com.budaos.support.json.serializer;

import com.budaos.support.domain.vo.AttachmentVO;
import com.budaos.support.service.AttachmentService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件key进行序列化对象
 */
public class AttachmentKeyVoSerializer extends JsonSerializer<String> {

    @Resource
    private AttachmentService fileService;


    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (StringUtils.isEmpty(value)) {
            jsonGenerator.writeObject(List.of());
            return;
        }
        if(fileService == null){
            jsonGenerator.writeString(value);
            return;
        }
        String[] fileKeyArray = value.split(",");
        List<String> fileKeyList = Arrays.asList(fileKeyArray);
        List<AttachmentVO> fileKeyVOList = fileService.getFileList(fileKeyList);
        jsonGenerator.writeObject(fileKeyVOList);
    }
}
