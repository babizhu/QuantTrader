FROM ubuntu:latest

MAINTAINER liulaoye,http://www.bbz.com/liulaoye

#Install sshd net-tools
RUN apt update && \ 
apt install wget -y && \ 
apt install tar -y && \
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u144-b01/090f390dda5b47b9b721c7dfaa008135/jdk-8u144-linux-x64.tar.gz wget -qO- | tar -zx -C /opt/ && \ 

wget https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz && \ 
tar zxvf mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz && \
mkdir /opt/quant-trade && \
mv ./mongodb-linux-x86_64-ubuntu1604-3.4.9 /opt/quant-trade && \

apt-get install -y python3 && \
apt-get install -y python3-pip && \ 
pip3 install --upgrade pip && \
pip3 install lxml && \
pip3 install pandas && \
pip3 install requests && \
pip3 install bs4 && \
pip3 install tushare && \
apt remove python3-pip -y && \
apt remove tar -y && \
apt remove wget -y && \
apt autoremove && \
apt autoclean && \
apt clean && \
rm mongodb-linux-x86_64-ubuntu1604-3.4.9.tgz ** \
rm -rf /var/lib/apt/lists/* ** \
rm -rf /root/*

ENV JAVA_VERSION=8u144 \
    JAVA_VERSION_PREFIX=1.8.0_144

ENV JAVA_HOME=/opt/jdk$JAVA_VERSION_PREFIX


ENV PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
COPY ./res/* /opt/quant-trade
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