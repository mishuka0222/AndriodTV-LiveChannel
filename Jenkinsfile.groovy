import jenkins.model.*

def getAppVersionCode() {
    def appVersionCode = jenkins.model.Jenkins.instance.getItem("android-tvheadend-version-code").nextBuildNumber
    build job: 'android-tvheadend-version-code', wait: false
    return appVersionCode
}

def assemble() {
    sh './gradlew assemble'
}

def archive() {
    archiveArtifacts artifacts: 'app/build/outputs/apk/*.apk', fingerprint: true
    stash includes: 'app/build/outputs/apk/*.apk', name: 'built-apk'
}

def lint() {
    sh './gradlew lint'
    androidLint canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', healthy: '', pattern: '**/lint-results*.xml', unHealthy: ''
}

def publishApkToStore(String trackName) {
    def changeLog = sh(returnStdout: true, script: "./tools/generate-changelog").trim().take(500)

    androidApkUpload(
        apkFilesPattern: 'app/build/outputs/apk/ie.macinnes.tvheadend_*-release.apk',
        googleCredentialsId: 'android-tvheadend',
        trackName: trackName,
        recentChangeList: [
            [language: 'en-GB', text: changeLog],
        ],
    )
}

def publishApkToGitHub() {
    def tagName = sh(returnStdout: true, script: "git describe --tags --abbrev=0 --exact-match").trim()
    def changeLog = sh(returnStdout: true, script: "./tools/generate-changelog").trim().replaceAll(~/'/, "\'")

    withCredentials([
        [$class: 'StringBinding', credentialsId: '  github-pat-kiall', variable: 'GITHUB_TOKEN'],
    ]) {
        sh(script: "github-release release --user kiall --repo android-tvheadend --tag ${tagName} --name ${tagName} --description '${changeLog}'")
        sh(script: "github-release upload --user kiall --repo android-tvheadend --tag ${tagName} --name ie.macinnes.tvheadend_${tagName}-release.apk --file app/build/outputs/apk/ie.macinnes.tvheadend_${tagName}-release.apk")
    }
}

def withGithubNotifier(Closure<Void> job) {
   notifyGithub('STARTED')
   catchError {
      currentBuild.result = 'SUCCESS'
      job()
   }
   notifyGithub(currentBuild.result)
}
 
def notifyGithub(String result) {
   switch (result) {
      case 'STARTED':
         setGitHubPullRequestStatus(context: env.JOB_NAME, message: "Build started", state: 'PENDING')
         break
      case 'FAILURE':
         setGitHubPullRequestStatus(context: env.JOB_NAME, message: "Build error", state: 'FAILURE')
         break
      case 'UNSTABLE':
         setGitHubPullRequestStatus(context: env.JOB_NAME, message: "Build unstable", state: 'FAILURE')
         break
      case 'SUCCESS':
         setGitHubPullRequestStatus(context: env.JOB_NAME, message: "Build finished successfully", state: 'SUCCESS')
         break
   }
}

return this;
