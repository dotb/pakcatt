### PakCatt
PakCatt is a modern packet radio framework built on top of KISS and AX25. It's name comes from the much loved PAKRATT TNC modems used in the heyday of packet radio. 

It's written in Kotlin and the Springboot framework, which makes it highly extensible and able to integrate into other web services.

PakCatt is maintained by VK3AT and VK3LE, who also run an instance on 144.875MHz FM in the Melbourne area, Australia - Connect to VK3LIT-1. VK3FUR has made a fantastic video that walks you through the technical setup you need to access the PakCatt BBS in Melbourne, or any other BBS for that matter. You can find it on her Youtube channel: https://www.youtube.com/watch?v=ix7YEMuo2Jk

## Features

PakCatt is in its early days! Here are a few of the features it currently supports:

- **A Bulletin board** accessible to multiple users over RF

- **A Mailbox service** - you can send and receive mail personal messages
  
- **An extensible app framework** for developing digital services over packet

- **A Scripting framework** for quick integration with other systems and scripts

- **Beacon** - Beacon a custom message at a configured time interval 

- **Log of seen stations** - let you know who's been active recently
  
- **A Serial Interface** to hardware TNCs

- **KISS implementation** for raw communication with a TNC

- **AX.25 implementation**, or at least a good start of it ;-)

- **APRS® frames for** Messages, MIC-E, and Status frames.

## Build instructions
You'll need:

- **A Java JVM**
- **Maven** which you can [download](https://maven.apache.org/install.html), but we recommend using a package manager, such as [Homebrew](https://brew.sh/) for OSx.
- **Docker** is optional, if you want to build PakCatt into a Docker image
 
Build and run should be as easy as two commands:
```bash
$ mvn install
$ java -jar target/pakcatt-0.1.jar
╔═══╗     ╔╗  ╔═══╗      ╔╗  ╔╗ 
║╔═╗║     ║║  ║╔═╗║     ╔╝╚╗╔╝╚╗
║╚═╝║╔══╗ ║║╔╗║║ ╚╝╔══╗ ╚╗╔╝╚╗╔╝
║╔══╝╚ ╗║ ║╚╝╝║║ ╔╗╚ ╗║  ║║  ║║ 
║║   ║╚╝╚╗║╔╗╗║╚═╝║║╚╝╚╗ ║╚╗ ║╚╗
╚╝   ╚═══╝╚╝╚╝╚═══╝╚═══╝ ╚═╝ ╚═╝
By VK3AT & VK3LE
```  
You can also run the handy build.sh script, which will build and create a Docker image.
```bash
$ ./build.sh
```

## Configuration
The configuration options are kept in the application.yml file. Most of the defaults will work for you out-of-the box but at a minimum you'll need to configure your callsign, database connection and TNC connection.

The application.yml configuration compiled into the app bundle will be used by default, but it be overridden by specifying a configuration path at startup like this:
```
java -jar /opt/pakcatt/pakcatt-0.1.jar --spring.config.location=/path/to/application.yml
```

## References
The AX.25 implementation is based on the excellent specification for AX.25 version 2.2, by NJ7P, N7LEM, N7OO, N7CUU and WD5IVD. It is published by TAPR and can be found on [their website](https://www.tapr.org/pdf/AX25.2.2.pdf).

APRS® is a registered trademark of Bob Bruninga