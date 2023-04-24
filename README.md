# Sampler-0

## Section 1 - Instructions:

cd ~/Downloads && git clone https://github.com/th-bsu/Sampler-0.git
cd ~/Downloads/Sampler-0/Java-23-02-05-Template-Implicit/Chapter10
[reference all configuration values in Section 2]
[load all Configuration value(s) into their placeholder (i.e. Java), except for RepositoryApplication.kt (Kotlin)]
[install all dependencies (optionally), if needed to run all steps below]
clear && clear && sudo docker rm -f $(sudo docker ps -aq); ./gradlew clean build && sudo docker-compose build && sudo ./test-em-all.bash start
[enter password, as required by sudo]
[wait until all start-up tests pass (i.e. re-run if needed, until all tests pass)]
[open Android Studio]
[open project under ~/Downloads/Sampler-0/Kotlin]
[hopefully, everything would have been loaded correctly]
[issue ifconfig on Linux command line]
[load value from enp0s31f6:inet field into XXX.XXX.XXX.XXX, in RepositoryApplication.kt]
[load all Configuration value(s) into their placeholder (i.e. Kotlin)]
[run sample test: MainActivityTest_Template_Explicit.kt]
[wait until all tests pass]

## Section 2 - Data:

  Configuration For Place Holder:
     Location                                                                                                   Entry                             Place Holder                                               Value
     ...Template-Implicit/Chapter10/spring-cloud/gateway/src/main/resources/application.yml:                    server.port:                      ABCD                                                       8080
                                                                                                                eureka: client: serviceUrl:       defaultZone: http://${app.eureka-server}:ABCD/eureka/      8761
                                                                                                                id: eureka-api,        uri:       http://${app.eureka-server}:ABCD                           8761
                                                                                                                id: eureka-web-start,  uri:       http://${app.eureka-server}:ABCD                           8761
                                                                                                                id: eureka-web-other,  uri:       http://${app.eureka-server}:ABCD                           8761
     ...Template-Implicit/Chapter10/microservices/product-composite-service/src/main/resources/application.yml: server.port:                      ABCD                                                       7000
                                                                                                                eureka: client: serviceUrl:       defaultZone: http://${app.eureka-server}:ABCD/eureka/      8761
                                                                                                                defaultBrokerPort:                ABCD                                                       9092
                                                                                                                spring.rabbitmq: port:            ABCD                                                       5672
                                                                                                                server.port:                      ABCD                                                       8080
     ...Template-Implicit/Chapter10/microservices/product-service/src/main/resources/application.yml:           server.port:                      ABCD                                                       7001
                                                                                                                eureka: client: serviceUrl:       defaultZone: http://${app.eureka-server}:ABCD/eureka/      8761
                                                                                                                spring.data.mongodb:port:         ABCD                                                       27017
                                                                                                                spring.cloud.stream.kafka.binder: brokers: x.x.x.x                                           127.0.0.1
                                                                                                                spring.cloud.stream.kafka.binder: defaultBrokerPort: ABCD                                    9092
                                                                                                                spring.rabbitmq: host:            x.x.x.x                                                    127.0.0.1
                                                                                                                spring.rabbitmq: port:            ABCD                                                       5672
                                                                                                                server.port:                      ABCD                                                       8080
     ...Template-Implicit/Chapter10/spring-cloud/eureka-server/src/main/resources/application.yml:              server.port:                      ABCD                                                       8761
     ...Template-Implicit/Chapter10/docker-compose.yml:                                                         gateway:   ports:                 ABCD:ABCD                                                  8080:8080
                                                                                                                mongodb:   ports:                 ABCD:ABCD                                                  27017:27017
                                                                                                                mysql:     ports:                 ABCD:ABCD                                                  3306:3306
                                                                                                                rabbitmq:  ports:                 ABCD:ABCD, ABCD:ABCD                                       5672:5672, 15672:15672
     ...Template-Implicit/Chapter10/docker-compose-kafka.yml:                                                   gateway:   ports:                 ABCD:ABCD                                                  8080:8080
                                                                                                                mongodb:   ports:                 ABCD:ABCD                                                  27017:27017
                                                                                                                mysql:     ports:                 ABCD:ABCD                                                  3306:3306
                                                                                                                kafka:     ports:                 ABCD:ABCD                                                  9092:9092
                                                                                                                zookeeper: ports:                 ABCD:ABCD                                                  2181:2181
     ...Template-Implicit/Chapter10/docker-compose-partitions.yml:                                              gateway:   ports:                 ABCD:ABCD                                                  8080:8080
                                                                                                                mongodb:   ports:                 ABCD:ABCD                                                  27017:27017
                                                                                                                mysql:     ports:                 ABCD:ABCD                                                  3306:3306
                                                                                                                rabbitmq:  ports:                 ABCD:ABCD, ABCD:ABCD                                       5672:5672, 15672:15672
     ...Template-Implicit/Chapter10/microservices/product-composite-service/src/test/.../MessagingTests.java:   createCompositeProduct1:          "ABCD/127.0.1.1:0"                                         'hostname' output from terminal
                                                                                                                createCompositeProduct2:          "ABCD/127.0.1.1:0"                                         'hostname' output from terminal
     ...Template-Implicit/Chapter10/microservices/recommendation-service/src/main/resources/application.yml:    server.port:                      ABCD                                                       7002
                                                                                                                eureka:client:serviceUrl:         defaultZone: http://${app.eureka-server}:ABCD/eureka/      8761
                                                                                                                spring.data.mongodb:port:         ABCD                                                       27017
                                                                                                                defaultBrokerPort:                ABCD                                                       9092
                                                                                                                spring.rabbitmq:port:             ABCD                                                       5672
                                                                                                                server.port:                      ABCD                                                       8080
     ...Template-Implicit/Chapter10/microservices/review-service/src/main/resources/application.yml:            server.port:                      ABCD                                                       7003
                                                                                                                eureka: client: serviceUrl:       defaultZone: http://${app.eureka-server}:ABCD/eureka/      8761
                                                                                                                defaultBrokerPort:                ABCD                                                       9092
                                                                                                                spring.rabbitmq: port:            ABCD                                                       5672
                                                                                                                server.port:                      ABCD                                                       8080
     ...Kotlin/app/src/main/java/com/th/chapter_11/application/RepositoryApplication.kt                         baseUrl:                          http://XXX.XXX.XXX.XXX:ABCD                                'ifconfig:enp0s31f6:inet' output from terminal, 8080



