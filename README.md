# FtpDownloader
### Introdution
I had a several place from whitch I need to download mp3 files. Mini pc like raspberry pi or onange pc  constantly record sound , convert it to mp3 and then delete origin files. On mimi pc launch simple ftp server , the connection to them is  very unstable and only at night time   it can be downloaded something,  besides I want to get some information about succes or errors of this proccess to my smartphone. So I write simple application to get this result.
### Technologies
- Spring Boot 3.0
- Thymeleaf 3.0
- Apache Commons Net
- Quartz
- Pengrad ( Java Telegram Bot Api)
### Set up
You can run its via maven using run command or make executable jar archive  via maven ( plugin for it is included in pom file).
Before run you need to configure properties file in resources for your logging file path and datasource credentials, and type of your dataBase. To create table for quartz use scripts for you database type: https://github.com/quartz-scheduler/quartz/tree/master/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore.
For  messadging you need bot token and chat id.
### How to use:
After you saw a success logging information in terminal , use http://127.0.0.1:8080/schendule url in you web brouser. Prefer to user Chrome , because some input elements with date and time parametrs will not works in other browers.  
