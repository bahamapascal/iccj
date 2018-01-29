# ICCJ - IOTA Control Center (Java)



## Overview

The ICCJ allows for management of multiple ICCR server instances with a user friendly GUI.
It's a cross-platform java (thus the need for a J) based GUI application implemented using the JAVA Swing framework. It requires java version 1.8 or greater in order to run.

## Build Instructions
*NOTE : Java 8 or above, maven and previous building of ICCR required *
These instructions will create a zip file that can be deployed and unpacked onto the users PC

1. Create a new folder called "ICC" `mkdir ICC`
2. Change directory `cd ICC`
3. Build the ICCR] for dependencies: [ICCR Build Instructions](https://github.com/bahamapascal/ICCR#build-instructions)
4. Clone the repository to ICC `git clone https://github.com/bahamapascal/iccj`
5. Change directory `cd iccj`
6. Clean maven `mvn clean`
7. Compile `mvn compile`
8. Gather dependencies`mvn install`
9. Generate "iccj-<VERSION>.zip" `./release-iccj.bash <VERSION> <GROUP> <USER>`
*for example* `./release-iccj.bash  1.0.0 root root`
10. Move generated "iccj-<VERSION>.zip" to desired Destination


## How to install and run ICCJ
These instructions presume you have already built or downloaded the `iccj-<VERSION>.zip` file.

You can download the latest official ICCJ release here: [GitHub Releases](https://github.com/bahamapascal/iccj/releases) 
(use the `iccj-VERSION>.zip` file)

**For Windows**
1. 
1. Create a new folder and unzip the `iccj-<VERSION>.zip` file
2. Start the ICCJ by executing the icc.bat file in `~\bin`

**For Ubuntu/iOS**
1. Create a new folder `mkdir ICCJ`
2. Change directory `cd ICCJ`
3. Unzip into ICCJ folder `unzip path/to/iccj-<VERSION>.zip -d path/to/ICCJ`
4. Start ICCJ by executing `path/to/ICCJ/bin/icc`










