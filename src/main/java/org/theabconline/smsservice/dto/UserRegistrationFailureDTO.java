package org.theabconline.smsservice.dto;

public class UserRegistrationFailureDTO extends UserRegistrationDTO {

    private String errorMessage;

    public UserRegistrationFailureDTO(UserRegistrationDTO userRegistrationDTO, String errorMessage) {
        super(userRegistrationDTO.getUserid(),
                userRegistrationDTO.getName(),
                userRegistrationDTO.getDepartment(),
                userRegistrationDTO.getEmail(),
                userRegistrationDTO.getMobile());
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
