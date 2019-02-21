FROM ubuntu:bionic

MAINTAINER Martin Gwerder

# install all packages
RUN mkdir /var/tmp/messagevortex && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install \
               texlive-latex-base texlive-generic-recommended texlive-extra-utils texlive-latex-extra \
               texlive-bibtex-extra texlive-luatex \
               texlive-lang-german texlive-lang-english \
               pandoc calibre enscript \
               fig2dev \
               wget curl joe bc imagemagick \
               python-pip live-build linuxbrew-wrapper \
               openjdk-8-jdk-headless  \
               maven && \
    pip install xml2rfc && \
    sed 's/domain="coder" rights="none"/domain="coder" rights="read|write"/' </etc/ImageMagick-6/policy.xml >/tmp/policy.xml && mv /tmp/policy.xml /etc/ImageMagick-6/policy.xml 
    
VOLUME /var/tmp/messagevortex

CMD [ "/var/tmp/messagevortex/buildenv/runDockerTasks.sh" ]