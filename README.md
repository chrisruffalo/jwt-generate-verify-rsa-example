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

It is important to understand that the way this example handles API key generation allows clients to essentially become
their own valid JWT issuers. It is up to the clients to securely store and manage their credentials. It is up to any 
implementation to add additional constraints so that signed JWTs cannot be reused.

The reason not to use existing TLS, PKI, and x509 infrastructure to implement a mutual-TLS authentication mode is that
there can be a lot of issues managing and deploying the supporting infrastructure. This service is a stand-alone requiring
minimal infrastructure. To use mutual-TLS would require external connections to connect to the application server directly
or would require any load balancer, gateway, or router involved to be capable of passing along x509 information.

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

## Using Example

### Executing the Service
```
[]$ mvn clean package
[]$ java -jar target/quarkus-app/quarkus-run.jar
```

### Example Clients
For example (minimal) clients see the following resources:
* [Python](/clients/python/client.py)
* [NodeJS](/clients/nodejs/client.js)

### Manually Calling the Endpoints
Call one of the protected services without an access token:
```
[]$ curl localhost:8080/api/check -v
*   Trying 127.0.0.1:8080...
* TCP_NODELAY set
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /api/check HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.65.3
> Accept: */*
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 403 Forbidden
< Content-Length: 0
<
* Connection #0 to host localhost left intact
```
The protected endpoints deny access by default. They require a JWT token providing bearer authorization in order to allow access
to the end point.

First an API access token and private key needs to be generated:
```bash
[]$ curl localhost:8080/generate
{"accessToken":"uI_BMTDZwMwV7XxCpk-u40KypNMDeFuCETeWtZdGJvumzOHA2m84J-Kld1MxAw_uD-20YMJFCsN-F-OED4goqw","privateKey":"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCRg00HOP2dps2hD0f41KMcIpTUGsQE9T88rfag9j2+e1Ba7kQAqSDKOHBsAqIkquKuEFGuC4cW/f25Mja6b6Cff/Rh/yLlhzBJPik2tVT3KsdBBicqsWIMD7JNiFOqTyDn9hFD8mqsmTRkA1kIN9nWB0O5x7GbW8eGrY+rUtr5QkcitwZ+33gowY2+7Eg
5/pM9/wrJ2QhwUYS9aO3OVLBc1mjnzlZj4paRtikrFfmvJ2rqHmOpvp9YthmrRTDWeBRLTI79q8xEILkWvoIH8OPnpm2CXySiaI5aOimLpPQOqmvgmrdIlMPum2kMq7LdyVFRO5OGHvcZYXgKEqUPerL3AgMBAAECggEAHjJBKd6Jws16npGtPdXig1Qpzlo/k0fRd6YiKATxizXL1xLaychPJafQrJlfTHypzbVVPejmf771iBkDgmznyDGlZTCs7TTAsHJV2ySGHUSjVLsSJanpztbzCWPg+Nay3XAS5SbJPEgOtzJ6p1nvupqHU2Y9VSAWp7wctFDdYBwww2L282GonBk6gJZPSClT
/H80Kmd2K8iWdbJKbfYWzxCTogIgr9FQDALPZNywxTNShfN5Fxz7JqJ8ULAzDrtNTseX3kz5qab6Jkh+RHfx2/mSvdwwLdE2+TIaOvzvBCKfLtiWJgPodBybgK3HMz3dehoKTaZCE4iYJrXULlf9AQKBgQDGrvGg+zA8GiDVNHrXDE07ujQSxVE0ob0GnhOWGGD3i9e0N3jmvVWEv1cs9MCRbjY4/Xa8keLM6022kr4xwcMof32xM6m4LvmH4hv+UHuuPMevMKWPWpVzaUlKxqEW7DIuu7UviLaiTN2FF9wn+mCUJYBt6U770lsdbX5B4gDIiwKBgQC7faPFeGEBEZrfz2Wy3R3WgGt7w
IwIc6XrLjo7/23o6FkCCuEhm/jkshdkRl6jP6kRhiRbmY0sHCJ1aGeaghCNo6jLAVVau/4pIBrmQi7qmcn8hdk+WnipgLt/WfZseXqV9qucQXxHIL4y6mVhTohZVNnItJhE4aDwTzcebk0gxQKBgQCF5QQFnwJEnr8dr75RCoNKCxRoyf0N4SnIOeOtNUSzztRRKUkbBuGJEoGnVFIqMAHuqjHIpvAXdUPsFDyEv7XLpw+Hye9Ipq+XOXPwEUEojOFtWPVaBIvPOVchQ3bwQcEX6XwTSqj5+58VwJynfH51mEhSyfZmkr7AuDdsIuiwPQKBgG+R4V4GN3tiVY4vpa4dZL8bZlqbBvmUkD
x2ItNHOclqUmUjwjq0zRSSYdcbBQASRvKVp5cWtep0x5CkU1qfYWhX5n7/SSKYUjN41mkFI1QZthfeMpunTLxZTboH99svIuKQiiiO03ykIGq+DxwrlnnKQ1rrFN2QgqveB8fFDYKdAoGAGEAHGafw7b0CQSw5Nl4XmQOHM3yd32yeQ1ixhtPFnwloooy7PP/ZfTL9tqRYbDVEQ9wL1+3zBm8nwJUkZTz686++qMPfTGp6dZmqFqU/tLZDiy3mTMhYF5W/0UNXl/7tFXVv/BaHYBxsbYCMNps3BCv/zTNJ+jHLJh7kOkTYbKI="}
```

This generates the access token `uI_BMTDZwMwV7XxCpk-u40KypNMDeFuCETeWtZdGJvumzOHA2m84J-Kld1MxAw_uD-20YMJFCsN-F-OED4goqw` 
which also has an associated private key:
```
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCRg00HOP2dps2hD0f41KMcIpTUGsQE9T88rfag9j2+e1Ba7kQAqSDKOHBsAqIkquKuEFGuC4cW/f25Mja6b6Cff/Rh/yLlhzBJPik2tVT3KsdBBicqsWIMD7JNiFOqTyDn9hFD8mqsmTRkA1kIN9nWB0O5x7GbW8eGrY+rUtr5QkcitwZ+33gowY2+7Eg
5/pM9/wrJ2QhwUYS9aO3OVLBc1mjnzlZj4paRtikrFfmvJ2rqHmOpvp9YthmrRTDWeBRLTI79q8xEILkWvoIH8OPnpm2CXySiaI5aOimLpPQOqmvgmrdIlMPum2kMq7LdyVFRO5OGHvcZYXgKEqUPerL3AgMBAAECggEAHjJBKd6Jws16npGtPdXig1Qpzlo/k0fRd6YiKATxizXL1xLaychPJafQrJlfTHypzbVVPejmf771iBkDgmznyDGlZTCs7TTAsHJV2ySGHUSjVLsSJanpztbzCWPg+Nay3XAS5SbJPEgOtzJ6p1nvupqHU2Y9VSAWp7wctFDdYBwww2L282GonBk6gJZPSClT
/H80Kmd2K8iWdbJKbfYWzxCTogIgr9FQDALPZNywxTNShfN5Fxz7JqJ8ULAzDrtNTseX3kz5qab6Jkh+RHfx2/mSvdwwLdE2+TIaOvzvBCKfLtiWJgPodBybgK3HMz3dehoKTaZCE4iYJrXULlf9AQKBgQDGrvGg+zA8GiDVNHrXDE07ujQSxVE0ob0GnhOWGGD3i9e0N3jmvVWEv1cs9MCRbjY4/Xa8keLM6022kr4xwcMof32xM6m4LvmH4hv+UHuuPMevMKWPWpVzaUlKxqEW7DIuu7UviLaiTN2FF9wn+mCUJYBt6U770lsdbX5B4gDIiwKBgQC7faPFeGEBEZrfz2Wy3R3WgGt7w
IwIc6XrLjo7/23o6FkCCuEhm/jkshdkRl6jP6kRhiRbmY0sHCJ1aGeaghCNo6jLAVVau/4pIBrmQi7qmcn8hdk+WnipgLt/WfZseXqV9qucQXxHIL4y6mVhTohZVNnItJhE4aDwTzcebk0gxQKBgQCF5QQFnwJEnr8dr75RCoNKCxRoyf0N4SnIOeOtNUSzztRRKUkbBuGJEoGnVFIqMAHuqjHIpvAXdUPsFDyEv7XLpw+Hye9Ipq+XOXPwEUEojOFtWPVaBIvPOVchQ3bwQcEX6XwTSqj5+58VwJynfH51mEhSyfZmkr7AuDdsIuiwPQKBgG+R4V4GN3tiVY4vpa4dZL8bZlqbBvmUkD
x2ItNHOclqUmUjwjq0zRSSYdcbBQASRvKVp5cWtep0x5CkU1qfYWhX5n7/SSKYUjN41mkFI1QZthfeMpunTLxZTboH99svIuKQiiiO03ykIGq+DxwrlnnKQ1rrFN2QgqveB8fFDYKdAoGAGEAHGafw7b0CQSw5Nl4XmQOHM3yd32yeQ1ixhtPFnwloooy7PP/ZfTL9tqRYbDVEQ9wL1+3zBm8nwJUkZTz686++qMPfTGp6dZmqFqU/tLZDiy3mTMhYF5W/0UNXl/7tFXVv/BaHYBxsbYCMNps3BCv/zTNJ+jHLJh7kOkTYbKI=
```

The token can be crafted into an authorization:
```bash
# create the header that describes the signature algorithm
[]$ HEADER=$(echo -n '{"alg":"RS256","typ":"JWT"}' | base64 -w0 | sed s/\+/-/g | sed 's/\//_/g' | sed -E s/=+$//)
# create the encoded body with the subject (you could add an exp claim here as well)
[]$ BODY=$(echo -n '{"sub":"uI_BMTDZwMwV7XxCpk-u40KypNMDeFuCETeWtZdGJvumzOHA2m84J-Kld1MxAw_uD-20YMJFCsN-F-OED4goqw"}' | base64 -w0 | sed s/\+/-/g |sed 's/\//_/g' |  sed -E s/=+$//)
# create the PEM encoded key 
[]$ KEY=$(echo -e "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCRg00HOP2dps2hD0f41KMcIpTUGsQE9T88rfag9j2+e1Ba7kQAqSDKOHBsAqIkquKuEFGuC4cW/f25Mja6b6Cff/Rh/yLlhzBJPik2tVT3KsdBBicqsWIMD7JNiFOqTyDn9hFD8mqsmTRkA1kIN9nWB0O5x7GbW8eGrY+rUtr5QkcitwZ+33gowY2+7Eg
5/pM9/wrJ2QhwUYS9aO3OVLBc1mjnzlZj4paRtikrFfmvJ2rqHmOpvp9YthmrRTDWeBRLTI79q8xEILkWvoIH8OPnpm2CXySiaI5aOimLpPQOqmvgmrdIlMPum2kMq7LdyVFRO5OGHvcZYXgKEqUPerL3AgMBAAECggEAHjJBKd6Jws16npGtPdXig1Qpzlo/k0fRd6YiKATxizXL1xLaychPJafQrJlfTHypzbVVPejmf771iBkDgmznyDGlZTCs7TTAsHJV2ySGHUSjVLsSJanpztbzCWPg+Nay3XAS5SbJPEgOtzJ6p1nvupqHU2Y9VSAWp7wctFDdYBwww2L282GonBk6gJZPSClT
/H80Kmd2K8iWdbJKbfYWzxCTogIgr9FQDALPZNywxTNShfN5Fxz7JqJ8ULAzDrtNTseX3kz5qab6Jkh+RHfx2/mSvdwwLdE2+TIaOvzvBCKfLtiWJgPodBybgK3HMz3dehoKTaZCE4iYJrXULlf9AQKBgQDGrvGg+zA8GiDVNHrXDE07ujQSxVE0ob0GnhOWGGD3i9e0N3jmvVWEv1cs9MCRbjY4/Xa8keLM6022kr4xwcMof32xM6m4LvmH4hv+UHuuPMevMKWPWpVzaUlKxqEW7DIuu7UviLaiTN2FF9wn+mCUJYBt6U770lsdbX5B4gDIiwKBgQC7faPFeGEBEZrfz2Wy3R3WgGt7w
IwIc6XrLjo7/23o6FkCCuEhm/jkshdkRl6jP6kRhiRbmY0sHCJ1aGeaghCNo6jLAVVau/4pIBrmQi7qmcn8hdk+WnipgLt/WfZseXqV9qucQXxHIL4y6mVhTohZVNnItJhE4aDwTzcebk0gxQKBgQCF5QQFnwJEnr8dr75RCoNKCxRoyf0N4SnIOeOtNUSzztRRKUkbBuGJEoGnVFIqMAHuqjHIpvAXdUPsFDyEv7XLpw+Hye9Ipq+XOXPwEUEojOFtWPVaBIvPOVchQ3bwQcEX6XwTSqj5+58VwJynfH51mEhSyfZmkr7AuDdsIuiwPQKBgG+R4V4GN3tiVY4vpa4dZL8bZlqbBvmUkD
x2ItNHOclqUmUjwjq0zRSSYdcbBQASRvKVp5cWtep0x5CkU1qfYWhX5n7/SSKYUjN41mkFI1QZthfeMpunTLxZTboH99svIuKQiiiO03ykIGq+DxwrlnnKQ1rrFN2QgqveB8fFDYKdAoGAGEAHGafw7b0CQSw5Nl4XmQOHM3yd32yeQ1ixhtPFnwloooy7PP/ZfTL9tqRYbDVEQ9wL1+3zBm8nwJUkZTz686++qMPfTGp6dZmqFqU/tLZDiy3mTMhYF5W/0UNXl/7tFXVv/BaHYBxsbYCMNps3BCv/zTNJ+jHLJh7kOkTYbKI=" | fold -w 64)
[]$ FULLKEY=$(echo -ne "-----BEGIN PRIVATE KEY-----\n${KEY}\n-----END PRIVATE KEY-----" | openssl rsa )
# sign the header and payload with the key
[]$ SIGNATURE=$(openssl dgst -sha256 -sign <(printf "%s\n" "${FULLKEY}") <(echo -n "${HEADER}.${PAYLOAD}") | base64 -w0 | sed s/\+/-/g | sed 's/\//_/g' | sed -E s/=+$//)
# create the JWT
[]$ JWT="${HEADER}.${BODY}.${SIGNATURE}"
# call the endpoint with curl
[]$ curl -v -H "Authorization: Bearer ${JWT}" localhost:8080/api/check
```
