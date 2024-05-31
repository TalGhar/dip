/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend;

import org.hyperledger.fabric.protos.idemix.Idemix;
import org.hyperledger.fabric.sdk.identity.IdemixEnrollment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 *
 * @author talghar
 */
@RestController
public class SubmitTXUsingIdemix {

    @RequestMapping(value = "/enr")
    public static void main(String args[]) throws Exception {
        IdemixEnrollment test;
    }

}
