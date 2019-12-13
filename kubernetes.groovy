node{
    cleanWs()
    stage('scm') {
        git branch: 'Fibi-Development', credentialsId: 'Renjith', url: 'https://github.com/Polus-Software/fibi40-client.git'
    }

    stage('Build'){
        handler = ""
        try{
            //def nodeHome="/root/.nvm/versions/node/v11.5.0/bin"
            //env.PATH="${nodeHome}:${env.PATH}"
            sh 'npm install moment --save'
            sh "ng build 2> trap"
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
            //mail bcc: '', body: "Jenkins Angular BUILD FAILED with error: $handler", cc: 'renjith@polussoftware.com', from: '', replyTo: '', subject: 'BUILD FAILED', to: 'aravind.ps@polussoftware.com'

            //sh "grep 'eminem' trap > demo"
            throw new Exception("Angular Exception")
        }else{
            println("Build success")
            // mail bcc: '', body: "Jenkins Angular BUILD SUCCESS | You can check the application at http://192.168.1.139:8080/fibi_load", cc: 'renjith@polussoftware.com', from: '', replyTo: '', subject: 'BUILD SUCCESS', to: 'aravind.ps@polussoftware.com'
        }

    }

    stage('Docker') {
        sh "mkdir fibi_load"
        sh "cp -R dist/* fibi_load"
        sh '''
    cat <<EOT >> Dockerfile
    FROM tomcat:9-jdk11
    COPY fibi_load /usr/local/tomcat/webapps/fibi_load
    '''

        sh "docker build -t 192.168.1.100:5000/fibi_load:${BUILD_NUMBER} --network=host ."
        sh "docker push 192.168.1.100:5000/fibi_load:${BUILD_NUMBER}"
    }
    stage('Kubernetes'){
        sh '''
   cat <<EOT>> k8.yml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: test-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: test-deployment-demo
  minReadySeconds: 10
  template:
    metadata:
      labels:
        app: test-deployment-demo
        type: new
        version: v0.1
    spec:
      containers:
        - name: test-deployment-demo
          image: 192.168.1.100:5000/fibi_load:${BUILD_NUMBER}
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
              protocol: TCP
          env:
            - name: DEMO_ENV
              value: staging
   '''

        sh 'ssh root@192.168.1.57 "kubectl get pods"'

        sshagent(['kube-57']){
            sh 'scp -r -o StrictHostKeyChecking=no k8.yml root@192.168.1.57:/root/kubme/'
        }
        sh 'ssh root@192.168.1.57 "cd /root/kubme/; kubectl apply -f k8.yml --record"'
    }
}


-------------------------------------------------------------------------------------------------------

        node{
            cleanWs()
            stage('scm') {
                git branch: 'Fibi-Development', credentialsId: 'Renjith', url: 'https://github.com/Polus-Software/fibi40.git'
            }
            stage('compile') {
                def mvnHome=tool name: 'maven', type: 'maven'
                sh "${mvnHome}/bin/mvn clean"
                sh "${mvnHome}/bin/mvn compile"
                sh "${mvnHome}/bin/mvn war:war"
            }
            stage('Deploy') {
                //sshagent(['139newserver']){
                // sh 'scp -o StrictHostKeyChecking=no target/*.war root@192.168.1.139:/home/'
                sh '''
    cat <<EOT >> Dockerfile
    FROM tomcat:8
    COPY target/fibi4.war /usr/local/tomcat/webapps/fibi4.war
     '''
                sh "docker build -t 192.168.1.100:5000/fibi4.war:${BUILD_NUMBER} --network=host ."
                sh "docker push 192.168.1.100:5000/fibi4.war:${BUILD_NUMBER}"
            }
            stage('Kubernetes'){
                sh '''
   cat <<EOT>> k9.yml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: back-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: back-deployment-demo
  minReadySeconds: 10
  template:
    metadata:
      labels:
        app: back-deployment-demo
        type: new1
        version: v0.1
    spec:
      containers:
        - name: back-deployment-demo
          image: 192.168.1.100:5000/fibi4.war:${BUILD_NUMBER}
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
              protocol: TCP
          env:
            - name: DEMO_ENV
              value: staging
   '''

                sh 'ssh root@192.168.1.57 "kubectl get pods"'

                sshagent(['kube-57']){
                    sh 'scp -r -o StrictHostKeyChecking=no k9.yml root@192.168.1.57:/root/kubme/'
                }
                sh 'ssh root@192.168.1.57 "cd /root/kubme/; kubectl apply -f k9.yml --record"'
            }

        }





