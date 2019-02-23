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
               python-cairo-dev libcairo2 libpango-1.0-0 libpangocairo-1.0-0 libgdk-pixbuf2.0-0 libffi-dev shared-mime-info \
               openjdk-8-jdk-headless  \
               maven && \
    pip install xml2rfc 'weasyprint<=0.42.3' && \
    sed 's/domain="coder" rights="none"/domain="coder" rights="read|write"/' </etc/ImageMagick-6/policy.xml >/tmp/policy.xml && mv /tmp/policy.xml /etc/ImageMagick-6/policy.xml 
COPY mavenfiles.tar /tmp/mavenfiles.tar
RUN cd /var/tmp/messagevortex && \
    tar -xvf /tmp/mavenfiles.tar && \
    cat /etc/maven/settings.xml|sed 's~.xsd">~.xsd"><localRepository>/var/lib/maven</localRepository>~' >/etc/maven/settings.xml.new && mv /etc/maven/settings.xml.new /etc/maven/settings.xml && \
    mkdir -p /var/lib/maven/.m2 && \
    MAVEN_CONFIG=/var/lib/maven/ mvn dependency:go-offline dependency:resolve-plugins 
CMD [ "/var/tmp/messagevortex/buildenv/runDockerTasks.sh" ]