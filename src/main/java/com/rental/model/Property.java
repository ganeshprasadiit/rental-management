package com.rental.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(nullable = false)
    private Integer bathrooms;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rentAmount;

    @Column(nullable = false)
    private String status;

    private String tenantName;

    private String tenantEmail;

    private String tenantPhone;

    @Column(length = 1000)
    private String description;

    private Integer rentDueDay;

    private String lastPaidDate;
    private String paymentMode;
    private String paymentNotes;
    private String tenantMoveInDate;

    // Utilities - UK common: Electricity, Gas, Water, Internet, Council Tax, TV License
    private String electricityProvider;
    @Column(precision = 10, scale = 2)
    private BigDecimal electricityCost;
    private String electricityPaidBy;

    private String gasProvider;
    @Column(precision = 10, scale = 2)
    private BigDecimal gasCost;
    private String gasPaidBy;

    private String waterProvider;
    @Column(precision = 10, scale = 2)
    private BigDecimal waterCost;
    private String waterPaidBy;

    private String internetProvider;
    @Column(precision = 10, scale = 2)
    private BigDecimal internetCost;
    private String internetPaidBy;

    private String councilTaxProvider;
    @Column(precision = 10, scale = 2)
    private BigDecimal councilTaxCost;
    private String councilTaxPaidBy;

    private String tvLicenseProvider;
    @Column(precision = 10, scale = 2)
    private BigDecimal tvLicenseCost;
    private String tvLicensePaidBy;



    // Constructors
    public Property() {}

    public Property(String address, String type, Integer bedrooms, Integer bathrooms,
                    BigDecimal rentAmount, String status) {
        this.address = address;
        this.type = type;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.rentAmount = rentAmount;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public BigDecimal getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(BigDecimal rentAmount) {
        this.rentAmount = rentAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public String getTenantPhone() {
        return tenantPhone;
    }

    public void setTenantPhone(String tenantPhone) {
        this.tenantPhone = tenantPhone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRentDueDay() {
        return rentDueDay;
    }

    public void setRentDueDay(Integer rentDueDay) {
        this.rentDueDay = rentDueDay;
    }

    public String getLastPaidDate() {
        return lastPaidDate;
    }

    public void setLastPaidDate(String lastPaidDate) {
        this.lastPaidDate = lastPaidDate;
    }

    // Add these getters and setters with the existing ones
    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentNotes() {
        return paymentNotes;
    }

    public void setPaymentNotes(String paymentNotes) {
        this.paymentNotes = paymentNotes;
    }

    public String getTenantMoveInDate() {
        return tenantMoveInDate;
    }

    public void setTenantMoveInDate(String tenantMoveInDate) {
        this.tenantMoveInDate = tenantMoveInDate;
    }

    // Utilities getters and setters

    public String getElectricityProvider() {
        return electricityProvider;
    }

    public void setElectricityProvider(String electricityProvider) {
        this.electricityProvider = electricityProvider;
    }

    public BigDecimal getElectricityCost() {
        return electricityCost;
    }

    public void setElectricityCost(BigDecimal electricityCost) {
        this.electricityCost = electricityCost;
    }

    public String getElectricityPaidBy() {
        return electricityPaidBy;
    }

    public void setElectricityPaidBy(String electricityPaidBy) {
        this.electricityPaidBy = electricityPaidBy;
    }

    public String getGasProvider() {
        return gasProvider;
    }

    public void setGasProvider(String gasProvider) {
        this.gasProvider = gasProvider;
    }

    public BigDecimal getGasCost() {
        return gasCost;
    }

    public void setGasCost(BigDecimal gasCost) {
        this.gasCost = gasCost;
    }

    public String getGasPaidBy() {
        return gasPaidBy;
    }

    public void setGasPaidBy(String gasPaidBy) {
        this.gasPaidBy = gasPaidBy;
    }

    public String getWaterProvider() {
        return waterProvider;
    }

    public void setWaterProvider(String waterProvider) {
        this.waterProvider = waterProvider;
    }

    public BigDecimal getWaterCost() {
        return waterCost;
    }

    public void setWaterCost(BigDecimal waterCost) {
        this.waterCost = waterCost;
    }

    public String getWaterPaidBy() {
        return waterPaidBy;
    }

    public void setWaterPaidBy(String waterPaidBy) {
        this.waterPaidBy = waterPaidBy;
    }

    public String getInternetProvider() {
        return internetProvider;
    }

    public void setInternetProvider(String internetProvider) {
        this.internetProvider = internetProvider;
    }

    public BigDecimal getInternetCost() {
        return internetCost;
    }

    public void setInternetCost(BigDecimal internetCost) {
        this.internetCost = internetCost;
    }

    public String getInternetPaidBy() {
        return internetPaidBy;
    }

    public void setInternetPaidBy(String internetPaidBy) {
        this.internetPaidBy = internetPaidBy;
    }

    public String getCouncilTaxProvider() {
        return councilTaxProvider;
    }

    public void setCouncilTaxProvider(String councilTaxProvider) {
        this.councilTaxProvider = councilTaxProvider;
    }

    public BigDecimal getCouncilTaxCost() {
        return councilTaxCost;
    }

    public void setCouncilTaxCost(BigDecimal councilTaxCost) {
        this.councilTaxCost = councilTaxCost;
    }

    public String getCouncilTaxPaidBy() {
        return councilTaxPaidBy;
    }

    public void setCouncilTaxPaidBy(String councilTaxPaidBy) {
        this.councilTaxPaidBy = councilTaxPaidBy;
    }

    public String getTvLicenseProvider() {
        return tvLicenseProvider;
    }

    public void setTvLicenseProvider(String tvLicenseProvider) {
        this.tvLicenseProvider = tvLicenseProvider;
    }

    public BigDecimal getTvLicenseCost() {
        return tvLicenseCost;
    }

    public void setTvLicenseCost(BigDecimal tvLicenseCost) {
        this.tvLicenseCost = tvLicenseCost;
    }

    public String getTvLicensePaidBy() {
        return tvLicensePaidBy;
    }

    public void setTvLicensePaidBy(String tvLicensePaidBy) {
        this.tvLicensePaidBy = tvLicensePaidBy;
    }
}