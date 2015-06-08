# FakeDNS
FakeDNS, to use as dynamic dns for Docker containers

## Server

 1. DNS server listens to 53 port on specified host for UDP
 2. Registrator listens on specified port

Registrator is an application used to register host with provided hostname.

### Usage

* java -jar **fakedns-server-1.0-SNAPSHOT.jar** &lt;hostname to listen: *localhost*, 0.0.0.0, etc.&gt; &lt;registrator port&gt;

Example:

```
 java -jar fakedns-server-1.0-SNAPSHOT.jar localhost 8099
```

P.S. You can easily check it:

```
 dig @localhost myfake.host.org
```

## Java Client

 Connects to specified registrator running on specified host and listening to specified port, and registering this host (that running client) with specified hostname.

### Usage

* java -jar **fakedns-client-1.0-SNAPSHOT.jar** &lt;hostname of registrator&gt; &lt;registrator port&gt; &lt;your fake hostname&gt;

Example:

```
 java -jar fakedns-client-1.0-SNAPSHOT.jar localhost 8099 myfake.host.org
```

## Bash Client

 Connects to specified registrator running on specified host and listening to specified port, and registering this host (that running client) with specified hostname.

### Usage

* sh **fakedns-client.sh** &lt;hostname of registrator&gt; &lt;registrator port&gt; &lt;your fake hostname&gt;

Example:

```
 sh fakedns-client.sh localhost 8099 myfake.host.org
```

P.S. **netcat** is required.

## Used links

[http://www.zytrax.com/books/dns/ch15/](http://www.zytrax.com/books/dns/ch15/)

[http://www.freesoft.org/CIE/Topics/78.htm](http://www.freesoft.org/CIE/Topics/78.htm)

[Microsoft TechNet 'How DNS Works'](https://technet.microsoft.com/en-us/library/cc772774(v=ws.10).aspx)
