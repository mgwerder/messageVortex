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
					jacoco changeBuildStatus: true, classPattern: 'application-core-library/target/classes', execPattern: 'application-core-library/target/**.exec', inclusionPattern: '**/*.class', minimumBranchCoverage: '50', minimumClassCoverage: '50', minimumComplexityCoverage: '50', minimumLineCoverage: '70', minimumMethodCoverage: '50', sourcePattern: 'application-core-library/src/main/java,application-core-library/src/test/java'                
				}
            }
		}
		stage('SonarQube analysis') {
			// requires SonarQube Scanner 2.8+
			def scannerHome = tool 'SonarQube Scanner 2.8';
			withSonarQubeEnv('My SonarQube Server') {
				sh "$/opt/sonar-scanner/bin/sonar-scanner/bin/sonar-scanner"
			}
		}
		stage("Sonar Quality Gate"){
			timeout(time: 10, unit: 'MINUTES') { // Just in case something goes wrong, pipeline will be killed after a timeout
				def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
				if (qg.status != 'OK') {
					error "Pipeline aborted due to quality gate failure: ${qg.status}"
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