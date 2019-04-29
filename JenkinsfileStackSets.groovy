pipeline{

    agent { label "master" }

    parameters {
        choice(name: 'ACCOUNT', choices: ['351098335058','351098335058'], description: 'AWS Account')
    }

    environment {
        IS_JENKINS_MODE = "true"

    }

    stages {        
        stage('Apply StackSet') {
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
    }
}