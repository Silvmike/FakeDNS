# FakeDNS
FakeDNS, to use as dynamic dns for Docker containers

## Server

 1. DNS server listens to 53 port on specified host for UDP
 2. Registrator listens on specified port

Registrator is an application used to register host with provided hostname.

## Client

 Connects to specified registrator running on specified host and listening to specified port, and registering this host (that running client) with specified hostname.

## Used links

[http://www.zytrax.com/books/dns/ch15/](http://www.zytrax.com/books/dns/ch15/)

[http://www.freesoft.org/CIE/Topics/78.htm](http://www.freesoft.org/CIE/Topics/78.htm)

[Microsoft TechNet 'How DNS Works'](https://technet.microsoft.com/en-us/library/cc772774(v=ws.10).aspx)
