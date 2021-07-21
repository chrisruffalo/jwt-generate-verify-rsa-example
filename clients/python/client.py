import jwt
import requests

# config
host = "localhost"
port = "8080"
url = "http://" + host + ":" + port

# call generate endpoint
generateResponse = requests.get(url + "/generate")
access = generateResponse.json()

# extract token and key
accessToken = access['accessToken']
privateKeyBase64 = "-----BEGIN PRIVATE KEY-----\n" + access['privateKey'] + "\n-----END PRIVATE KEY-----"

# create encoded jwt
encodedJwt = jwt.encode({"sub": accessToken}, privateKeyBase64, algorithm="RS256")
headers = { "Authorization": "Bearer " + encodedJwt }

# show 403 without jwt
checkResponse = requests.get(url + "/api/check")
print(checkResponse)

# show 200 with jwt
checkResponse = requests.get(url + "/api/check", headers=headers)
print(checkResponse)