// PoliDinner - pipeline de integración continua (Jenkins en Docker)
// Requiere que el agente de Jenkins tenga Java 17. Maven no hace falta: se usa el wrapper ./mvnw del repo.
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Compilar') {
            steps {
                sh 'chmod +x mvnw && ./mvnw -B -q clean compile'
            }
        }
        stage('Pruebas unitarias') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Empaquetar') {
            steps {
                sh './mvnw -B -q package -DskipTests'
                archiveArtifacts artifacts: 'target/polidinner.war', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Integración exitosa: compila, pasan las pruebas y se generó polidinner.war'
        }
        failure {
            echo 'Integración fallida: revisar la etapa en rojo'
        }
    }
}
