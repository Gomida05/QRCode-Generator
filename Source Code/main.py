from kivy.uix.screenmanager import ScreenManager, NoTransition
from kivymd.uix.screen import MDScreen
from kivymd.app import MDApp
from kivy.utils import platform
from qrcode import make
from kivy.core.window import Window
from kivymd.uix.dialog import MDDialog, MDDialogButtonContainer, MDDialogHeadlineText, MDDialogSupportingText
from kivy.uix.widget import Widget
from kivymd.uix.button import MDButton, MDButtonText
import os
Window.clearcolor= (1,1,1,1)

if platform == "android":
    from android.loadingscreen import hide_loading_screen
    hide_loading_screen()
    paths= '/sdcard/DCIM/QRCode'
elif platform == "linux":
    paths = "/home/gomida/Desktop/QR_Generater"
elif platform == "win":
    paths = "C:/Users/esrom/Desktop/QR_Generater"
else:# ask user using print() to report it to the development team
    print("Platform not supported")
    print("Unsupported platform: please report this to the dev's team thanks!")

class Screen1(MDScreen):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.name = "screen1"

    def soon(self):
        if self.img.source=="":
            self.ans.text= "coming soon"



    def generate(self):
        
        x = self.txt.text
        if self.img.source =="":
            if x:
                mg=make(x)
                try:
                    y=""
                    if not os.path.isdir(paths):
                        os.makedirs(paths)
                        mg.save(f"{paths}/QR{y}.jpg")
                        self.img.source = f"{paths}/QR{y}.jpg"
    
    
                        self.ans.text= "The QRCode has Generate successful!"
                    else:
                        y=""
                        if os.path.exists(f"{paths}/QR{y}.jpg"):
                            y=1
                            while os.path.exists(f"{paths}/QR({y}).jpg"):
                                y+=1
                            if platform=="android":
                                from jnius import autoclass
    
                            mg.save(f"{paths}/QR({y}).jpg")
                            self.img.source = f"{paths}/QR({y}).jpg"
                            saved= self.img.source
                            MediaScannerCon= autoclass("android.media.MediaScannerConnection")
                            MediaScannerCon.scanFile(
                                autoclass("org.kivy.android.PythonActivity").mActivity,
                                [saved],
                                None,
                                None
                            )
    
                                #add one number if the file exists
                            # mg.save(f"{paths}/QR({y}).jpg")
                            # self.img.source = f"{paths}/QR({y}).jpg"
    
    
                        else:
                            if platform=="android":
                                import android
                                from jnius import autoclass

                            y=""
                            mg.save(f"{paths}/QR{y}.jpg")
                            self.img.source = f"{paths}/QR{y}.jpg"
                            saved= self.img.source
                            MediaScannerCon= autoclass("android.media.MediaScannerConnection")
                            MediaScannerCon.scanFile(
                                autoclass("org.kivy.android.PythonActivity").mActivity,
                                [saved],
                                None,
                                None
                            )
                        #mg.save(paths+'/QR.jpg')
                        self.ans.text= "The QRCode has Generate successful!"
                        

                except Exception as g:
                    self.ans.text= f"Error: {g}"
            else:
                self.ans.text= "There is nothing to generate a QRcode!"
        else:
            self.ans.text= "There is already a QRcode so please delete or save it using the buttons"

    def remove_its(self):
        y= self.txt.text
        x= self.img.source

        if x == "":
            self.ans.text= "There is no generated QRCode to remove!"

        else:
            try:
                #print("hello there",self.img.source)
                #os.remove(f"{paths}/QR.jpg")
                os.remove(x)
                self.img.source= ""
                self.ans.text= "The QRCode has been removed successful!"
                self.txt.text= ""

            except Exception as e:
                print(f"unluck got an error: {e}")
                self.ans.text= f"unluck got an error: {e}"

    def save_as(self):
        x=self.img.source
        if x == "":
            self.ans.text= "There is no generated QRCode to save!"
        else:
            self.ans.text= f"The QRCode has been saved successful! to {self.img.source}"
            self.txt.text= "" 
            self.img.source= ""




class Screen2(MDScreen):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.name= "screen2"
        self.Dialogn=MDDialog(
                MDDialogHeadlineText(
                text="Coming soon",
                halign="left",
            ),
            MDDialogSupportingText(
                text="This feature is currently under development",
                halign="left",
            ),
            MDDialogButtonContainer(
                Widget(),
                MDButton(
                    MDButtonText(text="Cancel"),
                    style="text",
                    on_release= self.cancel
                ),
                MDButton(
                    MDButtonText(text="Save"),
                    style="text",
                    on_release=self.cancel,
                ),
                spacing="8dp",
            ),
            )

    
    #def generate(self):
     #   t= self.manager.get_screen("screen1").txt.text
        

    def field(self):
        #t= self.manager.get_screen("screen1").txt.text
        ...

    def item(self):
        ...
    def test(self):
        self.Dialogn.open()
    def cancel(self, inst):
        self.Dialogn.dismiss()

"""        MDDialogButtonContainer(
            MDDialog(
                MDDialogHeadlineText(
                text="Coming soon",
                halign="left",
            ),
            MDDialogSupportingText(
                text="This is just a test for the app design!",
                halign="left",
            ),
            MDDialogButtonContainer(
                MDTextField(
                    hint_text="Enter your name",
                    multiline=False,
                ),
                MDTextField(
                    hint_text="Enter your name",
                    multiline=False,
                    radius= (25,25,25,25)
                )
            ),
            MDDialogButtonContainer(
                Widget(),
                MDButton(
                    MDButtonText(text="Cancel"),
                    style="text",
                ),
                MDButton(
                    MDButtonText(text="Save"),
                    style="text",
                ),
                spacing="8dp",
            ),
            ).open()
        ).orientation='vertical'
"""

    

class Main(MDApp):
    def build(self):
        self.on_start()
        sm= ScreenManager()
        sm.add_widget(Screen1())
        sm.add_widget(Screen2())
        sm.transition= NoTransition()
        return sm 
    
    def on_start(self):
        if platform == "android":
            from android.permissions import Permission, request_permissions,  check_permission
            from android import loadingscreen
            loadingscreen.hide_loading_screen()
            t= Permission.READ_EXTERNAL_STORAGE
            x= Permission.WRITE_EXTERNAL_STORAGE
            IM=Permission.READ_MEDIA_IMAGES
            request_permissions([t, x,IM])
            #check if it's PERMISSION_GRANTED
            if check_permission(t) and check_permission(x):
                print("Storage permission granted")
            else:
                #warin the user
                print("Storage permission denied")

        else:
            ...#exit()
if __name__ == '__main__':
    Main().run()