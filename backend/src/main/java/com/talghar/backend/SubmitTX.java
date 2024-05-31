/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.talghar.backend;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author talghar
 */
@RestController
public class SubmitTX {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    @RequestMapping(value = "/test")
    public static void main(String[] args) throws Exception {

        String connectionProfile = System.getProperty("user.home") + "/Projects/dip/idemix-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";

        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get(connectionProfile);
        System.out.println(networkConfigPath);
        Gateway.Builder builder = Gateway.createBuilder();

        builder.identity(wallet, "testUser19").networkConfig(networkConfigPath).discovery(true);
//
        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("zzz");
            System.out.println(network);
            Contract contract = network.getContract("basic");
            System.out.println(contract);

            byte[] result;
//            contract.submitTransaction("CreateAsset", "112", "yellow", "5", "Tom", "1300");
            contract.submitTransaction("InitLedger");
            result = contract.evaluateTransaction("GetAllAssets");

            System.out.println("\n\n\n");
            System.out.println(new String(result));
            System.out.println("\n\n\n");
        }

    }
}
