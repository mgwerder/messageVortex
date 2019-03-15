pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'50')) 
    disableConcurrentBuilds()
  }
  stages {
    stage ('Initialize') {
      steps {
        git credentialsId: 'github_readonly', url: 'ssh://git@github.com/mgwerder/messageVortex_internal'
        sh 'mvn clean' 
      }
    }
    stage ('Build') {
      steps {
        sh 'mvn -DskipTests compile' 
      }
    }
    stage ('Test') {
      steps{
        sh 'mvn -pl application-core-library jacoco:prepare-agent test jacoco:report'
      }
      post {
        success {
          junit 'application-core-library/target/surefire-reports/TEST-*.xml'
          jacoco changeBuildStatus: true, classPattern: 'application-core-library/target/classes', execPattern: 'application-core-library/target/**.exec', inclusionPattern: '**/*.class', minimumBranchCoverage: '50', minimumClassCoverage: '50', minimumComplexityCoverage: '50', minimumLineCoverage: '70', minimumMethodCoverage: '50', sourcePattern: 'application-core-library/src/main/java,application-core-library/src/test/java'

        }
      }
    }
    stage ('Package all') {
      steps {
        sh 'mkdir /var/www/messagevortex/devel/repo || /bin/true'
        sh 'mvn -DskipTests package'
      }
    }
    stage('SonarQube analysis') {
      steps {
        /* withSonarQubeEnv('SonarQube') {
          sh "mvn sonar:sonar"
        }*/
        sh '/bin/true'
      }
    }
  }
  post {
    always {
      publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'application-core-library/target/surefire-reports', reportFiles: 'index.html', reportName: 'MessageVortex Report', reportTitles: 'MessageVortex'])
    }
    success {
      archiveArtifacts artifacts: 'application-core-library/target/*.jar,thesis/target/main/latex/**/*.pdf', fingerprint: true

    }
  }
}