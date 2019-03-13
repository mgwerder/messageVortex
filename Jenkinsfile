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
      }
    }
    stage ('Build') {
      steps {
        sh 'mvn -DskipTests compile' 
      }
    }
    stage ('Test') {
      steps{
        sh 'mvn jacoco:prepare-agent test jacoco:report'
      }
      post {
        success {
          junit 'application-core-library/target/surefire-reports/TEST-*.xml' 
        }
      }
    }
    stage ('Package all') {
      steps {
        sh 'mvn -DskipTests -Durl=file:///var/www/messagevortex/devel/repo -DrepositoryId=messagevortex -Dfile=messagevortex-1.0.jar deploy' 
      }
    }
    stage('SonarQube analysis') {
      steps {
        withSonarQubeEnv('local sonar instance') {
          sh "/opt/sonar-scanner/bin/sonar-scanner/bin/sonar-scanner"
        }
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: 'application-core-library/target/*.jar,thesis/target/main/latex/**/*.pdf', fingerprint: true
      publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'application-core-library/target/surefire-reports', reportFiles: 'index.html', reportName: 'MessageVortex Report', reportTitles: 'MessageVortex'])
    }
  }
}