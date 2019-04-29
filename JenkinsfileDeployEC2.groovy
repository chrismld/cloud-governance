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
                    aws sts assume-role --role-arn arn:aws:iam::123456789012:role/xaccounts3access --role-session-name s3-access-example
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
                    aws cloudformation create-stack --stack-name bax-application-ec2 --template-body file://ApplicationInstance.yml
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