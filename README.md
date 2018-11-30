# npmserver

a node npm server written in java, 
this server cache in disk al request so you can install modules offline

## Download
go to builds 

## config
build server 

set npm registry to http://locahost:8888
``
$ npm config set registry http://127.0.0.1:8888
``

install some modules
``
$ npm i request -S
``

to work with yarn  add this line to /etc/hosts
`
127.0.1.1   registry.yarnpkg.com
`

watch modules doc in 

http://localhost:8888?p=request


list of modules docs  
http://localhost:8888/