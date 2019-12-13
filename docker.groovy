node{
    cleanWs()
    stage('scm') {
        git branch: 'Fibi-Development', credentialsId: 'Renjith', url: 'https://github.com/Polus-Software/fibi40-client.git'
    }
    stage('Build'){
        handler = ""
        try{
            def nodeHome="/root/.nvm/versions/node/v11.5.0/bin"
            env.PATH="${nodeHome}:${env.PATH}"
            sh 'npm install moment --save'
            sh "${nodeHome}/ng build --prod 2> trap"
        }catch(Exception ex){
            println("Bash Exception 1 handled ")

        }
        try{
            sh "grep 'ERROR in' trap > errortrap"
            handler = readFile('errortrap').trim()

        }catch(Exception ex){
            println("Bash Exception 2 Handled")
        }
        echo "${handler}"
        if (handler != ""){
            println("Build failed")
            echo "${handler}"
            mail bcc: '', body: "Jenkins Angular BUILD FAILED with error: $handler", cc: 'renjith@polussoftware.com', from: '', replyTo: '', subject: 'BUILD FAILED', to: 'aravind.ps@polussoftware.com'

            //sh "grep 'eminem' trap > demo"
            throw new Exception("Angular Exception")
        }else{
            println("Build success")
            mail bcc: '', body: "Jenkins Angular BUILD SUCCESS | You can check the application at http://192.168.1.139:8080/fibi_load", cc: 'renjith@polussoftware.com', from: '', replyTo: '', subject: 'BUILD SUCCESS', to: 'aravind.ps@polussoftware.com'
        }

    }
    stage('Deploy') {
        sh "mkdir fibi_load"
        sh "cp -R dist/* fibi_load"
        //sh 'ssh root@192.168.1.139 "rm -rf /opt/tomcat/webapps/fibi_load"'
        sshagent(['121']){
            sh 'scp -r -o StrictHostKeyChecking=no fibi_load root@192.168.1.139:/opt/tomcat/webapps/'
        }
    }
}
