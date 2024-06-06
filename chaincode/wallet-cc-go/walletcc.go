package main

import (
	"encoding/json"
	"fmt"
	"log"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SmartContract struct {
	contractapi.Contract
}

type Wallet struct {
	Address    string `json:"Address"`
	Ballance   int    `json:"Ballance"`
	CategoryID string `json:"CategoryID"`
	ID         string `json:"ID"`
	Owner      string `json:"Owner"`
}

type Category struct {
	ID           string `json:"ID"`
	Name         string `json:"Name"`
	DailyLimit   int    `json:"DailyLimit"`
	MonthlyLimit int    `json:"MonthlyLimit"`
}

func (s *SmartContract) CreateWallet(ctx contractapi.TransactionContextInterface, address string, ballance int, categoryID string, id string, owner string) error {
	exists, err := s.WalletExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("кошелёк %s уже существует", id)
	}
	catExists, catErr := s.CategoryExists(ctx, categoryID)
	if !catExists || catErr != nil {
		return fmt.Errorf("категории %s не существует", categoryID)
	}

	wallet := Wallet{
		Address:    address,
		Ballance:   ballance,
		CategoryID: categoryID,
		ID:         id,
		Owner:      owner,
	}
	walletJSON, err := json.Marshal(wallet)
	if err != nil {
		return err
	}
	return ctx.GetStub().PutState(id, walletJSON)

}

func (s *SmartContract) WalletExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	walletJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("не удалось найти кошелёк в леджере: %v", err)
	}
	return walletJSON != nil, nil
}

func (s *SmartContract) CreateCategory(ctx contractapi.TransactionContextInterface, id string, name string, daily int, monthly int) error {
	exists, err := s.CategoryExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("категория кошелька %s уже существует", id)
	}
	category := Category{
		ID:           id,
		Name:         name,
		DailyLimit:   daily,
		MonthlyLimit: monthly,
	}
	categoryJSON, err := json.Marshal(category)
	if err != nil {
		return err
	}

	return ctx.GetStub().PutState(id, categoryJSON)
}

func (s *SmartContract) CategoryExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	categoryJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("не удалось найти категорию в леджере: %v", err)
	}
	return categoryJSON != nil, nil
}

func (s *SmartContract) GetAllCategories(ctx contractapi.TransactionContextInterface) ([]*Category, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()
	var categories []*Category
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		var category Category
		err = json.Unmarshal(queryResponse.Value, &category)
		if err != nil {
			return nil, err
		}
		categories = append(categories, &category)
	}
	return categories, nil
}

func (s *SmartContract) GetAllWallets(ctx contractapi.TransactionContextInterface) ([]*Wallet, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()
	var wallets []*Wallet
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		var wallet Wallet
		err = json.Unmarshal(queryResponse.Value, &wallet)
		if err != nil {
			return nil, err
		}
		wallets = append(wallets, &wallet)
	}
	return wallets, nil
}

func (s *SmartContract) GetOwnerWallets(ctx contractapi.TransactionContextInterface, owner string) ([]*Wallet, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()
	var wallets []*Wallet
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		var wallet Wallet
		err = json.Unmarshal(queryResponse.Value, &wallet)
		if err != nil {
			return nil, err
		}
		if wallet.Owner == owner {
			wallets = append(wallets, &wallet)
		}
	}
	return wallets, nil
}

func (s *SmartContract) UpdateCategory(ctx contractapi.TransactionContextInterface, id string, name string, daily int, monthly int) error {
	exists, err := s.CategoryExists(ctx, id)
	if err != nil {
		return err
	}
	if !exists {
		return fmt.Errorf("категории %s не существует", id)
	}
	category := Category{
		ID:           id,
		Name:         name,
		DailyLimit:   daily,
		MonthlyLimit: monthly,
	}
	categoryJSON, err := json.Marshal(category)
	if err != nil {
		return err
	}
	return ctx.GetStub().PutState(id, categoryJSON)
}

func main() {
	walletChaincode, err := contractapi.NewChaincode(&SmartContract{})
	if err != nil {
		log.Panicf("Ошибка при создании смартконтракта: %v", err)
	}
	if err := walletChaincode.Start(); err != nil {
		log.Panicf("Ошибка при запуске смартконтракта: %v", err)
	}
}
