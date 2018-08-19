openshift.withCluster() {
  env.NAMESPACE = openshift.project()
  env.POM_FILE = env.BUILD_CONTEXT_DIR ? "${env.BUILD_CONTEXT_DIR}/pom.xml" : "pom.xml"
  env.APP_NAME = "${env.JOB_NAME}".replaceAll(/-?pipeline-?/, '').replaceAll(/-?${env.NAMESPACE}-?/, '').replaceAll("/", '')
  echo "Starting Pipeline for ${APP_NAME}..."
  def projectBase = "${env.NAMESPACE}".replaceAll(/-build/, '')
  env.STAGE0 = "${projectBase}"
  env.STAGE1 = "${projectBase}"
  env.STAGE2 = "${projectBase}"
  env.STAGE3 = "${projectBase}"
  echo "POM ${POM_FILE}" 
  echo "STAGE0 ${env.STAGE0}"
}

pipeline {
  // Use Jenkins Maven slave
  // Jenkins will dynamically provision this as OpenShift Pod
  // All the stages and steps of this Pipeline will be executed on this Pod
  // After Pipeline completes the Pod is killed so every run will have clean
  // workspace
  agent {
    label 'maven'
  }

  // Pipeline Stages start here
  // Requeres at least one stage
  stages {

    // Checkout source code
    // This is required as Pipeline code is originally checkedout to
    // Jenkins Master but this will also pull this same code to this slave
  //  stage('Git Checkout') {
  //    steps {
  //      // Turn off Git's SSL cert check, uncomment if needed
  //      // sh 'git config --global http.sslVerify false'
  //      git url: "${APPLICATION_SOURCE_REPO}"
  //    }
  //  }

    // Run Maven build, skipping tests
    stage('Build'){
      steps {
        sh "mvn clean install -DskipTests=true -f ${POM_FILE}"
      }
    }

    // Run Maven unit tests
    stage('Unit Test'){
      steps {
        sh "mvn test -f ${POM_FILE}" 
      }
    }

    // Build Container Image using the artifacts produced in previous stages
    stage('Build Container Image'){
      steps {
        // Copy the resulting artifacts into common directory
        sh """
          ls target/*
          rm -rf oc-build && mkdir -p oc-build/deployments
          for t in \$(echo "jar;war;ear" | tr ";" "\\n"); do
            cp -rfv ./target/*.\$t oc-build/deployments/ 2> /dev/null || echo "No \$t files"
          done
        """

        // Build container image using local Openshift cluster
        // Giving all the artifacts to OpenShift Binary Build
        // This places your artifacts into right location inside your S2I image
        // if the S2I image supports it.
        script {
          openshift.withCluster() {
            openshift.withProject() {
              openshift.selector("bc", "${APP_NAME}").startBuild("--from-dir=oc-build").logs("-f")
            }
          }
        }
		echo "after oc-build"
      }
    }

    stage('Promote from Build to Dev') {
      steps {
        script {
          openshift.withCluster() {
            openshift.tag("${env.STAGE0}/${env.APP_NAME}:latest", "${env.STAGE1}/${env.APP_NAME}:latest")
          }
        }
		echo "after promote from Build to Dev"
      }
    }

    stage ('Verify Deployment to Dev') {
      steps {
        script {
          openshift.withCluster() {
              openshift.withProject("${STAGE1}") {
              def dcObj = openshift.selector('dc', env.APP_NAME).object()
              def podSelector = openshift.selector('pod', [deployment: "${APP_NAME}-${dcObj.status.latestVersion}"])
              podSelector.untilEach {
                  echo "pod: ${it.name()}"
                  return it.object().status.containerStatuses[0].ready
              }
            }
          }
        }
      }
    }


  }
}

