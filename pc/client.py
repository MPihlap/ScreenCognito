from urllib.request import urlopen
import subprocess
import time
import sys

if sys.platform.startswith("win"):
    from win10toast import ToastNotifier
    toaster = ToastNotifier()


def create_toast():
    if sys.platform.startswith("linux"):
        subprocess.call(["notify-send", 'Hoiatus!', 'Ekraan suletakse peagi'])
    elif sys.platform.startswith("win"):
        toaster.show_toast("Hoiatus!", "Ekraan suletakse peagi", duration=5)


def turn_screen_off():
    if sys.platform.startswith("linux"):
        subprocess.call(["xset", "-display", ":0.0", "dpms", "force", "off"])
    elif sys.platform.startswith("win"):
        subprocess.call("turnoff.exe")


prev = 0
while True:
    link = "http://90.191.160.122/web/sodi/screencognito/data.json"
    f = urlopen(link)
    content = f.read().decode("utf-8")
    print(content)
    if content != prev:
        prev = content
        if content == "1":
            create_toast()
        elif content == "2":
            turn_screen_off()

    time.sleep(0.5)
