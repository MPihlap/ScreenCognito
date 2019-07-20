import tkinter as tk
import time
import requests
import sys

# create an INET, STREAMing socket
# now connect to the web server on port 80 - the normal http port
def start_beacon():
    ip = "http://10.11.69.113/2"
    r = requests.get(url=ip)
def warning():
    ip = "http://10.11.69.113/1"
    r = requests.get(url=ip)
def stop_beacon():
    ip = "http://10.11.69.113/0"
    r = requests.get(url=ip)

if sys.argv[1] == '1':
    warning()
elif sys.argv[1] == '0':
    stop_beacon()
else:
    start_beacon()
