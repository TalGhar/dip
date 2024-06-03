///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.talghar.backend.Controllers;
//
//import com.talghar.backend.Models.UserRequest;
//
//import java.io.File;
//import java.nio.file.Paths;
//import java.security.PrivateKey;
//import java.util.Properties;
//import java.util.Set;
//
//import org.hyperledger.fabric.gateway.Identities;
//import org.hyperledger.fabric.gateway.Identity;
//import org.hyperledger.fabric.gateway.Wallet;
//import org.hyperledger.fabric.gateway.Wallets;
//import org.hyperledger.fabric.gateway.X509Identity;
//
//import org.hyperledger.fabric.sdk.Enrollment;
//import org.hyperledger.fabric.sdk.User;
//import org.hyperledger.fabric.sdk.identity.IdemixEnrollmentSerialized;
//import org.hyperledger.fabric.sdk.security.CryptoSuite;
//import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
//
//import org.hyperledger.fabric_ca.sdk.HFCAClient;
//import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// *
// * @author talghar
// */
//@RestController
//@CrossOrigin(origins = "http://localhost:3000")
//@RequestMapping("/api/admin")
//public class UserRegistrationController {
//
//    static {
//        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody UserRequest regReq) throws Exception {
//        String enrollmentId = regReq.getUsername();
//        String caCertPEM = new File(System.getProperty("user.dir")).getParentFile() + "/idemix-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";
//
//        Properties props = new Properties();
//        props.put("pemFile", caCertPEM);
//        props.put("allowAllHostNames", "true");
//        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
//        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
//        caClient.setCryptoSuite(cryptoSuite);
//
//        // Create a wallet for managing identities
//        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
//
//        // Check to see if we've already enrolled the user.
//        if (wallet.get(enrollmentId) != null) {
//            System.out.println("An identity for the user " + enrollmentId + " already exists in the wallet");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("An identity for the user " + enrollmentId + " already exists in the wallet");
//
//        }
//
//        X509Identity adminIdentity = (X509Identity) wallet.get("admin");
//        if (adminIdentity == null) {
//            System.out.println("\"admin\" needs to be enrolled and added to the wallet first");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin needs to be enrolled and added to the wallet first");
//        }
//        User admin = new User() {
//
//            @Override
//            public String getName() {
//                return "admin";
//            }
//
//            @Override
//            public Set<String> getRoles() {
//                return null;
//            }
//
//            @Override
//            public String getAccount() {
//                return null;
//            }
//
//            @Override
//            public String getAffiliation() {
//                return "org1.department1";
//            }
//
//            @Override
//            public Enrollment getEnrollment() {
//                return new Enrollment() {
//
//                    @Override
//                    public PrivateKey getKey() {
//                        return adminIdentity.getPrivateKey();
//                    }
//
//                    @Override
//                    public String getCert() {
//                        return Identities.toPemString(adminIdentity.getCertificate());
//                    }
//                };
//            }
//
//            @Override
//            public String getMspId() {
//                return "Org1MSP";
//            }
//
//        };
//
//        // Register the user, enroll the user, and import the new identity into the wallet.
//        RegistrationRequest registrationRequest = new RegistrationRequest(enrollmentId);
//        registrationRequest.setAffiliation("org1.department1");
//        registrationRequest.setEnrollmentID(enrollmentId);
//        String enrollmentSecret = caClient.register(registrationRequest, admin);
//        Enrollment enrollment = caClient.enroll(enrollmentId, enrollmentSecret);
//
//        IdemixEnrollmentSerialized idemixEnrollment = (IdemixEnrollmentSerialized) caClient.idemixEnrollAsString(enrollment, "Org1IdemixMSP");
//        System.out.println("\nIdemix Enrollment IPK: " + idemixEnrollment.getIpk());
//        System.out.println("\nIdemix enrollment MSP: " + idemixEnrollment.getMspId());
//
//        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
//        wallet.put(enrollmentId, user);
//
//        Identity id = Identities.newIdemixIdentity(idemixEnrollment.getIpk(), idemixEnrollment.getRevocationPk(), idemixEnrollment.getMspId(), idemixEnrollment.getSk(), idemixEnrollment.getCred(), idemixEnrollment.getCri(), idemixEnrollment.getOu(), idemixEnrollment.getRoleMask());
//        wallet.put(enrollmentId + "Idemix", id);
//        System.out.println("Successfully enrolled user " + enrollmentId + " and imported it into the wallet");
//        return ResponseEntity.ok("User " + enrollmentId + " successfully enrolled and saved in the wallet");
//
//    }
//
//}
