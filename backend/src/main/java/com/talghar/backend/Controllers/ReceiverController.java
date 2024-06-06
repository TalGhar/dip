/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend.Controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.talghar.backend.Models.CategoryRequest;
import com.talghar.backend.Models.UserRequest;
import com.talghar.backend.Models.WalletRequest;
import com.talghar.backend.Utils.GJSonUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
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
@Tag(name = "Получатель перевода", description = "Создание кошельков, установка лимитов")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/receiver")
public class ReceiverController {

    private static final String CHANNEL_NAME = System.getenv().getOrDefault("CHANNEL_NAME", "zzzz");
    private static final String CHAINCODE_NAME = System.getenv().getOrDefault("CHAINCODE_NAME", "walletcc");
    GJSonUtil gjsonUtil = new GJSonUtil();
    private String username;

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    @PostMapping("/auth")
    public ResponseEntity<?> login(@RequestBody UserRequest authReq) throws IOException, EnrollmentException, InvalidArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, CryptoException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException {
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        if (wallet.get(authReq.getUsername()) + "Receiver" != null) {
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
                caClient.enroll(authReq.getUsername(), authReq.getPassword(), enrollmentRequestTLS);
                this.username = authReq.getUsername();
                return ResponseEntity.ok("Вы успешно вошли в систему");

            } catch (EnrollmentException | InvalidArgumentException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Неправильные данные для входа в систему");
            }

        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Неправильные данные для входа в систему");

        }
    }

    @GetMapping("/get-receiver-wallets")
    public ResponseEntity<?> getReceiverWallets() throws IOException, ContractException {
        String connectionProfile = System.getProperty("user.home") + "/Projects/dip/idemix-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";

        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        Path networkConfigPath = Paths.get(connectionProfile);
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, this.username + "Receiver").networkConfig(networkConfigPath).discovery(true);

        try (Gateway gateway = builder.connect()) {
            Network network = gateway.getNetwork(CHANNEL_NAME);
            Contract contract = network.getContract(CHAINCODE_NAME);

            var result = contract.evaluateTransaction("GetOwnerWallets", this.username);
            return new ResponseEntity<>(gjsonUtil.prettyJson(result), HttpStatus.OK);

        }
    }

    @PostMapping("create-wallet")
    public ResponseEntity<?> createWallet(@RequestBody WalletRequest wReq) throws IOException, ContractException {
        String connectionProfile = System.getProperty("user.home") + "/Projects/dip/idemix-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";
        String randomString = this.username + UUID.randomUUID().toString();
        String address = Base64.getEncoder().encodeToString(randomString.getBytes());
        String ballance = wReq.getBallance();
        String categoryId = wReq.getCategoryID();
        String ID = wReq.getID();

        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        Path networkConfigPath = Paths.get(connectionProfile);
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, this.username + "Receiver").networkConfig(networkConfigPath).discovery(true);

        try (Gateway gateway = builder.connect()) {

            Network network = gateway.getNetwork(CHANNEL_NAME);
            Contract contract = network.getContract(CHAINCODE_NAME);

            contract.submitTransaction("CreateWallet", address, ballance, categoryId, ID, this.username);
            return ResponseEntity.status(HttpStatus.OK).body("Кошелёк " + ID + " успешно создан");

        } catch (InterruptedException | TimeoutException | ContractException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }

    }

    @PostMapping("/update-category")
    public ResponseEntity<?> updateCategory(@RequestBody CategoryRequest catReq) throws IOException {
        String categoryName = catReq.getName();
        String categoryId = catReq.getId();
        String categoryDaily = catReq.getDailyLimit();
        String categoryMonthly = catReq.getMonthlyLimit();

        String connectionProfile = System.getProperty("user.home") + "/Projects/dip/idemix-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";

        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        Path networkConfigPath = Paths.get(connectionProfile);
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, this.username + "Receiver").networkConfig(networkConfigPath).discovery(true);

        try (Gateway gateway = builder.connect()) {

            Network network = gateway.getNetwork(CHANNEL_NAME);
            Contract contract = network.getContract(CHAINCODE_NAME);
            contract.submitTransaction("UpdateCategory", categoryId, categoryName, categoryDaily, categoryMonthly);
            return ResponseEntity.ok("Категория " + catReq.getId() + " успешно изменена");

        } catch (ContractException | TimeoutException | InterruptedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

}
