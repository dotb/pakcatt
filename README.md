### PakCatt
PakCatt is a modern packet radio framework built on top of KISS and AX25. It's name hails from the much loved PAKRATT TNC modems used in the heyday of packet radio. It's written in Kotlin and the Springboot framework, which makes it highly extensible and able to integrate into other modern web services.

PakCatt is maintained by VK3AT and VK3LE who also run an instance on 144.875MHz FM in the Melbourne area, Australia - Connect to VK3AT-1.

VK3FUR has made a fantastic video that walks you through the technical setup you need to access VK3AT-1 in Melbourne, or any other BBS for that matter. You can find it on her Youtube channel: https://www.youtube.com/watch?v=ix7YEMuo2Jk

## Features

- **Radio and TCP/IP Interfaces** allow you to connect via RF or an IP network

- **A Bulletin board** accessible to multiple users over RF

- **A Mailbox service** to send and receive personal mail messages
  
- **An extensible app framework** for developing digital services over packet radio

- **A Scripting framework** for quick integration with other systems and your own scripts

- **Beacon** messages broadcast at a configured interval 

- **A log of seen stations** to let you know who's been active recently
  
- **TNC connections** allow PakCatt to maintain connections to multiple hardware and software TNCs

- **KISS implementation** for raw communication with a TNC

- **AX.25 implementation**, or at least a good start of it ;-)

- **APRS® implementation** to handle APRS Messages, MIC-E (location) frames, and Status frames.

## Install and setup
Running PakCatt is easy:

1) Download the latest pakcatt-x.x.jar and application.yml files from the [PakCatt repo on GitHub](https://github.com/dotb/pakcatt/releases/). Replace x.x with the latest release version. For release 0.3:
   ```
   $ wget https://github.com/dotb/pakcatt/releases/download/release_0.3/pakcatt-0.3.jar
   $ wget https://github.com/dotb/pakcatt/releases/download/release_0.3/application.yml
   ```

2) Install the Java Runtime Environment (JRE). Yes I know, don't worry it'll be painless! On a Debian / Ubuntu system this can be done using apt:
   ```
   $ apt install default-jre
   ```

3) Edit the application.yml file to taste. At minimum you'll want to change the mycall, TCP/IP and MongoDB configuration:
   ```
   pakcatt:
    application:
        mycall: PAKCAT-1

   network.tcp:
    enabled: true
    port: 7331
    callsign-regex: '[a-zA-Z]+[0-9][a-zA-Z]+-?[0-9]?'
    pre-welcome-message: Welcome to PakCatt!
    callsign-regex-fail-message: Sorry, your callsign could not be validated.
  
   spring:
    ## Database configuration
    data.mongodb.database: pakcatt
    spring.data.mongodb.host: 127.0.0.1
    spring.data.mongodb.port: 27017
   ```


4) The bulletin board and mail apps need a database to store messages. PakCatt uses MongoDB as it's persistence layer, which is also quick and easy to get running. On Debian / Ubuntu systems this can be done using apt, then run MongoDB and specify a folder to store your data:
   ```
   $ apt install mongodb
   $ mongodb --dbpath /home/mydir/pakcattdb
   ```

Now you can start PakCatt and then connect to it over TCP/IP to create posts and messages!

```
$ java -jar pakcatt-x.x.jar --spring.config.location=./application.yml
╔═══╗     ╔╗  ╔═══╗      ╔╗  ╔╗ 
║╔═╗║     ║║  ║╔═╗║     ╔╝╚╗╔╝╚╗
║╚═╝║╔══╗ ║║╔╗║║ ╚╝╔══╗ ╚╗╔╝╚╗╔╝
║╔══╝╚ ╗║ ║╚╝╝║║ ╔╗╚ ╗║  ║║  ║║ 
║║   ║╚╝╚╗║╔╗╗║╚═╝║║╚╝╚╗ ║╚╗ ║╚╗
╚╝   ╚═══╝╚╝╚╝╚═══╝╚═══╝ ╚═╝ ╚═╝
By VK3AT & VK3LE
2023-03-03 09:46:59.349 [         PakCattKt] 	 INFO: Starting PakCattKt v0.2 
on mufasa.utiku.io with PID 21435 (/tmp/pakcatt-0.2.jar started by bradley 
in /tmp)
2023-03-03 09:46:59.355 [         PakCattKt] 	 INFO: The following profiles 
are active: production
...
```

Check out [this blog post](https://bradleyclayton.io/posts/radio/pakcatt_getting_started/) for more detailed configuration instructions.

## Build instructions
You'll need:

- **A Java JVM**
- **Maven** which you can [download](https://maven.apache.org/install.html), but we recommend using a package manager, such as [Homebrew](https://brew.sh/) for OSx.
 
Build and run should be as easy as two commands:
```bash
$ mvn install
$ java -jar target/pakcatt-x.x.jar --spring.config.location=./application.yml
╔═══╗     ╔╗  ╔═══╗      ╔╗  ╔╗ 
║╔═╗║     ║║  ║╔═╗║     ╔╝╚╗╔╝╚╗
║╚═╝║╔══╗ ║║╔╗║║ ╚╝╔══╗ ╚╗╔╝╚╗╔╝
║╔══╝╚ ╗║ ║╚╝╝║║ ╔╗╚ ╗║  ║║  ║║ 
║║   ║╚╝╚╗║╔╗╗║╚═╝║║╚╝╚╗ ║╚╗ ║╚╗
╚╝   ╚═══╝╚╝╚╝╚═══╝╚═══╝ ╚═╝ ╚═╝
By VK3AT & VK3LE
...
```

## Configuration
The configuration options are kept in the application.yml file. Most of the defaults will work for you out-of-the box but at a minimum you'll need to configure your callsign, database connection and TNC connection.

The application.yml configuration compiled into the app bundle will be used by default, but it be overridden by specifying a configuration path at startup like this:
```
java -jar /opt/pakcatt/pakcatt-x.x.jar --spring.config.location=/path/to/application.yml
```

## References
The AX.25 implementation is based on the excellent specification for AX.25 version 2.2, by NJ7P, N7LEM, N7OO, N7CUU and WD5IVD. It is published by TAPR and can be found on [their website](https://www.tapr.org/pdf/AX25.2.2.pdf).

APRS® is a registered trademark of Bob Bruninga