# FakeDNS Docker demo

## Server container

 Server container is located in **server** directory. To build it use the following command inside it:
 
 ```
  sh build.sh
 ```
 
 * It will build Docker image called **silvmike/fakedns-server**
 
 To run built container type the following:
 
 ```
  sh run-docker.sh
 ```
 
 * It will run Docker image **silvmike/fakedns-server** with container name **fakedns-server**
 
## Client container

 Client container is located in **client** directory. To build it use the following command inside it:
 
 ```
  sh build.sh
 ```
 
 * It will build Docker image called **silvmike/fakedns-client**. It uses bash-client for FakeDNS.
 
 To run built container type the following:
 
 ```
  sh run-docker.sh
 ```
 
 * It will run Docker image **silvmike/fakedns-client** 3 times, so you will have 3 separate containers with different IPs and hostnames.
 Hostnames are: **client1.hardocders.ru**, **client2.hardocders.ru**, **client3.hardocders.ru**. 
 Here you can connect to any of client containers and try to ping other containers.
