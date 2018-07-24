package org.theabconline.smsservice.dto;

public class UserRegistrationDTO {

    private String userid;
    private String name;
    private Integer department;
    private String email;
    private String mobile;

    public UserRegistrationDTO() {
    }

    public UserRegistrationDTO(String userid, String name, Integer department, String email, String mobile) {
        this.userid = userid;
        this.name = name;
        this.department = department;
        this.email = email;
        this.mobile = mobile;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
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
