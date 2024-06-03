///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.talghar.backend.Controllers;
//
//import com.talghar.backend.Models.UserRequest;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.nio.file.Paths;
//import java.util.Properties;
//
//import org.hyperledger.fabric.gateway.Wallet;
//import org.hyperledger.fabric.gateway.Wallets;
//
//import org.hyperledger.fabric.sdk.exception.CryptoException;
//import org.hyperledger.fabric.sdk.security.CryptoSuite;
//import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
//
//import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
//import org.hyperledger.fabric_ca.sdk.HFCAClient;
//import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
//import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// *
// * @author talghar
// */
//@Tag(name = "Оператор", description = "Работа с пользователями и кошельками")
//@RestController
////@CrossOrigin(origins = "http://localhost:3000")
////@RequestMapping("/api/admin")
//public class AdminAuthController {
//
//    static {
//        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
//    }
//
//    @PostMapping("/auth")
//    public ResponseEntity<?> signin(@RequestBody UserRequest sigReq) throws IOException, EnrollmentException, InvalidArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, CryptoException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException {
//        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
//        if (wallet.get(sigReq.getUsername()) != null) {
//
//            String caCertPEM = new File(System.getProperty("user.dir")).getParentFile() + "/idemix-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";
//            Properties props = new Properties();
//            props.put("pemFile", caCertPEM);
//            props.put("allowAllHostNames", "true");
//            HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
//            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
//            caClient.setCryptoSuite(cryptoSuite);
//            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
//            enrollmentRequestTLS.addHost("localhost");
//            caClient.enroll(sigReq.getUsername(), sigReq.getPassword(), enrollmentRequestTLS);
//            System.out.println("Succ");
//            return ResponseEntity.ok(sigReq.getUsername());
//
//        } else {
//            return new ResponseEntity<Error>(HttpStatus.CONFLICT);
//
//        }
//    }
//
//}
