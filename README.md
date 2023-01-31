# SSH_Honeypot

# Contents
 
- [Description](#description) 
- [Installation Guide](#Installation-Guide)
- [MongoDB Database](#MongoDB-Database) 
- [The ssh server command](#server-command)
- [The Dashboard](#the-dashboard)

--- 

## Description
This is the description of the honeypot

## Installation Guide

Quick guide on how to use this honeypot.  You will find a detailed procedure to get the honeypot working on your device.

1. Download the source code zip and save it into your desired folder or use the command 
```bash 
$ git clone https://github.com/marcocampione/SSH_honeypot.git 
```

2. Check if you have installed the lates version of java in your system.
- Open command prompt and type : 
```bash
$ java -version
java version "17.0.1" 2021-10-19 LTS
Java(TM) SE Runtime Environment (build 17.0.1+12-LTS-39)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.1+12-LTS-39, mixed mode, sharing)

$ javac -version
javac 17.0.1
```
- If you don't have java installed check this [guide](https://docs.oracle.com/en/java/javase/19/install/overview-jdk-installation.html#GUID-8677A77F-231A-40F7-98B9-1FD0B48C346A) and install it. 

3. To use the honeypot you need to compile and build it first, use the commands
```bash 
$ javac -d classes -classpath "lib/*" src/*.java src/util/*.java src/filesystem/*.java
```

```bash
$ jar -cf sshd.jar -C classes filesystem -C classes util -C classes DummyCommand.class -C classes SshServerMain.class
```

- After these steps, you should have created two new files in the folder :
	- A folder named **classes**
	- Afile named **sshd.jar**

4. Create a `.env` file on the main folder and put the connection string for your MongoDB database in this format :
```
MONGODB_CONNECTION_STRING=mongodb://myDBReader:D1fficultP%40ssw0rd@mongodb0.example.com/?retryWrites=true&w=majority
```
- You can find the connection string by clicking on your database, then on the `connect` button and finally on `connect your application`. This is what will appear : 

![image](https://user-images.githubusercontent.com/38539173/215849120-a6c65376-c47f-41b8-b1e8-629aeb20583d.png)

5. The honeypot is configured in a way that it will use the `port 22` on the host server/machine as listen port, so before running it we have to change the ssh port in our system to a different one since the `port 22` is setted by default. 
	-  Log on to the server as an administrator.
	-  Open the SSH configuration file sshd_config with the text editor :
```bash 
$ sudo nano /etc/ssh/sshd_config
```

![Screenshot 2023-01-31 183123](https://user-images.githubusercontent.com/38539173/215848779-206d4feb-3c7a-4de2-b364-f45f3dcd1b96.png)

- Replace `port 22` with a port between 1024 and 65536 and uncomment the line
- Save the file
- Restart the service
```bash 
$ service ssh restart
```

6. The setup is completed now you can run the honeypot using the command
```bash 
$ java -cp "lib/*:sshd.jar" SshServerMain
```




## MongoDB Database


## Server Command


## The Dashboard

