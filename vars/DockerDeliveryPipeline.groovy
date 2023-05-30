def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    pipeline {
        agent any
        environment {
            registryURI = 'https://registry.hub.docker.com/'
            registry = 'sarangp007/sarang_cloudethix_nginx'
            registryCredential = 'dh_creds_dev'
        //platform = getPlatformName()
        }
        stages {
            stage('Building image from project dir from shared Lib') {
                environment {
                    registry_endpoint = "${env.registryURI}" + "${env.registry}"
                    tag_commit_id     = "${env.registry}" + ":$GIT_COMMIT"
                }
                steps {
                    script {
                        //sh 'echo "${env.platform}"'
                        def app = docker.build(tag_commit_id)
                        docker.withRegistry( registry_endpoint, registryCredential ) {
                            app.push()
                        }
                    }
                }
            }
            stage('Remove Unused docker image from shared Lib') {
                steps {
                    sh "docker rmi $registry:$GIT_COMMIT"
                }
            }
        }
        post {
            always {
                echo 'Deleting Workspace from shared Lib'
                deleteDir() /* clean up our workspace */
            }
        }
    }
}

