from urllib.request import urlopen
import time
import subprocess

import sys

def turnOffScreen():
    if sys.platform.startswith("linux"):
        subprocess.call(["xset", "-display", ":0.0", "dpms", "force", "off"])
    elif sys.platform.startswith("windows"):
        subprocess.call("turnoff.exe")


prev = 17
while True:
    link = "http://90.191.160.122/web/sodi/screencognito/data.json?fbclid=IwAR0Cc93sV1Qm-bbylcoZia2mbfoRDcgErX-P6jDQl_osostgzm79qNvjN4w"
    f = urlopen(link)
    content = f.read().decode("utf-8")
    print(content)
    if content != prev:
        prev = content
        turnOffScreen()
    time.sleep(0.5)