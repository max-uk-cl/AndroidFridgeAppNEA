import json
import re
import requests

def ProductInfo(ProductCode):
    url = "http://casep.servegame.com:5000/api/get/8001630003968"
    try:
        codeFiltered = (re.findall("[0-9]+",ProductCode))[0]
    except:
        pass
    #url = url+codeFiltered
    prodData = requests.get(url)
    try:
        print("hi")
        prodData = requests.get(url,stream = True)
        if prodData.ok:
            jData = json.loads(prodData.content)
            response = str(jData[2])
        else:
            response = "Error. Couldn't access API"
    except:
        response =  "mewo"

    return response