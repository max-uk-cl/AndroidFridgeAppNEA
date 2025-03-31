import json
import os
import re
import csv
import requests
from time import sleep
url_base = "http://8gah.serveminecraft.net:5001/api/get"
def FridgeContents(filepath):
    productCount = 0
    class Product:
        def __init__(self,code,name,stock):
            self.code = code
            self.name = name
            self.stock = stock
        def __iter__(self):
            return iter([self.code,self.name,self.stock])
        def __str__(self):
            return f"{self.code}{self.name}{self.stock}"
    CurrentProduct = Product("abc","abc",0)
    f = open(filepath, "w+")
    f.close()
    with requests.get(url_base, stream=True,timeout=10) as r:
        sleep(0.3)
        #print("response: ", r)
        #print("headers:", r.headers)
        data=json.loads(r.content)
        for key in data:
            code = str(key[0])
            stock = str(key[1])
            url_baseProduct = "http://casep.servegame.com:5000/api/get/" + code
            with requests.get(url_baseProduct, stream=True,timeout=10) as r:
                sleep(0.3)
                #print("response: ", r)
                #print("headers:", r.headers)
                data=json.loads(r.content)
                for key in data:
                    if key == "Name":
                        productName = str(data[key])
                        if not(productName == CurrentProduct.name):
                            CurrentProduct = Product(code,productName,stock)
                            with open(filepath, "a") as file:
                                writer = csv.writer(file)
                                writer.writerow(CurrentProduct)
                                productCount = productCount + 1
    ##with open(filepath, "r") as file:
    ##    temp = file.read().splitlines()
    return productCount












