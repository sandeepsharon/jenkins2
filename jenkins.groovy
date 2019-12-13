##########################################################################
git-maven-139_deploy-email

node{
  stage('scm') {
    git credentialsId: 'qwqw', url: 'https://github.com/Polus-Software/fibi40.git'
  }
stage('compile') {
    def mvnHome=tool name: 'maven', type: 'maven'
    sh "${mvnHome}/bin/mvn clean"
    sh "${mvnHome}/bin/mvn compile"
    sh "${mvnHome}/bin/mvn war:war"

  }
  stage('Deploy') {
    sshagent(['139newserver']){
      sh 'scp -o StrictHostKeyChecking=no target/*.war root@192.168.1.139:/home/'
    }
  }
  stage('Email Notification') {
    mail bcc: '', body: 'test', cc: '', from: '', replyTo: '', subject: 'test', to: 'sandeep.s@polussoftware.com'
}
}
#######################################################################################################################

node{
    cleanWs()
  stage('scm') {
    git branch: 'Khalifa_Customizations', credentialsId: 'new_git', url: 'https://github.com/khalifa-university/fibi40-client.git'
  }
  stage('Build'){
    def nodeHome="/root/.nvm/versions/node/v11.5.0/bin"
    env.PATH="${nodeHome}:${env.PATH}"
    sh 'npm install'
    sh "${nodeHome}/ng build --prod"
}
  stage('Email Notification') {
    mail bcc: '', body: 'test', cc: '', from: '', replyTo: '', subject: 'test', to: 'sandeep.s@polussoftware.com'
  }
  stage('Deploy') {
      sh 'mkdir fibi40; cp -R dist/* fibi40'
      sshagent(['139_front']){
        sh 'ssh root@192.168.1.139 "rm -rf /home/sam"'
        sh 'scp -r -o StrictHostKeyChecking=no fibi40 root@192.168.1.139:/home/'
    }
}
}
#######################################################################################################
###PARAMETERISED BUILD

properties([parameters([choice(choices: ['master', 'Khalifa_Customizations'], description: 'select SCM', name: 'branch')])])
node{
  stage('scm checkout') {
    echo "Pulling changes from the branch ${params.branch}"
    git branch: "${params.branch}", credentialsId: 'new_git', url: 'https://github.com/khalifa-university/fibi40-client.git'
    }
}

################################################################################################################################
## Exception handling

node{
    cleanWs()
  stage('scm') {
    git branch: 'Khalifa_Customizations', credentialsId: 'new_git', url: 'https://github.com/khalifa-university/fibi40-client.git'
  }
  stage('Build'){
    def nodeHome="/root/.nvm/versions/node/v11.5.0/bin"
    env.PATH="${nodeHome}:${env.PATH}"
    sh 'npm install'
    sh "${nodeHome}/ng build --prod > trap.txt"
    sam = ""
    try{

    sh "grep 'Error in' trap.txt > trap1"


    sam = readFile('trap1').trim()
    println(readFile('trap1').trim())
    //echo "${sam}"
    }catch(Exception ex){
        println("Exception Handled")
        //echo "${sam}"
    }
    echo "${sam}"
    if (sam != ""){
        println("Build failed")
        echo "${sam}"
    }else{
        println("Build success")
        echo "${sam}"
    }



}
}
########################################################################################################################################
##COMPLETE SCRIPT FOR ERROR HANDLING AND EMAIL NOTIFICATION

node{
    cleanWs()
  stage('scm') {
    git branch: 'Khalifa_Customizations', credentialsId: 'new_git', url: 'https://github.com/khalifa-university/fibi40-client.git'
  }
  stage('Build'){
    def nodeHome="/root/.nvm/versions/node/v11.5.0/bin"
    env.PATH="${nodeHome}:${env.PATH}"
    sh 'npm install'
    sh "${nodeHome}/ng build --prod > trap.txt"
    handler = ""
    try{
      sh "grep 'ERROR in' trap > errortrap"
      handler = readFile('errortrap').trim()

    }catch(Exception ex){
        println("Bash Exception Handled")
    }
    echo "${handler}"
    if (handler != ""){
        println("Build failed")
        echo "${handler}"
        mail bcc: '', body: "Jenkins Angular BUILD FAILED with error: $handler", cc: '', from: '', replyTo: '', subject: 'BUILD FAILED', to: 'sandeep.s@polussoftware.com'
        sh "grep 'ERROR in' trap.txt > demo"

    }else{
        println("Build success")

    }

}
stage('Deploy') {
  sh "mkdir fibi"
  sh "cp -R dist/* fibi"
  sh 'ssh root@192.168.1.21 "rm -rf /opt/tomcat/webapps/fibi"'
  sshagent(['121']){
    sh 'scp -r -o StrictHostKeyChecking=no fibi root@192.168.1.21:/opt/tomcat/webapps/'
  }
}
}
##################################

node{
    cleanWs()
  stage('scm') {
    git branch: 'Khalifa_Customizations', credentialsId: 'new_git', url: 'https://github.com/khalifa-university/fibi40-client.git'
  }
  stage('Build'){
    handler = ""
    try{
    def nodeHome="/root/.nvm/versions/node/v11.5.0/bin"
    env.PATH="${nodeHome}:${env.PATH}"
    sh 'npm install'
    sh "${nodeHome}/ng build --prod 2> trap"
    }catch(Exception ex){
        println("Bash Exception 1 handled ")
    }
    try{
      sh "grep 'ERROR in' trap > errortrap"
      handler = readFile('errortrap').trim()

    }catch(Exception ex){
        println("Bash Exception Handled")
    }
    echo "${handler}"
    if (handler != ""){
        println("Build failed")
        echo "${handler}"
        mail bcc: '', body: "Jenkins Angular BUILD FAILED with error: $handler", cc: '', from: '', replyTo: '', subject: 'BUILD FAILED', to: 'mrudul@polussoftware.com'
        sh "grep 'eminem' trap > demo"

    }else{
        println("Build success")
        mail bcc: '', body: "Jenkins Angular BUILD SUCCESS | You can check the application at http://192.168.1.21:8080/fibi", cc: '', from: '', replyTo: '', subject: 'BUILD SUCCESS', to: 'mrudul@polussoftware.com'


    }

}
  stage('Deploy') {
    sh "mkdir fibi"
    sh "cp -R dist/* fibi"
    sh 'ssh root@192.168.1.21 "rm -rf /opt/tomcat/webapps/fibi"'
    sshagent(['121']){
      sh 'scp -r -o StrictHostKeyChecking=no fibi root@192.168.1.21:/opt/tomcat/webapps/'
    }
  }
}
