/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend.Models;

/**
 *
 * @author talghar
 */
public class WalletRequest {

//    private String address;
    private String ballance;
    private String categoryID;
    private String ID;
//    private String owner;

//    public String getAddress() {
//        return address;
//    }

    public String getBallance() {
        return ballance;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public String getID() {
        return ID;
    }

//    public String getOwner() {
//        return owner;
//    }

//    public void setAddress(String address) {
//        this.address = address;
//    }

    public void setBallance(String ballance) {
        this.ballance = ballance;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

//    public void setOwner(String owner) {
//        this.owner = owner;
//    }

}
