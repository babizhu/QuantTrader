FROM ubuntu:latest                                                                        │drwxrwxr-x  3 liulaoye liulaoye 4.0K 9月   5 16:51 monitor-dashboard
                                                                                          │drwxrwxr-x  3 liulaoye liulaoye 4.0K 9月   5 16:51 order-microservice
MAINTAINER liulaoye,http://www.cnblogs.com/liulaoye                                       │drwxrwxr-x  3 liulaoye liulaoye 4.0K 9月   5 16:51 payment-microservice
                                                                                          │-rw-rw-r--  1 liulaoye liulaoye 8.1K 5月  10 14:19 pom.xml
#Install sshd net-tools                                                                   │drwxrwxr-x  3 liulaoye liulaoye 4.0K 9月   5 16:51 product-microservice
RUN apt update && \                                                                       │-rw-rw-r--  1 liulaoye liulaoye 2.4K 5月  10 14:19 README.md
apt install wget -y && \                                                                  │-rw-rw-r--  1 liulaoye liulaoye 1.4K 5月  10 14:19 README.zh-cn.md
apt install tar -y && \                                                                   │drwxrwxr-x  2 liulaoye liulaoye 4.0K 9月   5 16:51 recommendation-microservice
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-secureback│drwxrwxr-x  3 liulaoye liulaoye 4.0K 9月   5 16:51 shopping-cart-microservice
up-cookie" http://download.oracle.com/otn-pub/java/jdk/8u144-b01/090f390dda5b47b9b721c7dfa│drwxrwxr-x  3 liulaoye liulaoye 4.0K 9月   5 16:51 shopping-ui
a008135/jdk-8u144-linux-x64.tar.gz wget -qO- | tar -zx -C /opt/ && \                      │drwxrwxr-x  3 liulaoye liulaoye 4.0K 9月   5 16:51 store-microservice
                                                                                          │-rw-rw-r--  1 liulaoye liulaoye  629 5月  10 14:19 TODO.md
wget https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz && \      │-rw-rw-r--  1 liulaoye liulaoye  143 5月  10 14:19 .travis.yml
tar zxvf mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz && \                                   │-rw-rw-r--  1 liulaoye liulaoye 6.6K 9月   5 16:51 vertx-blueprint-microservice.iml
mkdir /opt/quant-trade && \                                                               │➜  vertx-blueprint-microservice git:(master) ✗ cd doc              
mv ./mongodb-linux-x86_64-ubuntu1604-3.4.9 /opt/quant-trade && \                          │cd: 没有那个文件或目录: doc
                                                                                          │➜  vertx-blueprint-microservice git:(master) ✗ cd docker 
apt-get install -y python3 && \                                                           │➜  docker git:(master) ✗ l
apt-get install -y python3-pip && \                                                       │总用量 24K
pip3 install --upgrade pip && \                                                           │drwxrwxr-x  3 liulaoye liulaoye 4.0K 6月   8 14:53 .
pip3 install lxml && \                                                                    │drwxrwxr-x 20 liulaoye liulaoye 4.0K 9月   5 16:51 ..
pip3 install pandas && \                                                                  │-rwxrwxr-x  1 liulaoye liulaoye  825 5月  10 14:19 build.sh
pip3 install requests && \                                                                │drwxrwxr-x  3 liulaoye liulaoye 4.0K 5月  10 14:19 config-server
pip3 install bs4 && \                                                                     │-rw-rw-r--  1 liulaoye liulaoye 3.3K 6月   8 14:53 docker-compose.yml
pip3 install tushare && \                                                                 │-rwxrwxr-x  1 liulaoye liulaoye  758 5月  10 14:19 run.sh
apt remove python3-pip -y && \                                                            │➜  docker git:(master) ✗ less build.sh 
apt remove tar -y && \                                                                    │➜  docker git:(master) ✗ vim run.sh 
apt remove wget -y && \                                                                   │➜  docker git:(master) ✗ docker-containerd
apt autoremove && \                                                                       │➜  docker git:(master) ✗ less run.sh 
apt autoclean && \                                                                        │➜  docker git:(master) ✗ less docker-compose.yml 
apt clean && \                                                                            │➜  docker git:(master) ✗ docke
rm mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz ** \                                         │➜  docker git:(master) ✗ cd   
rm -rf /var/lib/apt/lists/*                                                               │➜  ~ cd docker/quant_trade 
rm -rf /root/*                                                                            │➜  quant_trade ll
                                                                                          │总用量 8.0K
ENV JAVA_VERSION=8u144 \                                                                  │-rw-rw-r-- 1 liulaoye liulaoye 1.4K 10月 12 16:14 Dockerfile
    JAVA_VERSION_PREFIX=1.8.0_144                                                         │-rw-rw-r-- 1 liulaoye liulaoye  928 10月 12 11:38 Dockerfile.1
                                                                                          │➜  quant_trade cp Dockerfile Dockerfile.2
ENV JAVA_HOME=/opt/jdk$JAVA_VERSION_PREFIX                                                │➜  quant_trade df -u
                                                                                          │df：无效选项 -- u
                                                                                          │Try 'df --help' for more information.
ENV PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH                                                │➜  quant_trade df -h
#Set password for root                                                                    │文件系统        容量  已用  可用 已用% 挂载点
#RUN echo 'root:iloveworld' | chpasswd                                                    │udev            2.8G     0  2.8G    0% /dev
#RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_confi│tmpfs           577M   16M  561M    3% /run
g                                                                                         │/dev/sda5        35G   28G  5.4G   84% /
                                                                                          │tmpfs           2.9G  295M  2.6G   11% /dev/shm
#Set history record                                                                       │tmpfs           5.0M  4.0K  5.0M    1% /run/lock
#ENV HISTTIMEFORMAT "%F %T  "