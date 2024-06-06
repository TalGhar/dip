/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend.Models;

/**
 *
 * @author talghar
 */
public class CategoryRequest {

    private String id;
    private String name;
    private String dailyLimit;
    private String monthlyLimit;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDailyLimit() {
        return dailyLimit;
    }

    public String getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDailyLimit(String dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void setMonthlyLimit(String monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

}
