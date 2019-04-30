pipeline{

    agent { label "master" }

    parameters {
        choice(name: 'ACCOUNT', choices: ['351098335058','272318516296'], description: 'AWS Account')
        choice(name: 'ENV', choices: ['dev','test','prod'], description: 'Environment')
        string(name: 'VPCId', description: 'VPC Id')
    }

    environment {
        IS_JENKINS_MODE = "true"

    }

    stages {        
        stage('Assume Jekins Role') {
            steps {
                sh '''
                    aws sts assume-role 
                    --role-arn arn:aws:iam::${ACCOUNT}:role/AWSJenkinsDeploymentRole 
                    --role-session-name jenkins
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

        stage('CloudFormation Lint') {
            steps {
                sh '''
                    cfn-lint --template ApplicationInstance.yml
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

        stage('CloudFormation Security') {
            steps {
                sh '''
                    cfn_nag_scan --input-path ApplicationInstance.yml
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
                    aws cloudformation deploy --stack-name bax-application-ec2 \
                    --template-file file://ApplicationInstance.yml \
                    --parameter-overrides \
                        Env="${ENV_TAG}" \
                        VPCId="${VPCID}" \
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