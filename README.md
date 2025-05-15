# DevOps Screencast Projekt: Chat-Anwendung mit Ollama

Dieses Repository enthält den Quellcode für eine Chat-Anwendung, die im Rahmen eines DevOps-Kurses als Screencast-Projekt entwickelt wurde. Die Anwendung nutzt ein lokal gehostetes Sprachmodell (LLM) über Ollama, um auf Benutzereingaben zu antworten.

## Überblick

Das Projekt demonstriert verschiedene DevOps-Praktiken, darunter:

* Verwendung von Build-Tools (Gradle für das Backend, NPM für das Frontend)
* Containerisierung der Anwendungsdienste mit Docker
* Orchestrierung der Container mit Docker Compose für die lokale Entwicklung und Ausführung

Die Anwendung besteht aus einem Java-basierten Backend, einem React-basierten Frontend und einem Ollama-Dienst, der das Sprachmodell bereitstellt.

## Technologie-Stack

* **Backend:**
    * Java 21
    * Spring Boot 3.x (für die REST-API)
    * Gradle (Build-Tool)
    * WebClient (für die Kommunikation mit Ollama)
* **Frontend:**
    * JavaScript
    * React 18.x (UI-Bibliothek)
    * Vite (Build-Tool und Dev-Server)
    * NPM (Paketmanagement)
    * Nginx (als Webserver für die statischen Frontend-Dateien im Docker-Container)
* **LLM-Bereitstellung:**
    * Ollama
    * Modell: `phi3` (oder ein anderes kompatibles Modell)
* **Containerisierung & Orchestrierung:**
    * Docker
    * Docker Compose

## Backend (`Screencast-DevOps/backend/chat-backend/`)

Das Backend ist eine Spring Boot-Anwendung, die eine REST-API unter `/api/chat` bereitstellt.

* **Funktionsweise:**
    1.  Nimmt POST-Requests mit einer Benutzereingabe (Prompt) entgegen.
    2.  Leitet den Prompt an den Ollama-Dienst weiter (standardmäßig `http://localhost:11434/api/generate` bei lokaler Ausführung ohne Docker, bzw. `http://ollama:11434/api/generate` bei Ausführung im Docker-Netzwerk).
    3.  Empfängt die Antwort vom Sprachmodell.
    4.  Gibt die Antwort des Modells als JSON zurück an das Frontend.
* **Build:**
    Das Backend wird mit Gradle gebaut. Der Build-Prozess ist im `Dockerfile` im Verzeichnis `backend/chat-backend/` definiert, welcher ein ausführbares JAR-Archiv erstellt. Für die lokale Ausführung ohne Docker wird der Gradle Wrapper (`./gradlew`) verwendet.

## Frontend (`Screencast-DevOps/frontend/`)

Das Frontend ist eine Single-Page-Application (SPA), die mit React und Vite entwickelt wurde.

* **Funktionsweise:**
    1.  Bietet eine Chat-Oberfläche im Browser.
    2.  Sendet Benutzereingaben per `Workspace`-API an den Backend-Endpunkt:
        * `http://localhost:8080/api/chat` bei lokaler Ausführung ohne Docker.
        * `/api/chat` bei Ausführung mit Docker (Nginx-Proxy leitet weiter).
    3.  Zeigt die Antworten des Sprachmodells im Chatfenster an.
* **Build:**
    Das Frontend wird mit Vite und NPM gebaut (`npm run build`). Dieser Prozess generiert statische HTML-, JavaScript- und CSS-Dateien. Im Docker-Kontext werden diese statischen Dateien von einem Nginx-Server ausgeliefert. Der Build-Prozess ist im `Dockerfile` im Verzeichnis `frontend/` definiert. Die `nginx.conf` in diesem Verzeichnis konfiguriert Nginx entsprechend. Für die lokale Ausführung ohne Docker wird der Vite Dev-Server (`npm run dev`) genutzt.

## Lokales Ausführen OHNE Docker

Für die Entwicklung und zum Testen der einzelnen Komponenten können Frontend und Backend auch direkt auf dem Host-System gestartet werden.

1.  **Ollama-Dienst starten:**
    * Installiere Ollama gemäß der [offiziellen Anleitung](https://ollama.com/).
    * Lade das gewünschte Sprachmodell herunter (z.B. `phi3`):
        ```bash
        ollama pull phi3
        ```
    * Starte den Ollama-Server (falls er nicht bereits als Dienst läuft). In einem separaten Terminal:
        ```bash
        ollama serve
        ```
        Ollama ist dann standardmäßig unter `http://localhost:11434` erreichbar.

2.  **Backend starten:**
    * Öffne ein Terminal im Verzeichnis `Screencast-DevOps/backend/chat-backend/`.
    * Stelle sicher, dass die `ollama.api.url` in `src/main/resources/application.properties` auf `http://localhost:11434` gesetzt ist.
    * Starte die Spring Boot-Anwendung mit dem Gradle Wrapper:
        ```bash
        ./gradlew bootRun
        ```
        Das Backend läuft dann standardmäßig unter `http://localhost:8080`.

3.  **Frontend starten:**
    * Öffne ein weiteres Terminal im Verzeichnis `Screencast-DevOps/frontend/`.
    * Stelle sicher, dass die `Workspace`-URL in `src/App.jsx` auf `http://localhost:8080/api/chat` zeigt.
    * Installiere die Frontend-Abhängigkeiten (falls noch nicht geschehen):
        ```bash
        npm install
        ```
    * Starte den Vite Development-Server:
        ```bash
        npm run dev
        ```
        Das Frontend ist dann üblicherweise unter `http://localhost:5173` (Vite gibt die genaue URL aus) im Browser erreichbar.

## Ausführen mit Docker Compose

Um die gesamte Anwendung (Backend, Frontend und Ollama-Dienst) lokal als Container-Verbund auszuführen:

1.  **Voraussetzungen:**
    * Docker Engine und Docker Compose müssen installiert sein.
    * Stelle sicher, dass keine anderen Dienste die Ports `3000`, `8080` oder `11434` auf dem Host belegen (oder passe die Port-Mappings in `docker-compose.yml` an).

2.  **Klonen des Repositories (falls noch nicht geschehen):**
    ```bash
    git clone <repository-url>
    cd Screencast-DevOps
    ```

3.  **Eventuell laufende lokale Dienste stoppen:**
    Stoppe manuell gestartete Ollama-, Backend- oder Frontend-Prozesse auf dem Host, um Portkonflikte zu vermeiden.

4.  **Anwendung starten:**
    Führe im Hauptverzeichnis des Projekts (wo sich die `docker-compose.yml` befindet) folgenden Befehl aus:
    ```bash
    docker-compose up --build
    ```
    Beim ersten Start kann der Build-Prozess einige Zeit in Anspruch nehmen.

5.  **Sprachmodell in Ollama-Container laden (nur beim ersten Start mit leerem Volume):**
    * Während `docker-compose up` in einem Terminal läuft, öffne ein **zweites Terminal**.
    * Navigiere ebenfalls ins Hauptverzeichnis des Projekts.
    * Führe folgenden Befehl aus, um das `phi3`-Modell in den laufenden Ollama-Container herunterzuladen:
        ```bash
        docker-compose exec ollama ollama pull phi3
        ```
        Warte, bis der Download abgeschlossen ist.

6.  **Anwendung im Browser öffnen:**
    Öffne deinen Webbrowser und navigiere zu:
    `http://localhost:3000`

7.  **Anwendung stoppen:**
    * Um die Anwendung zu stoppen, gehe zurück zum Terminal, in dem `docker-compose up` ausgeführt wird, und drücke `Ctrl+C`.
    * Um die Container und Netzwerke vollständig zu entfernen:
        ```bash
        docker-compose down
        ```
        Um auch das Ollama-Datenvolume zu entfernen (Vorsicht: heruntergeladene Modelle gehen verloren):
        ```bash
        docker-compose down --volumes
        ```

---
Viel Erfolg mit deinem DevOps-Screencast!