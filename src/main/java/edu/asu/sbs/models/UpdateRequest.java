package edu.asu.sbs.models;

import edu.asu.sbs.config.Constants;
import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Data
public class UpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@GeneratedValue(generator = "uuid2")
    //@GenericGenerator(name = "uuid2", strategy = "uuid2")
    //@Column(name = "updateRequestId", unique = true, columnDefinition = "BINARY(16)")
    private UUID updateRequestId;

    @NotNull
    private String role;

    @Transient
    private String username;

    @NotNull
    @Size(min = 60, max = 60)
    private String password;

    @NotNull
    @Size(min = 2)
    private String firstName;

    @Size(min = 0)
    private String middleName;

    @NotNull
    @Size(min = 2)
    private String lastName;

    @NotNull
    @Email
    @Size(min = 5, max = 254)
    @Column(unique = true, nullable = false, length = 254)
    private String email;

    @NotNull
    @Pattern(regexp = Constants.PHONE_NUMBER_REGEX)
    @Column(unique = true, nullable = false)
    private String phone;

    @NotNull
    @Size(min = 2, max = 50)
    private String addressLine1;

    @Size(min = 0, max = 50)
    private String addressLine2;

    @NotNull
    @Size(min = 2, max = 35)
    private String city;

    @NotNull
    @Size(min = 2, max = 35)
    private String State;

    @NotNull
    @Size(min = 5, max = 5)
    private String zip;

    @NotNull
    private String userType;
    @NotNull
    private LocalDateTime createdOn;

    private LocalDateTime modifiedOn;

    @NotNull
    private String status;

    @NotNull
    private Boolean active;

    @ManyToOne(fetch = FetchType.EAGER)
    private User approvedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    /**
     * @param updateRequestId
     * @param role
     * @param username
     * @param password
     * @param firstName
     * @param middleName
     * @param lastName
     * @param email
     * @param phone
     * @param addressLine1
     * @param addressLine2
     * @param city
     * @param state
     * @param zip
     * @param userType
     * @param createdOn
     * @param modifiedOn
     * @param status
     * @param active
     * @param approvedBy
     * @param user
     */
    public UpdateRequest(UUID updateRequestId, String role, String username, String password,
                         String firstName, String middleName, String lastName, String email, String phone, String addressLine1,
                         String addressLine2, String city, String state, String zip, String userType, LocalDateTime createdOn,
                         LocalDateTime modifiedOn, String status, Boolean active, User approvedBy, User user) {
        super();
        this.updateRequestId = updateRequestId;
        this.role = role;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.State = state;
        this.zip = zip;
        this.userType = userType;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
        this.status = status;
        this.active = active;
        this.approvedBy = approvedBy;
        this.user = user;
    }

    public UpdateRequest() {

    }

    /**
     * @return the updateRequestId
     */
    public UUID getUpdateRequestId() {
        return updateRequestId;
    }

    /**
     * @param updateRequestId the updateRequestId to set
     */
    public void setUpdateRequestId(UUID updateRequestId) {
        this.updateRequestId = updateRequestId;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the addressLine1
     */
    public String getAddressLine1() {
        return addressLine1;
    }

    /**
     * @param addressLine1 the addressLine1 to set
     */
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * @return the addressLine2
     */
    public String getAddressLine2() {
        return addressLine2;
    }

    /**
     * @param addressLine2 the addressLine2 to set
     */
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the state
     */
    public String getState() {
        return State;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        State = state;
    }

    /**
     * @return the zip
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the userType
     */
    public String getUserType() {
        return userType;
    }

    /**
     * @param userType the userType to set
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * @return the createdOn
     */
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @return the modifiedOn
     */
    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    /**
     * @param modifiedOn the modifiedOn to set
     */
    public void setModifiedOn(LocalDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the approvedBy
     */
    public User getApprovedBy() {
        return approvedBy;
    }

    /**
     * @param approvedBy the approvedBy to set
     */
    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UpdateRequest [updateRequestId=" + updateRequestId + ", role=" + role + ", username="
                + username + ", password=" + password + ", firstName=" + firstName + ", middleName=" + middleName
                + ", lastName=" + lastName + ", email=" + email + ", phone=" + phone + ", addressLine1=" + addressLine1
                + ", addressLine2=" + addressLine2 + ", city=" + city + ", State=" + State + ", zip=" + zip
                + ", userType=" + userType + ", createdOn=" + createdOn + ", modifiedOn=" + modifiedOn + ", status="
                + status + ", active=" + active + ", approvedBy=" + approvedBy + ", user=" + user + "]";
    }
}