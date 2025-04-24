FROM tensorflow/tensorflow:latest-jupyter
LABEL maintainer "Carlos M. Abreu <magno.mabreu@gmail.com>"

RUN mkdir /home/oracle
COPY ./ta-lib_0.6.4_amd64.deb /home/oracle

WORKDIR /home/oracle

RUN apt update && apt install -y wget

RUN wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb
RUN dpkg -i jdk-21_linux-x64_bin.deb && rm jdk-21_linux-x64_bin.deb	

RUN dpkg -i ta-lib_0.6.4_amd64.deb

RUN pip install --upgrade pip

RUN pip install \
	matplotlib \
	matplotlib-inline \
	matplotlib-venn \
	pandas \
	pandas-datareader \
	pandas-gbq \
	pandas-stubs \
	pandas_ta \
	yfinance \
	sklearn-compat \
	sklearn-pandas \
	keras \
	TA-Lib \
	tensorflowjs \
	--timeout=1000
	
COPY ./oracle-train.py /home/oracle
COPY ./oracle-use.py /home/oracle
COPY ./target/oracle-1.0.war /opt/lib/
RUN chmod 777 /home/oracle/oracle-train.py && chmod 777 /home/oracle/oracle-use.py


ENTRYPOINT ["java"]
ENV LANG=pt_BR.utf8 
CMD ["-jar", "/opt/lib/oracle-1.0.war"]	