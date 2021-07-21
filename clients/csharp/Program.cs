using System;
using System.IO;
using System.Net;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Security.Cryptography;

namespace csharp
{
    class Program
    {
        static void Main(string[] args)
        {
            // call generate endpoint
            WebRequest webRequest = WebRequest.Create("http://localhost:8080/generate");
            HttpWebResponse webResp = (HttpWebResponse)webRequest.GetResponse();

            // get response as json
            string json;
            using (var sr = new StreamReader(webResp.GetResponseStream()))
            {
                json = sr.ReadToEnd();
            }
            Dictionary<string, string> values = JsonConvert.DeserializeObject<Dictionary<string, string>>(json);

            // extract details
            string accessToken = values["accessToken"];
            string privateKey = values["privateKey"];

            // read private key https://vcsjones.dev/key-formats-dotnet-3/
            using var rsa = RSA.Create();
            rsa.ImportPkcs8PrivateKey(Convert.FromBase64String(privateKey), out _);

            // create JWT token
            var payload = new Dictionary<string, object>()
            {
                { "sub", accessToken },
            };
            string token=Jose.JWT.Encode(payload, rsa, Jose.JwsAlgorithm.RS256);

            webRequest = WebRequest.Create("http://localhost:8080/api/check");
            try {
                webResp = (HttpWebResponse)webRequest.GetResponse();
            } catch (WebException e) {
                Console.WriteLine("Error: " + e.Message);
            }

            webRequest = WebRequest.Create("http://localhost:8080/api/check");
            webRequest.Headers.Add("Authorization", "Bearer " + token);
            webResp = (HttpWebResponse)webRequest.GetResponse();
            Console.WriteLine("Status Code: " + webResp.StatusCode);
        }
    }
}
