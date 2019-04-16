#wysper-server
Server application for Wysper messaging service

##IntelliJ Configuration

##### Importing
* Import from Github link using green Clone button.

* Right-click root folder (wysper-server) -- Mark Directory as -- Sources Root

* File -- Project Structure -- Select 1.8 in JDK Dropdown, and set Project Language Level to 8

#####Running
* Click run button next to Server.main() (it should fail)

* Run -- Edit Configurations...

* Under "Server", in the Program arguments field, type `<port number> [no-]save`, filling in with your desired port number and if you would like to save the database.

#####Formatting
* Install `google-java-format` via File -- Settings -- Plugins -- Marketplace

* Restart IntelliJ

* Go to Settings -- google-java-format Settings -- Click `Enable google-java-format` checkbox -- Apply

* Click "enable formatting" in pop-up window at bottom of screen

* Format the current file using Code -- Reformat Code or use `Ctrl-Alt-L`
