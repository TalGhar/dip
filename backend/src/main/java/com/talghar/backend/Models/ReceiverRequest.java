/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend.Models;

/**
 *
 * @author talghar
 */
public class ReceiverRequest {

    private String username;
    private String password;
    private String form;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public String getForm() {
        return form;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setForm(String form) {
        this.form = form;
    }
}
