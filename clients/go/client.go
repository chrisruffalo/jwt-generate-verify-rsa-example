package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"github.com/go-jose/go-jose/jose-util/generator"
	"github.com/go-jose/go-jose/v3"
	"github.com/go-jose/go-jose/v3/jwt"
	"net/http"
	"time"
)

// shared http client for all instances
var httpClient = &http.Client{Timeout: 2 * time.Second}

func main() {
	r, err := httpClient.Get("http://localhost:8080/generate")
	if err != nil {
		return
	}
	defer r.Body.Close()

	// Declared an empty map interface
	var result map[string]interface{}
	json.NewDecoder(r.Body).Decode(&result)

	// this is a quick way to get these as a string
	accessToken := fmt.Sprintf("%s", result["accessToken"])
	privateKey := fmt.Sprintf("%s", result["privateKey"])

	// decode the private key
	privateKeyDecoded, err := base64.StdEncoding.DecodeString(privateKey)
	if err != nil {
		fmt.Printf("Error decoding private key %s\n", err)
		return
	}

	// read the private key into an instance
	privateKeyInstance, err := generator.LoadPrivateKey(privateKeyDecoded)
	if err != nil {
		fmt.Printf("Error loading private key: %s\n", err)
		return
	}

	// sign token with the key and serialize out to string
	signer, err := jose.NewSigner(jose.SigningKey{Algorithm:  jose.RS256, Key: privateKeyInstance}, nil)
	token, err := jwt.Signed(signer).Claims(&jwt.Claims{
		Subject: accessToken,
	}).CompactSerialize()
	if err != nil {
		fmt.Printf("Error signing jwt token: %s\n", err)
		return
	}

	// call and get status code for unauthorized
	req, _ := http.NewRequest("GET", "http://localhost:8080/api/check", nil)
	r, err = httpClient.Do(req)
	defer r.Body.Close()
	fmt.Printf("Status: %s\n", r.Status)

	// call and get status code for authorized
	req, _ = http.NewRequest("GET", "http://localhost:8080/api/check", nil)
	req.Header.Set("Authorization", "Bearer " + token)
	r, err = httpClient.Do(req)
	defer r.Body.Close()
	fmt.Printf("Status: %s\n", r.Status)
}

