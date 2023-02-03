# SSH_Honeypot

# Contents
 
- [Description](#description) 
- [Installation Guide](#Installation-Guide)
- [The ssh server command](#server-command)
- [MongoDB Integration](#MongoDB-integration) 
- [The Dashboard](#the-dashboard)



## Description
The development of this honeypot starts with an example provided in the [Apache MINA SSHD](https://github.com/apache/mina-sshdApaMINA) libraries: ServerMain.java. This script allows the instauration of a server at a port that the user can choose (in our case port 22, dedicated to SSH communications). The program basically gives access to the host operating system, hence giving complete access to the machine once the authentication step in passed. 
The data that the honeypot will gather during the activity time are then store on a MongoDB database. I chose to use this database since it gives a really nice representation of the data that are stored in the collection. Thanks to the dashboard function that is buit in MongoDB, we can graphically visualise the data we will collect.

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

## Server Command
This is a list of all the command that are implemented inside the honeypot server. These are some of the most used linux terminal commands, implemented in a way that accurately emulates the workings on Linux. You can add or modify the command by modifying the `DummyCommand.java` file located in `src` folder. 

|     |      Command       | Description                                                                                         |
|:---:|:------------------:|:--------------------------------------------------------------------------------------------------- |
|  1  |        help        | Will display all the available commands                                                             |
|  2  |        exit        | Will close the connection with the honeypot                                                         |
|  3  |         ls         | This command will list files                                                                        |
|  4  |         cd         | This command allows you to move between directories                                                 |
|  5  |       clear        | This command will clear the terminal screen                                                         |
|  6  |       mkdir        | This command creates a directory or subdirectory                                                    |
| 6.a | mkdir -h / --help  | This command displays help for the mkdir command                                                    |
|  7  |         rm         | This command allows removing files and directories                                                  |
| 7.a |   rm -h /--help    | This command displays help for the rm command                                                       |
|  8  |        pwd         | This commang writes to standard output the full path name of your current directory                 |
|  9  |       whoami       | This command allows the user to see the currently logged-in user                                    |
| 9.a | whoami -h / --help | This command displays help for the whoami command                                                   |
| 10  |        echo        | This command will display lines of text or string which are passed as arguments on the command line |
| 11  |       passwd       | This command will show a Permission denied message                                                  |
| 12  |      iptables      | This command will show a Permission denied message                                                  |
| 13  |        grep        | This command will show a Permission denied message                                                  |
| 14  |        sudo        | This command will show a Permission denied message                                                  |
| 15  |        cat         | This command will show a Permission denied message                                                  |
| 16  |        halt        | This command will show a Permission denied message                                                  |

## MongoDB Integration
To use this honeypot you need fist to register to [MongoDB](https://www.mongodb.com/) because we will use their sevices to store the data from our machine. I chose this service for its user-friendly nature, but especially for the ability it offers to have databases hosted directly by them and completely free of charge. Another feature that made me choose this service is the ability to graphically visualize the collection of data being collected by our honeypot.

- After we register we need to create a new project and then a new cluster that will host our database: 
![Screenshot 2023-02-01 143228](https://user-images.githubusercontent.com/38539173/216057330-505405f2-2b14-4fcd-abbf-0495f43ecc6e.png)
 
The data that the honeypot will send to our database are in this format

```
_id : ObjectId('xxxxxxxxxxxxxxxxxxxxxxxx')
time:"yyyy-mm-dd hh:mm:ss"
ip:"127.0.0.1"
status:"success"
continent:"continentName"
continentCode:"XX"
country: "countryName"
countryCode:"XX"
region: "xx"
regionName: "regionName"
city: "cityName"
zip:"xxxx"
location: 
	Object type: "Point"
	coordinates:
	Array 
		0: 00.0000
		1: 11.1111
isp:"ispName"
org: ""
as: "name"
asname: "name"
username: "root"
password: "test"
authentication: "Failed"
```

All the geolocation information that we have in this file are obtained using an api call thanks to [this service](https://ip-api.com/), the free api that I am using in this project is rate limited to **45** request per minutes, but from my tests are more than enough for the kind of use we need to perform.

## The Dashboard

The choice to use mongoDB over other services that offer the ability to host databases , was to be able to directly create interactive dashboards that update in real time. [Here](https://www.mongodb.com/docs/charts/tutorial/movie-details/create-dashboard/) you can find a complete guide on how to create a dashboard. 
In the folder you will also find a file named `Honeypot_Dashboard.charts` this is the configuration file of my dashboard that you can import into MondgoDB to get the same dashboard I created. 

- Go on the `Charts` tab
![Screenshot 2023-02-02 085042](https://user-images.githubusercontent.com/38539173/216264366-6ec7780d-758a-4b08-b029-505c153eb77b.png)

- Click on `Add Dashboard` then on `Import dashboard`
![Screenshot 2023-02-02 085206](https://user-images.githubusercontent.com/38539173/216264370-ac676e7d-2357-40ec-a039-30e79e57db8b.png)

- After selecting the `Honeypot_dashboard.charts` file that you can find on the main folder click on `save`. 
![Screenshot 2023-02-02 085321](https://user-images.githubusercontent.com/38539173/216264371-799b1094-48dc-4c09-902a-510294298bd3.png)

- You have successfully imported the dashboard. 
This is the result you will obtain [Honeypot Dashboard](https://tinyurl.com/HoneypotDashboard) 
