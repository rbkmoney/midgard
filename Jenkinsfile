#!groovy
build('midgard-service', 'java-maven') {
    checkoutRepo()
    loadBuildUtils()

    def javaServicePipeline
    runStage('load JavaService pipeline') {
        javaServicePipeline = load("build_utils/jenkins_lib/pipeJavaService.groovy")
    }

    def serviceName = env.REPO_NAME
    def mvnArgs = '-DjvmArgs="-Xmx256m"'

    javaServicePipeline(serviceName, useJava11, mvnArgs)
}