node {
  git url: 'ssh://git@github.com:/mgwerder/messageVortex_internal', branch: 'master', credentialsID: 'github_readonly'
  def mvnHome = tool 'M3'
  sh "buildenv/build.sh"
}