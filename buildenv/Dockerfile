FROM ubuntu:24.04

MAINTAINER Martin Gwerder

# Create working directory and install base packages
RUN mkdir /var/tmp/messagevortex

# Install system packages in layers for better caching
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install software-properties-common && \
 printf '%s\n' \
      "deb http://archive.ubuntu.com/ubuntu noble main restricted universe multiverse" \
      "deb http://archive.ubuntu.com/ubuntu noble-updates main restricted universe multiverse" \
      "deb http://archive.ubuntu.com/ubuntu noble-backports main restricted universe multiverse" \
      "deb http://security.ubuntu.com/ubuntu noble-security main restricted universe multiverse" \
      > /etc/apt/sources.list && apt-get update

RUN DEBIAN_FRONTEND=noninteractive apt-get -y install zip unzip

# Install build tools and basic dependencies
RUN set -eux && DEBIAN_FRONTEND=noninteractive apt-get -y install --no-install-recommends build-essential curl file git sudo wget bc rsync openjdk-8-jdk-headless maven python3 python3-pip

# Install LaTeX packages (Ubuntu 24.04 versions)
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install --no-install-recommends \
    texlive-latex-base \
    texlive-extra-utils texlive-latex-extra texlive-publishers texlive-fonts-extra \
    texlive-bibtex-extra texlive-luatex texlive-lang-german texlive-lang-english \
    texlive-science cm-super xzdec biber

# Install graphics and document processing tools  
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install --no-install-recommends \
    ghostscript gsfonts gsfonts-other gsfonts-x11 \
    pandoc calibre enscript fig2dev imagemagick inkscape \
    libcairo2 libpango-1.0-0 libpangocairo-1.0-0 libgdk-pixbuf2.0-0 libffi-dev shared-mime-info \
    python3-cairo-dev genisoimage live-build

# Install Maven plugins and libraries
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install --no-install-recommends \
    libmaven-javadoc-plugin-java libmaven-archiver-java libmaven-clean-plugin-java \
    libmaven-compiler-plugin-java libmaven-common-artifact-filters-java \
    libmaven-dependency-analyzer-java libmaven-dependency-tree-java \
    libmaven-deploy-plugin-java libmaven-exec-plugin-java libmaven-invoker-java \
    libmaven-jar-plugin-java libmaven-plugin-testing-java libmaven-plugin-tools-java \
    libmaven-reporting-impl-java libmaven-repository-builder-java \
    libmaven-resources-plugin-java libmaven-shared-jar-java libmaven-site-plugin-java \
    libmaven-verifier-java libmaven3-core-java libproperties-maven-plugin-java \
    maven-repo-helper lib32ncurses6 lib32z1

# Clean up apt cache to reduce image size
RUN apt-get clean && rm -rf /var/lib/apt/lists/*
# Install Python packages with pinned versions (Ubuntu 24.04 requires --break-system-packages)
RUN pip3 install --break-system-packages xml2rfc==3.15.3 'weasyprint>=0.53.1,<54.0'

# Configure ImageMagick policy for PDF processing
RUN sed 's/domain="coder" rights="none"/domain="coder" rights="read|write"/' </etc/ImageMagick-6/policy.xml >/tmp/policy.xml && \
    mv /tmp/policy.xml /etc/ImageMagick-6/policy.xml

# Copy build files
COPY mavenfiles.tar /tmp/mavenfiles.tar
COPY AdobeICCProfilesWin_end-user.zip /tmp/AdobeICCProfilesWin_end-user.zip

# Configure Maven and install color profiles
RUN cd /var/tmp/messagevortex && \
    tar -xvf /tmp/mavenfiles.tar && \
    cat /etc/maven/settings.xml|sed 's~.xsd">~.xsd"><localRepository>/var/lib/maven</localRepository>~' >/etc/maven/settings.xml.new && \
    mv /etc/maven/settings.xml.new /etc/maven/settings.xml && \
    mkdir -p /var/lib/maven/.m2

# Install Adobe color profiles for PDF/A compliance
RUN unzip -c /tmp/AdobeICCProfilesWin_end-user.zip "Adobe ICC Profiles (end-user)/RGB Profiles/AdobeRGB1998.icc" \
    >$(dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1))/AdobeRGB1998.icc && \
    sed "s~/ICCProfile (srgb.icc)~/ICCProfile ($(dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1))/AdobeRGB1998.icc)~" \
    <$(dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1))/PDFA_def.ps \
    >/tmp/PDFA_def.ps.new && \
    mv /tmp/PDFA_def.ps.new $(dirname $(find /usr/share/ghostscript/*.*/lib/ -type f|head -n 1))/PDFA_def.ps

# Pre-download Maven dependencies for faster builds
RUN cd /var/tmp/messagevortex && \
    MAVEN_CONFIG=/var/lib/maven/ mvn dependency:go-offline dependency:resolve-plugins

# Configure TexLive with pinned repository versions for reproducibility (2023 version for Ubuntu 24.04 compatibility)
RUN mkdir -p /root && \
    rm -rf /root/texmf 2>/dev/null || true && \
    tlmgr --repository https://ftp.tu-chemnitz.de/pub/tug/historic/systems/texlive/2023/tlnet-final init-usertree && \
    tlmgr --repository https://ftp.tu-chemnitz.de/pub/tug/historic/systems/texlive/2023/tlnet-final update --self && \
    tlmgr install --repository https://ftp.tu-chemnitz.de/pub/tug/historic/systems/texlive/2023/tlnet-final pdfx colorprofiles acmart && \
    tlmgr option repository https://mirror.ctan.org/systems/texlive/tlnet && \
    tlmgr update --self && \
    tlmgr update --list && \
    tlmgr update --all || true

# Clean up temporary files
RUN rm -f /tmp/mavenfiles.tar /tmp/AdobeICCProfilesWin_end-user.zip
CMD [ "/var/tmp/messagevortex/buildenv/runDockerTasks.sh" ]
