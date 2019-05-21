FROM ubuntu:bionic

MAINTAINER Martin Gwerder

# install all packages
RUN mkdir /var/tmp/messagevortex && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install \
               texlive-latex-base texlive-generic-recommended texlive-extra-utils texlive-latex-extra \
               texlive-bibtex-extra texlive-luatex \
               texlive-lang-german texlive-lang-english \
               ghostscript gsfonts gsfonts-other gsfonts-x11 \
               pandoc calibre enscript \
               fig2dev \
               wget curl joe bc imagemagick rsync inkscape \
               python-pip live-build linuxbrew-wrapper \
               python-cairo-dev libcairo2 libpango-1.0-0 libpangocairo-1.0-0 libgdk-pixbuf2.0-0 libffi-dev shared-mime-info \
               openjdk-8-jdk-headless  \
               genisoimage \
               maven libmaven-javadoc-plugin-java libmaven-archiver-java libmaven-clean-plugin-java libmaven-compiler-plugin-java libmaven-clean-plugin-java libmaven-common-artifact-filters-java libmaven-dependency-analyzer-java libmaven-dependency-tree-java libmaven-deploy-plugin-java libmaven-exec-plugin-java libmaven-invoker-java libmaven-jar-plugin-java libmaven-plugin-testing-java libmaven-plugin-tools-java libmaven-reporting-impl-java libmaven-repository-builder-java libmaven-resources-plugin-java libmaven-shared-jar-java libmaven-site-plugin-java libmaven-verifier-java libmaven3-core-java libproperties-maven-plugin-java maven-repo-helper && \               
    pip install xml2rfc 'weasyprint<=0.42.3' && \
    sed 's/domain="coder" rights="none"/domain="coder" rights="read|write"/' </etc/ImageMagick-6/policy.xml >/tmp/policy.xml && mv /tmp/policy.xml /etc/ImageMagick-6/policy.xml 
COPY mavenfiles.tar /tmp/mavenfiles.tar
COPY AdobeICCProfilesWin_end-user.zip /tmp/AdobeICCProfilesWin_end-user.zip
RUN cd /var/tmp/messagevortex && \
    tar -xvf /tmp/mavenfiles.tar && \
    cat /etc/maven/settings.xml|sed 's~.xsd">~.xsd"><localRepository>/var/lib/maven</localRepository>~' >/etc/maven/settings.xml.new && mv /etc/maven/settings.xml.new /etc/maven/settings.xml && \
    mkdir -p /var/lib/maven/.m2 && \
    unzip -c /tmp/AdobeICCProfilesWin_end-user.zip  "Adobe ICC Profiles (end-user)/RGB Profiles/AdobeRGB1998.icc" >$(dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1) )/AdobeRGB1998.icc && \
    sed "s~/ICCProfile (srgb.icc)~/ICCProfile ($( dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1) )/AdobeRGB1998.icc)~" <$(dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1) )/PDFA_def.ps >/tmp/PDFA_def.ps.new && mv /tmp/PDFA_def.ps.new $( dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1) )/AdobeRGB1998.icc && \
    MAVEN_CONFIG=/var/lib/maven/ mvn dependency:go-offline dependency:resolve-plugins 
CMD [ "/var/tmp/messagevortex/buildenv/runDockerTasks.sh" ]