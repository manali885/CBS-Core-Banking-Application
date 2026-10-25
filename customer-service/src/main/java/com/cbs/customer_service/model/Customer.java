package com.cbs.customer_service.model;

public class Customer {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String phone;
    private KycStatus kycStatus;

    public Customer(){
    }
    public Customer(String name,String email,String phone,String address){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.kycStatus = KycStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }
}
