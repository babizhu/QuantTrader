FROM ubuntu:latest

MAINTAINER liulaoye,http://www.cnblogs.com/liulaoye

COPY ./res/ /opt/quant-trade/
#Install sshd net-tools
RUN apt update && \
apt-get install wget -y && \
#wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u144-b01/090f390dda5b47b9b721c7dfaa008135/jdk-8u144-linux-x64.tar.gz -qO- | tar -zx -C /opt/ && \
tar zxvf /opt/quant-trade/jdk-8u152-linux-x64.tar.gz && \
mv ./jdk1.8.0_152 /opt/ && \
wget https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz && \
tar zxvf mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz && \
mv ./mongodb-linux-x86_64-ubuntu1604-3.4.9 /opt/quant-trade && \
rm /opt/quant-trade/jdk-8u152-linux-x64.tar.gz && \


apt-get install -y python3 && \
apt-get install -y python3-pip && \
pip3 install --upgrade pip && \
pip3 install lxml && \
pip3 install pandas && \
pip3 install requests && \
pip3 install bs4 && \
pip3 install tushare && \
pip3 install tornado && \
apt remove python3-pip -y && \
apt remove wget -y && \
apt autoremove -y && \
apt autoclean -y && \
#apt clean && \
rm mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz && \
rm -rf /var/lib/apt/lists/* && \
mkdir /opt/quant-trade/mongodb-linux-x86_64-ubuntu1604-3.4.9/data && \
mkdir /opt/quant-trade/mongodb-linux-x86_64-ubuntu1604-3.4.9/data/db && \
rm -rf /root/*

ENV JAVA_VERSION=8u152 \
    JAVA_VERSION_PREFIX=1.8.0_152

ENV JAVA_HOME=/opt/jdk$JAVA_VERSION_PREFIX


ENV PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

EXPOSE 8080

#Set password for root
#RUN echo 'root:iloveworld' | chpasswd
#RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

#Set history record
#ENV HISTTIMEFORMAT "%F %T  "


#Change timezone CST
#RUN \cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

#Open 22 port
#EXPOSE 22

#Auto running sshd service
#CMD ["/usr/sbin/sshd","-D"]