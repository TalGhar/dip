/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend.Controllers;

import com.sun.net.httpserver.Authenticator.Success;
import com.talghar.backend.Models.UserRequest;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Properties;
import java.util.Set;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.identity.IdemixEnrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author talghar
 */
@Controller
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/")
public class UserRegistrationController {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    @PostMapping("/admin/register")
    public ResponseEntity<?> register(@RequestBody UserRequest regReq) throws Exception {
        String enrollmentId = regReq.getUsername();
        String caCertPEM = new File(System.getProperty("user.dir")).getParentFile() + "/idemix-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";

        Properties props = new Properties();
        props.put("pemFile", caCertPEM);
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        if (wallet.get(enrollmentId) != null) {
            return new ResponseEntity<Error>(HttpStatus.CONFLICT);
        }

        X509Identity adminIdentity = (X509Identity) wallet.get("admin");

        User admin = new User() {

            @Override
            public String getName() {
                return "admin";
            }

            @Override
            public Set<String> getRoles() {
                return null;
            }

            @Override
            public String getAccount() {
                return null;
            }

            @Override
            public String getAffiliation() {
                return "org1.department1";
            }

            @Override
            public Enrollment getEnrollment() {
                return new Enrollment() {

                    @Override
                    public PrivateKey getKey() {
                        return adminIdentity.getPrivateKey();
                    }

                    @Override
                    public String getCert() {
                        return Identities.toPemString(adminIdentity.getCertificate());
                    }
                };
            }

            @Override
            public String getMspId() {
                return "Org1MSP";
            }

        };

        RegistrationRequest registrationRequest = new RegistrationRequest(enrollmentId);
        registrationRequest.setAffiliation("org1.department1");
        registrationRequest.setEnrollmentID(enrollmentId);
        registrationRequest.setSecret(regReq.getPassword());

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        String enrollmentSecret = caClient.register(registrationRequest, admin);
        Enrollment enrollment = caClient.enroll(enrollmentId, enrollmentSecret, enrollmentRequest);
        System.out.println(enrollment.hashCode());
        
        IdemixEnrollment idemixEnrollment = (IdemixEnrollment) caClient.idemixEnroll(enrollment, "Org1IdemixMSP");
        
        System.out.println("\nIdemix Enrollment IPK: " + idemixEnrollment.getIpk());
        System.out.println("\nIdemix enrollment MSP: " + idemixEnrollment.getMspId());
        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(enrollmentId, user);
        System.out.println("Successfully enrolled user " + enrollmentId + " and imported it into the wallet");
        
        return ResponseEntity.ok(regReq.getUsername());
    }

}
