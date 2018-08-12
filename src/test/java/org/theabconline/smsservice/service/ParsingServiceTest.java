package org.theabconline.smsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.theabconline.smsservice.entity.FormBO;
import org.theabconline.smsservice.mapping.FieldMapping;
import org.theabconline.smsservice.mapping.FormMetadata;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.FormRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ParsingServiceTest {

    private static final String MESSAGE = "{\"data\":{\"_id\":\"5b4ea350a25367212cef8ad0\",\"_widget_1523332159193\":\"A2018070004\",\"_widget_1523332889737\":\"开启\",\"_widget_1523334349599\":[\"15011483305\",\"17600232730\"],\"_widget_1525090890427\":\"尊敬的${name} ，感谢您申请${season}志愿者，筛选结果将于${time}公布，您可届时在ABC企业号-个人中心中查询。\",\"_widget_1525090890655\":\"2018春季北京咨询季—面试\",\"_widget_1525093769521\":\"面试\",\"_widget_1525093770254\":2,\"_widget_1525093770282\":\"测试-志愿者申请\",\"_widget_1525666970346\":[],\"_widget_1528175546128\":\"群发\",\"_widget_1529819292779\":\"短信\",\"_widget_1529819293243\":[],\"_widget_1529819293978\":[\"杜阳宇15011483305\",\"杜阳宇217600232730\"],\"_widget_1530072482212\":\"SMS_129759808\",\"_widget_1531304739460\":\"\",\"_widget_1531537999423\":\"{\\n  \\\"smsTemplates\\\": [\\n    {\\n      \\\"smsTemplateCode\\\": \\\"SMS_139972716\\\",\\n      \\\"phoneNumbersWidget\\\": \\\"_widget_1523334349599\\\",\\n      \\\"fieldMappings\\\": [\\n        {\\n          \\\"widget\\\": \\\"_widget_1531701681875\\\",\\n          \\\"smsField\\\": \\\"name\\\"\\n        },\\n        {\\n          \\\"widget\\\": \\\"_widget_1531701681890\\\",\\n          \\\"smsField\\\": \\\"season\\\"\\n        },\\n        {\\n          \\\"widget\\\": \\\"_widget_1531701682021\\\",\\n          \\\"smsField\\\": \\\"time\\\"\\n        }\\n      ]\\n    }\\n  ]\\n}\",\"_widget_1531700970559\":\"N\",\"_widget_1531701681875\":\"杜阳宇\",\"_widget_1531701681890\":\"2018秋季咨询季\",\"_widget_1531701682021\":\"2018年8月31日\",\"_widget_1531793836458\":\"\",\"appId\":\"5b47232e5bec850a2d15dcaf\",\"createTime\":\"2018-07-18T02:17:52.353Z\",\"creator\":null,\"deleteTime\":null,\"deleter\":null,\"entryId\":\"5acc3444f3f9e5594d199c16\",\"flowState\":1,\"formName\":\"活动-发布活动通知-beta\",\"updateTime\":\"2018-07-18T02:17:52.353Z\",\"updater\":null},\"op\":\"data_create\"}";

    private static final String APP_ID = "5b47232e5bec850a2d15dcaf";
    private static final String ENTRY_ID = "5acc3444f3f9e5594d199c16";
    private static final String METADATA_WIDGET = "_widget_1531537999423";
    private static final String MESSAGE_WIDGET = "_widget_1531793836458";

    @InjectMocks
    ParsingService fixture;

    @Mock
    FormRepository formRepository;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fixture, "defaultPath", "/data");
        ReflectionTestUtils.setField(fixture, "appIdWidget", "appId");
        ReflectionTestUtils.setField(fixture, "entryIdWidget", "entryId");
        ReflectionTestUtils.setField(fixture, "dataIdWidget", "_id");
        ReflectionTestUtils.setField(fixture, "mapper", new ObjectMapper());
        FormBO formBO = new FormBO();
        formBO.setAppId(APP_ID);
        formBO.setEntryId(ENTRY_ID);
        formBO.setMetadataWidget(METADATA_WIDGET);
        when(formRepository.findAllByAppIdAndEntryId(eq(APP_ID), eq(ENTRY_ID))).thenReturn(Lists.newArrayList(formBO));
    }

    @Test
    public void testGetAppId() throws IOException {
        String appId = fixture.getAppId(MESSAGE);

        assertEquals(APP_ID, appId);
    }

    @Test
    public void testGetEntryId() throws IOException {
        String entryId = fixture.getEntryId(MESSAGE);

        assertEquals(ENTRY_ID, entryId);
    }

    @Test
    public void testGetDataId() throws IOException {
        String dataId = fixture.getDataId(MESSAGE);

        assertEquals("5b4ea350a25367212cef8ad0", dataId);
    }

    @Test
    public void testGetFormMetaData() throws IOException {
        FormMetadata formMetadata = fixture.getFormMetadata(MESSAGE);

        assertEquals(1, formMetadata.getSmsTemplates().size());
        SmsTemplate smsTemplate = formMetadata.getSmsTemplates().get(0);
        assertEquals("SMS_139972716", smsTemplate.getSmsTemplateCode());
        assertEquals("_widget_1523334349599", smsTemplate.getPhoneNumbersWidget());
        assertEquals(3, smsTemplate.getFieldMappings().size());
        Map<String, String> widgetFieldMap = Maps.newHashMap();
        for (FieldMapping fieldMapping : smsTemplate.getFieldMappings()) {
            widgetFieldMap.put(fieldMapping.getWidget(), fieldMapping.getSmsField());
        }
        assertEquals("name", widgetFieldMap.get("_widget_1531701681875"));
        assertEquals("season", widgetFieldMap.get("_widget_1531701681890"));
        assertEquals("time", widgetFieldMap.get("_widget_1531701682021"));
    }

    @Test
    public void testPhoneNumbers() throws IOException {
        String phoneNumbersWidget = fixture.getFormMetadata(MESSAGE).getSmsTemplates().get(0).getPhoneNumbersWidget();
        String phoneNumbers = fixture.getPhoneNumbers(MESSAGE, phoneNumbersWidget);

        assertEquals("15011483305,17600232730", phoneNumbers);
    }

    @Test
    public void testGetPayload() throws IOException {
        List<FieldMapping> fieldMappings = fixture.getFormMetadata(MESSAGE).getSmsTemplates().get(0).getFieldMappings();
        String payload = fixture.getPayload(MESSAGE, fieldMappings);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> payloadMap = objectMapper.readValue(payload, Map.class);

        assertEquals("杜阳宇", payloadMap.get("name"));
        assertEquals("2018秋季咨询季", payloadMap.get("season"));
        assertEquals("2018年8月31日", payloadMap.get("time"));

    }

    @Test
    public void testGetFieldValue() throws IOException {

        assertEquals("A2018070004", fixture.getFieldValue(MESSAGE, "_widget_1523332159193"));
    }

}
