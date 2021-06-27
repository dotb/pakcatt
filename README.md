### PakCatt
PakCatt is a modern packet radio framework built on top of KISS and AX25. It's name comes from the much loved PAKRATT TNC modems used in the heyday of packet radio. 

It's written in Kotlin and the Springboot framework, which makes it highly extensible and able to integrate into other web services.

PakCatt is maintained by VK3LIT and VK2VRO, who also run an instance on 144.875MHz FM in the Melbourne area, Australia - Connect to VK3LIT-1.

## Features

PakCatt is in its early days! Here are a few of the features it currently supports:

- **An extensible app framework** for developing digital services over packet

- **A Maildrop service** - you can send and receive mail!

- **Beacon** - Beacon a custom message at a configured time interval 

- **Log of seen stations** - check out the `last` command
  
- **Serial Interface** to hardware TNCs

- **KISS implementation** for raw communication with a TNC

- **AX.25 implementation**, or at least a good start of it ;-)

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
By VK3LIT & VK2VRO 
```  
You can also run the handy build.sh script, which will build and create a Docker image.
```bash
$ ./build.sh
```

## References
The AX.25 implementation is based on the excellent specification for AX.25 version 2.2, by NJ7P, N7LEM, N7OO, N7CUU and WD5IVD. It is published by TAPR and can be found on [their website](https://www.tapr.org/pdf/AX25.2.2.pdf).