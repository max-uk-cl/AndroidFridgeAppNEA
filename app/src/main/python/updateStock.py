import requests
url_base = "http://8gah.serveminecraft.net:5001/api/"
def addItem(code):
    url_update = url_base + "insert/" + str(code)
    print(url_update)
    requests.get(url_update, stream=True,timeout=10)
    print("itemAdded")
def removeItem(code):
    url_update = url_base + "delete/" + str(code)
    requests.get(url_update, stream=True,timeout=10)
    print("itemRemoved")