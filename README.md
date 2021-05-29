### PakCatt
PakCatt is a modern packet radio framework built on top of KISS and AX25. It's name comes from the much loved PAKRATT TNC modems used in the heyday of packet radio. PakCatt is made by VK3LIT and VK2VRO.
    
## Features

PakCatt is in its early days! Here are a few of the features it currently supports:

- **Serial Interface** to hardware TNCs

- **KISS implementation** for raw communication with a TNC

- **AX.25 implementation**, or at least the start of it ;-)

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