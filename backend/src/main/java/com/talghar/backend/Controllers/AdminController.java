/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend.Controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.talghar.backend.Models.CategoryRequest;
import com.talghar.backend.Models.ReceiverRequest;
import com.talghar.backend.Models.UserRequest;
import com.talghar.backend.Utils.GJSonUtil;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.X509Identity;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.identity.IdemixEnrollmentSerialized;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.Attribute;

import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author talghar
 */
@Tag(name = "Оператор", description = "Работа с пользователями, получателями переводов и категориями кошельков")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/admin")
public class AdminController {

    private static final String CHANNEL_NAME = System.getenv().getOrDefault("CHANNEL_NAME", "zzzz");
    private static final String CHAINCODE_NAME = System.getenv().getOrDefault("CHAINCODE_NAME", "walletcc");

    GJSonUtil gjsonUtil = new GJSonUtil();

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
            try {
                caClient.enroll(sigReq.getUsername(), sigReq.getPassword(), enrollmentRequestTLS);
                return ResponseEntity.ok("Вы успешно вошли в систему в роли оператора");

            } catch (EnrollmentException | InvalidArgumentException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Неправильные данные для входа в систему в роли оператора");
            }

        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Неправильные данные для входа в систему в роли оператора");

        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequest reqReq) throws Exception {
        String enrollmentId = reqReq.getUsername();
        String caCertPEM = new File(System.getProperty("user.dir")).getParentFile() + "/idemix-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";

        Properties props = new Properties();
        props.put("pemFile", caCertPEM);
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        if (wallet.get(enrollmentId) != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Пользователь " + enrollmentId + " уже зарегистрирован в системе");

        }

        X509Identity adminIdentity = (X509Identity) wallet.get("admin");
        if (adminIdentity == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Оператор системы должен авторизоваться в системе");
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
        registrationRequest.setSecret(reqReq.getPassword());
        registrationRequest.setEnrollmentID(enrollmentId);

        caClient.register(registrationRequest, admin);

        Enrollment enrollment = caClient.enroll(enrollmentId, reqReq.getPassword());

        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(enrollmentId, user);

        IdemixEnrollmentSerialized idemixEnrollment = (IdemixEnrollmentSerialized) caClient.idemixEnrollAsString(enrollment, "Org1IdemixMSP");

        String idemixIPK = idemixEnrollment.getIpk();
        String idemixRPK = idemixEnrollment.getRevocationPk();
        String idemixMSP = idemixEnrollment.getMspId();
        String idemixSecret = idemixEnrollment.getSk();
        String idemixCred = idemixEnrollment.getCred();
        String idemixCri = idemixEnrollment.getCri();
        String idemixOu = idemixEnrollment.getOu();
        String idemixRoleMask = idemixEnrollment.getRoleMask();

        Identity id = Identities.newIdemixIdentity(idemixIPK, idemixRPK, idemixMSP, idemixSecret, idemixCred, idemixCri, idemixOu, idemixRoleMask);
        wallet.put(enrollmentId + "Idemix", id);
        return ResponseEntity.ok("Пользователь " + enrollmentId + " успешно зарегистрирован в системе");
    }

    @PostMapping("/register-receiver")
    public ResponseEntity<?> registerReceiver(@RequestBody ReceiverRequest recReq) throws Exception {
        String regex = "^rec[A-Za-z0-9_]{4,20}$";
        if (!recReq.getUsername().matches(regex)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Имя получателя перевода должно начинаться с rec");
        }
        String enrollmentId = recReq.getUsername();
        String caCertPEM = new File(System.getProperty("user.dir")).getParentFile() + "/idemix-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";

        Properties props = new Properties();
        props.put("pemFile", caCertPEM);
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        if (wallet.get(enrollmentId + "Receiver") != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Получатель перевода " + enrollmentId + " уже зарегистрирован в системе");

        }

        X509Identity adminIdentity = (X509Identity) wallet.get("admin");
        if (adminIdentity == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Оператор системы должен авторизоваться в системе");
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
        registrationRequest.setSecret(recReq.getPassword());
        registrationRequest.setEnrollmentID(enrollmentId);

        Attribute formAttr = new Attribute("FORM", recReq.getForm());
        registrationRequest.addAttribute(formAttr);

        caClient.register(registrationRequest, admin);

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.addHost("localhost");
        enrollmentRequest.addAttrReq("FORM");

        Enrollment enrollment = caClient.enroll(enrollmentId, recReq.getPassword(), enrollmentRequest);

        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(enrollmentId + "Receiver", user);
        return ResponseEntity.ok("Получатель перевода " + enrollmentId + " успешно зарегистрирован в системе");
    }

    @PostMapping("/create-category")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest catReq) throws ContractException, TimeoutException, InterruptedException, IOException {
        String categoryName = catReq.getName();
        String categoryId = catReq.getId();
        String categoryDaily = catReq.getDailyLimit();
        String categoryMonthly = catReq.getMonthlyLimit();

        String connectionProfile = System.getProperty("user.home") + "/Projects/dip/idemix-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";

        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        Path networkConfigPath = Paths.get(connectionProfile);
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "admin").networkConfig(networkConfigPath).discovery(true);

        try (Gateway gateway = builder.connect()) {

            Network network = gateway.getNetwork(CHANNEL_NAME);
            Contract contract = network.getContract(CHAINCODE_NAME);

            contract.submitTransaction("CreateCategory", categoryId, categoryName, categoryDaily, categoryMonthly);
            return ResponseEntity.ok("Категория " + catReq.getId() + " успешно создана");

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Данная категория кошелька уже существует в системе");
        }
    }

    @GetMapping("/get-categories")
    public ResponseEntity<?> getCategories() throws IOException {
        String connectionProfile = System.getProperty("user.home") + "/Projects/dip/idemix-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";

        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        Path networkConfigPath = Paths.get(connectionProfile);
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "admin").networkConfig(networkConfigPath).discovery(true);

        try (Gateway gateway = builder.connect()) {

            Network network = gateway.getNetwork(CHANNEL_NAME);
            Contract contract = network.getContract(CHAINCODE_NAME);

            var result = contract.evaluateTransaction("GetAllCategories");
            return new ResponseEntity<>(gjsonUtil.prettyJson(result), HttpStatus.OK);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Список категорий пуст");
        }
    }

    @PostMapping("/get-owner-wallets")
    public ResponseEntity<?> getOwnerWallets(@RequestBody String owner) throws IOException, ContractException {
        String connectionProfile = System.getProperty("user.home") + "/Projects/dip/idemix-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";

        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        Path networkConfigPath = Paths.get(connectionProfile);
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "admin").networkConfig(networkConfigPath).discovery(true);

        try (Gateway gateway = builder.connect()) {

            Network network = gateway.getNetwork(CHANNEL_NAME);
            Contract contract = network.getContract(CHAINCODE_NAME);

            var result = contract.evaluateTransaction("GetOwnerWallets", owner);
            return new ResponseEntity<>(gjsonUtil.prettyJson(result), HttpStatus.OK);

        }

    }

    @GetMapping("/get-all-users")
    public ResponseEntity<?> getAllUsers() throws IOException {
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        Set<String> users = wallet.list();
        if (users.isEmpty()) {
            return new ResponseEntity<>("Список пользователей пуст", HttpStatus.CONFLICT);
        }
        Set<String> formattedUsers = new HashSet<>();
        for (String user : users) {

            if (user.contains("Idemix")) {
                user = user.substring(0, user.lastIndexOf("Idemix"));
                formattedUsers.add(user);
            }
        }
        return new ResponseEntity<>(formattedUsers, HttpStatus.OK);
    }

    @GetMapping("/get-all-receivers")
    public ResponseEntity<?> getAllReceivers() throws IOException {
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        Set<String> users = wallet.list();
        if (users.isEmpty()) {
            return new ResponseEntity<>("Список получателей переводов пуст", HttpStatus.CONFLICT);
        }
        Set<String> formattedUsers = new HashSet<>();
        for (String user : users) {
            if (user.contains("Receiver")) {
                user = user.substring(0, user.lastIndexOf("Receiver"));
                formattedUsers.add(user);
            }
        }
        return new ResponseEntity<>(formattedUsers, HttpStatus.OK);
    }

}
