properties([
    parameters ([
        string(name: 'BUILD_NODE', defaultValue: 'omar-build', description: 'The build node to run on'),
        booleanParam(name: 'CLEAN_WORKSPACE', defaultValue: true, description: 'Clean the workspace at the end of the run')
    ]),
    pipelineTriggers([
            [$class: "GitHubPushTrigger"],
            pollSCM('H/2 * * * *')
    ])
])

node("${BUILD_NODE}"){

    stage("Checkout branch $BRANCH_NAME")
    {
        checkout(scm)
    }

    stage("Load Variables")
    {
        step ([$class: "CopyArtifact",
        projectName: "ossim-ci",
           filter: "common-variables.groovy",
           flatten: true])

        load "common-variables.groovy"
    }

    stage ("Assemble") {
        sh """
        gradle assemble \
            -PossimMavenProxy=${OSSIM_MAVEN_PROXY} \
            -x bootRepackage
        """
        archiveArtifacts "plugins/*/build/libs/*.jar"
    }

    stage ("OpenShift Tag Image")
    {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: 'openshiftCredentials',
                          usernameVariable: 'OPENSHIFT_USERNAME',
                          passwordVariable: 'OPENSHIFT_PASSWORD']])
        {
            // Run all tasks on the app. This includes pushing to OpenShift and S3.
            sh """
                gradle openshiftTagImage \
                    -PossimMavenProxy=${OSSIM_MAVEN_PROXY}

            """
        }
    }

        
   stage("Clean Workspace")
   {
      if ("${CLEAN_WORKSPACE}" == "true")
        step([$class: 'WsCleanup'])
   }
}
