import csv
import os
def returnProductLine(filepath,lineNum):
    with open(filepath, "r") as file:
        temp = file.read().splitlines()
        return temp[lineNum]