import json
import re
import requests
from time import sleep
##def ProductInfo(ProductCode):
##    print ("check 2")
 ##   url = "http://casep.servegame.com:5000/api/get/8001630003968"
   ## print("check 3")
    ##try:
      ##  codeFiltered = (re.findall("[0-9]+",ProductCode))[0]
    ##except:
      ##  pass
    #url = url+codeFiltered
    #prodData = requests.get(url)

    #try:
     #   jData = json.loads(prodData.content)
      #  response = str(jData[2])
    #except:
     #   response =  "couldnt access api"

    #return response

url_base = "http://casep.servegame.com:5000/api/get/7622300315283"
def ProductInfo(ProductCode):
    with requests.get(url_base, stream=True,timeout=10) as r:
        sleep(0.3)
        print("response: ", r)
        print("headers:", r.headers)
        data=json.loads(r.content)
        for key in data:
            print(key + " : " + str(data[key]))


