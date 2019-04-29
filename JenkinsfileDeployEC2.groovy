pipeline{

    agent { label "master" }

    parameters {
        choice(name: 'ACCOUNT', choices: ['351098335058','272318516296'], description: 'AWS Account')
    }

    environment {
        IS_JENKINS_MODE = "true"

    }

    stages {        
        stage('Assume Jekins Role') {
            steps {
                sh '''
                    aws cloudformation list-stack-sets
                '''
            }
            post {
                failure {
                    script {
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }

        stage('Create EC2') {
            steps {
                sh '''
                    terraform -v
                '''
            }
            post {
                failure {
                    script {
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
    }
}