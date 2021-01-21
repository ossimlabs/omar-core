properties([
    parameters([
        string(name: 'DOCKER_REGISTRY_DOWNLOAD_URL', defaultValue: 'nexus-docker-private-group.ossim.io', description: 'Repository of docker images')
    ]),
    pipelineTriggers([
        [$class: "GitHubPushTrigger"]
    ]),
    [$class: 'GithubProjectProperty', displayName: '', projectUrlStr: 'https://github.com/ossimlabs/omar-core'],
    buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '20')),
    disableConcurrentBuilds()
])

podTemplate(
    containers: [
        containerTemplate(
            name: 'docker',
            image: 'docker:19.03.11',
            ttyEnabled: true,
            command: 'cat',
            privileged: true
        ),
        containerTemplate(
            image: "${DOCKER_REGISTRY_DOWNLOAD_URL}/alpine/helm:3.2.3",
            name: 'helm',
            command: 'cat',
            ttyEnabled: true
        ),
        containerTemplate(
            name: 'git',
            image: 'alpine/git:latest',
            ttyEnabled: true,
            command: 'cat',
            envVars: [
                envVar(key: 'HOME', value: '/root')
            ]
        )
    ],
    volumes: [
        hostPathVolume(
            hostPath: '/var/run/docker.sock',
            mountPath: '/var/run/docker.sock'
        ),
    ]
) {
  node(POD_LABEL) {
    stage("Checkout branch") {
      scmVars = checkout(scm)
      GIT_BRANCH_NAME = scmVars.GIT_BRANCH
      BRANCH_NAME = """${sh(returnStdout: true, script: "echo ${GIT_BRANCH_NAME} | awk -F'/' '{print \$2}'").trim()}"""
      sh """
        touch buildVersion.txt
        grep buildVersion gradle.properties | cut -d "=" -f2 > "buildVersion.txt"
      """
      preVERSION = readFile "buildVersion.txt"
      VERSION = preVERSION.substring(0, preVERSION.indexOf('\n'))

      GIT_TAG_NAME = "omar-core" + "-" + VERSION //webhook stuff
      ARTIFACT_NAME = "ArtifactName"

      script {
        if (BRANCH_NAME == 'master') {
          buildName "${VERSION} - ${BRANCH_NAME}"
        } else {
          buildName "${VERSION} - ${BRANCH_NAME}-SNAPSHOT"
        }
      }
    }

    stage("Load Variables") {
      withCredentials([string(credentialsId: 'o2-artifact-project', variable: 'o2ArtifactProject')]) {
        step([$class     : "CopyArtifact",
              projectName: o2ArtifactProject,
              filter     : "common-variables.groovy",
              flatten    : true])
      }

      load "common-variables.groovy"

      switch (BRANCH_NAME) {
        case "master":
          TAG_NAME = VERSION
          break

        case "dev":
          TAG_NAME = "latest"
          break

        default:
          TAG_NAME = BRANCH_NAME
          break
      }

      DOCKER_IMAGE_PATH = "${DOCKER_REGISTRY_PRIVATE_UPLOAD_URL}/omar-core"
    }

    stage("Assemble") {
      sh """
        ./gradlew assemble -PossimMavenProxy=${MAVEN_DOWNLOAD_URL}
      """
      archiveArtifacts "plugins/*/build/libs/*.jar"
      //archiveArtifacts "apps/*/build/libs/*.jar"
    }

    stage("Publish Nexus") {
      withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                        credentialsId   : 'nexusCredentials',
                        usernameVariable: 'MAVEN_REPO_USERNAME',
                        passwordVariable: 'MAVEN_REPO_PASSWORD']]) {
        sh """
          ./gradlew publish -PossimMavenProxy=${MAVEN_DOWNLOAD_URL}
        """
      }
    }

    /*
        stage ("Publish Docker App")
        {
            withCredentials([[$class: 'UsernamePasswordMultiBinding',
                            credentialsId: 'dockerCredentials',
                            usernameVariable: 'DOCKER_REGISTRY_USERNAME',
                            passwordVariable: 'DOCKER_REGISTRY_PASSWORD']])
            {
                // Run all tasks on the app. This includes pushing to OpenShift and S3.
                sh """
                ./gradlew pushDockerImage \
                    -PossimMavenProxy=${MAVEN_DOWNLOAD_URL}
                """
            }
        }

        try {
            stage ("OpenShift Tag Image")
            {
                withCredentials([[$class: 'UsernamePasswordMultiBinding',
                                credentialsId: 'openshiftCredentials',
                                usernameVariable: 'OPENSHIFT_USERNAME',
                                passwordVariable: 'OPENSHIFT_PASSWORD']])
                {
                    // Run all tasks on the app. This includes pushing to OpenShift and S3.
                    sh """
                        ./gradlew openshiftTagImage \
                            -PossimMavenProxy=${MAVEN_DOWNLOAD_URL}

                    """
                }
            }
        } catch (e) {
            echo e.toString()
        }
    */
  }
}
