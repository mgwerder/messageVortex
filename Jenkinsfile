node {
  git credentialsId: 'github_readonly', url: 'ssh://git@github.com/mgwerder/messageVortex_internal'
  def mvnHome = tool 'M3'
  sh "buildenv/build.sh"
  mail from: 'messagevortex@gwerder.net', subject: '[MessageVortex]', to: 'martin@gwerder.net', body: '''MessageVortex build process done.

see https://www.gwerder.net/jenkins'''
}