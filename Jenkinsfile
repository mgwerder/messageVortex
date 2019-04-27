/***
 * Jenkinsfile for pipeline build of all messagevortex paths
 ***/
pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'20'))
    disableConcurrentBuilds()
  }
  stages {
    stage ('Initialize') {
      steps {
        //git credentialsId: 'github_readonly', url: 'ssh://git@github.com/mgwerder/messageVortex_internal'
        checkout scm
        sh 'mvn clean'
      }
    }
    stage ('Build') {
      steps {
        sh 'mvn -DskipTests compile'
      }
    }
    stage ('Test') {
      parallel {
        stage ('Test on JDK8') {
          options {
            timeout(time: 120, unit: 'MINUTES')
          }
          steps{
                sh 'mvn -pl application-core-library jacoco:prepare-agent test jacoco:report'
                sh 'mvn -pl application-core-library site'
          }
          post {
            success {
              junit 'application-core-library/target/surefire-reports/TEST-*.xml'
              jacoco changeBuildStatus: true, classPattern: 'application-core-library/target/classes', execPattern: 'application-core-library/target/**.exec', inclusionPattern: '**/*.class', minimumBranchCoverage: '50', minimumClassCoverage: '50', minimumComplexityCoverage: '50', minimumLineCoverage: '70', minimumMethodCoverage: '50', sourcePattern: 'application-core-library/src/main/java,application-core-library/src/test/java'
              publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'application-core-library/target/site', reportFiles: 'index.html', reportName: 'MessageVortex Report', reportTitles: 'MessageVortex'])
            }
          }
        }
        stage ('Test on JDK10') {
          agent {
            docker {
                image 'maven:3.6.0-jdk-10'
                args '--mount type=bind,source="$HOME/.m2",target="/root/.m2" --mount type=bind,source="$HOME/MesageVortexKeyCache",target="/root/keyCache"'
            }
          }
          options {
            timeout(time: 30, unit: 'MINUTES')
          }
          steps{
                sh 'mvn -pl application-core-library jacoco:prepare-agent test jacoco:report'
          }
        }
        stage ('Test on JDK11') {
          agent {
            docker {
                image 'maven:3.6.0-jdk-11-slim'
                args '--mount type=bind,source="$HOME/.m2",target="/root/.m2" --mount type=bind,source="$HOME/MesageVortexKeyCache",target="/root/keyCache"'
            }
          }
          options {
            timeout(time: 30, unit: 'MINUTES')
          }
          steps{
            script {
              //if (env.BRANCH_NAME != 'master') {
                sh script: 'mvn -pl application-core-library jacoco:prepare-agent test jacoco:report'
              //}
            }
          }
        }
        stage ('Test on JDK12') {
          agent {
            docker {
                image 'maven:3.6.0-jdk-12'
                args '--mount type=bind,source="$HOME/.m2",target="/root/.m2" --mount type=bind,source="$HOME/MesageVortexKeyCache",target="/root/keyCache"'
            }
          }
          options {
            timeout(time: 30, unit: 'MINUTES')
          }
          steps {
            script {
              //if (env.BRANCH_NAME != 'master') {
                sh script: 'mvn -pl application-core-library jacoco:prepare-agent test jacoco:report'
              //}
            }
          }
        }
        stage ('Test on JDK13') {
          agent {
            docker {
                image 'maven:3.6.0-jdk-13'
                args '--mount type=bind,source="$HOME/.m2",target="/root/.m2" --mount type=bind,source="$HOME/MesageVortexKeyCache",target="/root/keyCache"'
            }
          }
          options {
            timeout(time: 30, unit: 'MINUTES')
          }
          steps {
            script {
              //if (env.BRANCH_NAME != 'master') {
                sh script: 'mvn -pl application-core-library jacoco:prepare-agent test jacoco:report'
              //}
            }
          }
        }
      }
    }
    stage ('Package all') {
      steps {
        sh 'mkdir /var/www/messagevortex/devel/repo || /bin/true'
        sh 'mvn -DskipTests compile package'
      }
    }
    stage ('Site build') {
      steps {
        sh 'mvn -pl application-core-library -DskipTests site'
      }
    }
    stage('SonarQube analysis') {
      steps {
        withSonarQubeEnv('SonarQube') {
          sh "mvn sonar:sonar"
        }
      }
    }
    stage('Publish artifacts') {
      steps {
        sh 'mvn -pl application-core-library -DskipTests dependency:copy-resources@publish-artifacts'
      }
    }
  }
  post {
    success {
      archiveArtifacts artifacts: 'application-core-library/target/*.jar,application-core-library/target/*.exe,application-core-library/target/*.dmg,thesis/target/main/latex/**/*.pdf*', fingerprint: true
    }
  }
}
