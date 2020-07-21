pipeline{
   agent {
	dockerfile {
		filename 'Dockerfile'
		additionalBuildArgs  "${GLOBAL_PROPERTIES_DOCKER_BUILD_ARGS}"
	}
   }
   stages {
        stage("Deploy to rbi-lime-test01 (054365201063)"){
            environment { 
                AWS_ACCOUNT_ID = '054365201063'
		ENVIRONMENT_NAME = "DEV01"
            }
            steps {
              sh "id"
              ansiColor('xterm') {
                sh "/dist/deploy.sh"
              }
            }
        }
    }
}

