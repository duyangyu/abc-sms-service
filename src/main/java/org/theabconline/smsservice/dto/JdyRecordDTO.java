package org.theabconline.smsservice.dto;

import java.util.Map;

public class JdyRecordDTO {

    private String data_id;
    private Map<String, Map<String, Object>> data;

    public JdyRecordDTO() {
    }

    public String getData_id() {
        return data_id;
    }

    public void setData_id(String data_id) {
        this.data_id = data_id;
    }

    public Map<String, Map<String, Object>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Object>> data) {
        this.data = data;
    }
}
