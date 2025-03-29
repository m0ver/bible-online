Introduction
---
An open source project of bible online service which bases on struct2.0 framework.

MySQL Server Installation
---
docker run -p 3306:3306 --name mysql-5.7 -v /Users/James/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=password -d mysql:5.7

Run it in a servlet container
---
```tcsh
# bin/dispatcher start --import org.tinystruct.system.TomcatServer
```
Learn more information by clicking <a href="https://github.com/tinystruct/tinystruct2.0">here</a>. 
