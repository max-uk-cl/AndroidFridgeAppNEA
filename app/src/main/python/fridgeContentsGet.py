import json
import re
import requests
from time import sleep
url_base = "http://8gah.serveminecraft.net:5001/api/get"
def FridgeContents():
    class Product:
        def __init__(self,productCode,name,stock):
            self.productCode = productCode
            self.name = name
            self.stock = stock
        def __str__(self):
            return f"({self.productCode}){self.name}{self.stock}"
    with requests.get(url_base, stream=True,timeout=10) as r:
        sleep(0.3)
        print("response: ", r)
        print("headers:", r.headers)
        data=json.loads(r.content)
        for key in data:
            code = str(key[0])
            url_baseProduct = "http://casep.servegame.com:5000/api/get/" + code
            print("b")
            with requests.get(url_baseProduct, stream=True,timeout=10) as r:
                sleep(0.3)
                print("response: ", r)
                print("headers:", r.headers)
                data=json.loads(r.content)
                for key in data:
                    if key == "Name":
                        print(str(data[key]))




