import cv2
from pyzbar.pyzbar import decode
def BarcodeReader(image):
    img = cv2.imread(image)
    detectedBarcodes = decode(img)
    if not detectedBarcodes:
        whooops = "meow"
        return whooops
    else:

        # Traverse through all the detected barcodes in image
        for barcode in detectedBarcodes:

            # Locate the barcode position in image
            (x, y, w, h) = barcode.rect

        # Put the rectangle in image using
        # cv2 to highlight the barcode
            cv2.rectangle(img, (x-10, y-10),
                      (x + w+10, y + h+10),
                      (255, 0, 0), 2)

            if barcode.data!="":
                return barcode.data