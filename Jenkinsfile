node {
  git branch: 'master', credentialsID: 'github_readonly', url: 'ssh://git@github.com:/mgwerder/messageVortex_internal'
  def mvnHome = tool 'M3'
  sh "buildenv/build.sh"
}