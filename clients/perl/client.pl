#!/usr/bin/perl

use HTTP::Request;
use LWP::UserAgent;
use JSON;
use Crypt::JWT qw(encode_jwt);
use Crypt::PK::RSA;
use Crypt::OpenSSL::RSA;

# make response
my $ua = LWP::UserAgent->new();
my $request = HTTP::Request->new('GET', "http://localhost:8080/generate");
my $response = $ua->request($request);
my $content = $response->decoded_content;

# create json from response and get values
my $json = decode_json $content;
my $accessToken = $json->{accessToken};
my $privateKey = $json->{privateKey};

# create signed jwt (https://stackoverflow.com/a/65031751)
my $key = Crypt::OpenSSL::RSA->new_private_key("-----BEGIN PRIVATE KEY-----\n$privateKey\n-----END PRIVATE KEY-----\n");
my $payload = {
    sub => $accessToken,
};
my $alg = "RS256";
my $token = encode_jwt(payload => $payload, key => $key, alg => $alg);

# make call to service without bearer authorization header
$request = HTTP::Request->new('GET', "http://localhost:8080/api/check");
$response = $ua->request($request);
my $status = $response->status_line;
print "Status: $status\n";

# make the call with the bearer header
$request = HTTP::Request->new(
    GET => "http://localhost:8080/api/check",
    HTTP::Headers->new(Authorization => "Bearer $token")
);
$response = $ua->request($request);
my $status = $response->status_line;
print "Status: $status\n";