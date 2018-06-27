# npmserver

a  node npm server written in java
this server cache in disk al request so you can install modules offline
work as a doc server  

# config
build server 

set npm registry to http://locahost:8080

$ npm config set registry=http://localhost:8080

install some modules
$ npm i request -S

watch modules doc in 
http://localhost:8080?p=request


list of modules docs  
http://localhost:8080/
