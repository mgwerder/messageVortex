pipeline {
    agent any
	options {
    	buildDiscarder(logRotator(numToKeepStr:'50')) 
	}
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
		        	jacoco classPattern: 'application-core-library/target/classes', execPattern: 'application-core-library/target/*.exec', sourcePattern: 'application-core-library/src/main/java/**/*.java,application-core-library/src/test/java/**/*.java'
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