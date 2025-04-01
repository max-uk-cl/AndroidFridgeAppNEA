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

url_base = "http://casep.servegame.com:5000/api/get/"
def ProductInfo(ProductCode):
    url_update = url_base + str(ProductCode)
    ProductName = ""
    with requests.get(url_update, stream=True,timeout=10) as r:
        sleep(0.3)
        data=json.loads(r.content)
        for key in data:
            if key == "Name":
                ProductName = str(data[key])
    print("Product name" ,ProductName)
    return  ProductName


