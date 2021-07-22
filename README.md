# JWT RSA Signed Access Token Example

## Overview
This project aims to show how access tokens can be generated for a service and verified by a protected endpoint. This
example operates over HTTP but any implementations should operate over HTTPS. This is a proof of concept and example of 
how to issue and sign arbitrary keys for the creation of JWT tokens.

This was primarily designed as a demonstrator for generating API tokens that could be used by clients to access a service 
API and not as a primary means of securing web applications.

### Security
This is an example implementation and is not intended to be a complete (or fully secure) implementation. The
example covers, specifically, the following concerns:
1. Generating access tokens and secrets that can be verified cryptographically and without the original key.
2. Handling revocation of access tokens without the use of external infrastructure

This example implementation is designed to be similar to (but not the same as) the way that AWS and other providers handle 
access tokens.

It is important to understand that the way this example handles API key generation allows clients to essentially become
their own valid JWT issuers. It is up to the clients to securely store and manage their credentials. It is up to any 
implementation to add additional constraints so that signed JWTs cannot be reused. Using encrypted JWTS would mean that
no subject claim could be extracted to find the associated public key.

The reason not to use existing TLS, PKI, and x509 infrastructure to implement a mutual-TLS authentication mode is that
there can be a lot of issues managing and deploying the supporting infrastructure. This service is a stand-alone requiring
minimal infrastructure. To use mutual-TLS would require external connections to connect to the application server directly
or would require any load balancer, gateway, or router involved to be capable of passing along x509 information.

The client software is less complex as a result of not requiring TLS or PKI for access to the API endpoints. Simple HTTPs
can be  used which more developers will be comfortable with vs mutual-TLS which has its own pitfalls.

This example does not use a root CA to generate keys. The primary reason is that there is no need to build up a trust 
chain. Another reason is that there is no root CA to compromise and no risk of compromise during distribution. 

This example does not use the out-of-the-box Quarkus JWT security because there is no easy way to allow for arbitrary
keys to be used to verify a signature. 

### Practical Concerns
In order to create a more practical implementation several factors should be considered:
* The generation API should be an internal API invoked by an external facing service (user management page) on behalf of
  an authenticated user.
* The verification of a JWT could partially be handled by another internal API to externalize the logic and lookup of keys.
* Logic to support more JWT locations (cookie, header) should be considered.
* Implementation (for both clients and on the server side) of JWT expiration will be needed to prevent against replay attacks

## Using the Example

### Executing the Service
```
[]$ mvn clean package
[]$ java -jar target/quarkus-app/quarkus-run.jar
```

### Example Clients
Example clients are provided for the following languages:
* [Python](/clients/python/client.py)
* [NodeJS](/clients/nodejs/client.js)
* [Perl](/clients/perl/client.pl)
* [C#](/clients/csharp/Program.cs)
* [Go](/clients/go/client.go)
* [Java](/clients/java/src/main/java/io/github/chrisruffalo/example/jwt/client/Client.java)

Each of the example clients calls the access code generation endpoint to create a new access token and private key
response and then uses that response. First it makes a call to the `/api/check` endpoint that will be rejected with
"403 Forbidden" and then it makes another call with the correct JWT. In a production configuration clients would read
the access token and key from disk or another configuration source instead of immediately using them.