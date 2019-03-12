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
				sh 'mvn test'
			}
            post {
                success {
        			junit 'application-core-library/target/surefire-reports/TEST-*.xml' 
				}
            }
		}
        stage ('Package all') {
            steps {
                sh 'mvn -DskipTests deploy' 
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
			jacoco changeBuildStatus: true, classPattern: 'application-core-library/target/classes', execPattern: 'application-core-library/target/**.exec', inclusionPattern: '**/*.class', minimumBranchCoverage: '50', minimumClassCoverage: '50', minimumComplexityCoverage: '50', minimumLineCoverage: '70', minimumMethodCoverage: '50', sourcePattern: 'application-core-library/src/main/java,application-core-library/src/test/java'                
			publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'application-core-library/target/surefire-reports', reportFiles: 'index.html', reportName: 'MessageVortex Report', reportTitles: 'MessageVortex'])
		}
	}
}