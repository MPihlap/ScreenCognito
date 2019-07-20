import tkinter as tk
import time
import requests

# create an INET, STREAMing socket
# now connect to the web server on port 80 - the normal http port
def start_beacon():
    ip = "http://10.11.69.113/1"
    r = requests.get(url=ip)
def stop_beacon():
    ip = "http://10.11.69.113/0"
    r = requests.get(url=ip)
# print("Turning on")
# s.send('/1'.encode())
# stop_beacon()
start_beacon()



# root = tk.Tk()
# root.geometry('600x600')

# button = tk.Button(root, text="Lights out", fg="white", bg="red")
# button.place(x=300, y=300)
# button.config(width=20, height=10)

# root.mainloop()