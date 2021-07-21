const jwt = require('jsonwebtoken'),
      http = require('http')

statusCallback = function(response) {
    console.log("Status code: " + response.statusCode)
}

generateCallback = function(response) {
    let str = '';

    //another chunk of data has been received, so append it to `str`
    response.on('data', function (chunk) {
        str += chunk;
    });

    //the whole response has been received, so we just print it out here
    response.on('end', function () {
        // create json from str
        let jsonObject = JSON.parse(str);
        let accessToken = jsonObject.accessToken;
        let privateKey = "-----BEGIN PRIVATE KEY-----\n" + jsonObject.privateKey + "\n-----END PRIVATE KEY-----";

        // create jwt (with help from https://siddharthac6.medium.com/json-web-token-jwt-the-right-way-of-implementing-with-node-js-65b8915d550e)
        let signOptions = {
            subject: accessToken,
            algorithm: "RS256"
        };
        let token = jwt.sign({}, privateKey, signOptions);

        // call and show 403
        http.request({
            host: "localhost",
            port: 8080,
            path: "/api/check"
        }, statusCallback).end();

        // call with JWT and show 200
        http.request({
            host: "localhost",
            port: 8080,
            path: "/api/check",
            headers: {"Authorization": "Bearer " + token}
        }, statusCallback).end();

    });
}

http.request({
    host: "localhost",
    port: 8080,
    path: "/generate"
}, generateCallback).end();

