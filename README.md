# wysper-server
Server application for Wysper messaging service

## NOTE: Since PR #25, the server runs using command-line arguments. To configure in IntelliJ:

* Run -- Edit Configurations...

* Under "Server", in the Program arguments field, type `<port number> [no-]save`, filling in with your desired port number and if you would like to save the database.

* Everything else is taken care of under the hood!

## When setting up IntelliJ:

* Import from Github link using green Clone button.

* Right-click root folder (wysper-server) -- Mark Directory as -- Sources Root

* File -- Project Structure -- Select 1.8 in JDK Dropdown, and set Project Language Level to 8

