pipeline {
    agent any
    stages {
        stage ('Initialize') {
            steps {
                git credentialsId: 'github_readonly', url: 'ssh://git@github.com/mgwerder/messageVortex_internal'
            }
        }
        stage ('Build') {
            steps {
                sh 'mvn -DskipTests install' 
            }
        }
		stage ('Test') {
			steps{
				sh 'mvn surefire:test'
			}
            post {
                success {
                    junit 'application-core-library/target/surefire-reports/TEST-*.xml' 
                }
            }
		}
    }
}