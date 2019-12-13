
def label = "worker-${UUID.randomUUID().toString()}"
podTemplate(label: label, containers: [
        containerTemplate(name: 'node', image: 'node:12', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'angular', image: 'angular/ngcontainer', command: 'cat', ttyEnabled: true),

],

        volumes: [
                hostPathVolume(mountPath: '/home/.node11', hostPath: '/tmp/.node11'),
                hostPathVolume(mountPath: '/home/.angular', hostPath: '/tmp/.angular')
        ]) {
    node(label) {
        stage('scm'){
            container('node'){

                git branch: 'Demo_Instance', credentialsId: 'Renjith', url: 'https://github.com/Polus-Software/fibi40-client.git'
            }

        }

        stage('Test') {

            container('node') {
                sh """
pwd
ls
node -v
npm -v
npm install
ls

"""
            }
        }
    }
















}
