/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend.Controllers;

import com.talghar.backend.Models.UserRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.hyperledger.fabric.sdk.identity.IdemixEnrollmentSerialized;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author talghar
 */
@Tag(name = "Оператор", description = "Работа с пользователями и кошельками")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/admin")
public class AdminController {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    @PostMapping("/auth")
    public ResponseEntity<?> signin(@RequestBody UserRequest sigReq) throws IOException, EnrollmentException, InvalidArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, CryptoException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException {
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        if (wallet.get(sigReq.getUsername()) != null) {

            String caCertPEM = new File(System.getProperty("user.dir")).getParentFile() + "/idemix-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";
            Properties props = new Properties();
            props.put("pemFile", caCertPEM);
            props.put("allowAllHostNames", "true");
            HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            caClient.enroll(sigReq.getUsername(), sigReq.getPassword(), enrollmentRequestTLS);
            System.out.println("Succ");
            return ResponseEntity.ok(sigReq.getUsername());

        } else {
            return new ResponseEntity<Error>(HttpStatus.CONFLICT);

        }
    }

    @PostMapping("/register")
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("An identity for the user " + enrollmentId + " already exists in the wallet");

        }

        X509Identity adminIdentity = (X509Identity) wallet.get("admin");
        if (adminIdentity == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin needs to be enrolled and added to the wallet first");
        }
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
        registrationRequest.setSecret(regReq.getPassword());
        registrationRequest.setEnrollmentID(enrollmentId);
        caClient.register(registrationRequest, admin);
        Enrollment enrollment = caClient.enroll(enrollmentId, regReq.getPassword());

        IdemixEnrollmentSerialized idemixEnrollment = (IdemixEnrollmentSerialized) caClient.idemixEnrollAsString(enrollment, "Org1IdemixMSP");

        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(enrollmentId, user);

        Identity id = Identities.newIdemixIdentity(idemixEnrollment.getIpk(), idemixEnrollment.getRevocationPk(), idemixEnrollment.getMspId(), idemixEnrollment.getSk(), idemixEnrollment.getCred(), idemixEnrollment.getCri(), idemixEnrollment.getOu(), idemixEnrollment.getRoleMask());
        wallet.put(enrollmentId + "Idemix", id);
        return ResponseEntity.ok("User " + enrollmentId + " successfully enrolled and saved in the wallet");
    }
}
