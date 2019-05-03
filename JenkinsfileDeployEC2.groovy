pipeline{

    agent { label "master" }

    parameters {
        choice(name: 'ACCOUNT', choices: ['351098335058','272318516296','097365445969','273434547788'], description: 'AWS Account')
        choice(name: 'REGION', choices: ['us-east-1','us-west-2'], description: 'AWS Region')
        choice(name: 'ENV', choices: ['dev','test','prod'], description: 'Environment')
        string(name: 'VPCID', description: 'VPC Id')
        string(name: 'AppSubnet', description: 'Subnet Id')
        string(name: 'AppServerAMI', description: 'AMI Id')
    }

    environment {
        IS_JENKINS_MODE = "true"

    }

    stages {        
        stage('Assume Jekins Role') {
            steps {
                script {
                    env.STSRESPONSE=sh(returnStdout: true, script: "aws sts assume-role --role-arn arn:aws:iam::${ACCOUNT}:role/AWSJenkinsDeploymentRole --role-session-name jenkins")
                    env.AWS_ACCESS_KEY_ID = sh(returnStdout: true, script: "echo \$STSRESPONSE | jq -r .Credentials.AccessKeyId").trim()
                    env.AWS_SECRET_ACCESS_KEY = sh (returnStdout: true, script: "echo \$STSRESPONSE | jq -r .Credentials.SecretAccessKey").trim()
                    env.AWS_SESSION_TOKEN = sh (returnStdout: true, script: "echo \$STSRESPONSE | jq -r .Credentials.SessionToken").trim()
                }
            }
            post {
                failure {
                    script {
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }

        stage('Set Defualt Region') {
            steps {
                sh '''
                    aws configure set default.region ${REGION}
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
                    --template-file ApplicationInstance.yml \
                    --parameter-overrides \
                        Env="${ENV}" \
                        Appname="ec2-app-instance" \
                        VPCId="${VPCID}" \
                        AppSubnet="${AppSubnet}" \
                        AppServerAMI=${AppServerAMI} \
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