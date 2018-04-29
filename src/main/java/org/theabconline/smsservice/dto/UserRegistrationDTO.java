package org.theabconline.smsservice.dto;

public class UserRegistrationDTO {

    private String userId;
    private String name;
    private Integer department;
    private String email;
    private String mobile;

    public UserRegistrationDTO() {}

    public UserRegistrationDTO(String userId, String name, Integer department, String email, String mobile) {
        this.userId = userId;
        this.name = name;
        this.department = department;
        this.email = email;
        this.mobile = mobile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDepartment() {
        return department;
    }

    public void setDepartment(Integer department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
