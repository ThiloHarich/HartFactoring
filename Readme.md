In this project we provide fast java factoring algorithms for integers up to 63 bits.
The main goal for this are 

- Clean Code
- a fast implementation of Hart and trial division Factoring algorithms
- how to use vectorisation in factoring algorithms in Java without loosing readability
- get hands on some aspects of the algorithm by prepared tests, which can be extended
- provide a easy-to-use command line interface (on the way)

# Algorithms

## Hart Factoring algorithm

Some work went into it, to improve the original approach 
  - enable JIT to optimize the code for vectorization
  - optimize the multipliers 
  - optimize values on different modulus (64 and 81)

The algorithm runs in O(n^1/3). In combination with a optimized trial division algorithm
it should be one of the fastest algorithms for long numbers.

## Lemire Trial Division algorithm 

Since the main loop is simple java can also make use of vectorisation.
The fastest algorithm uses no divisions, which are expensive.
It uses just long values (casting to double and back to long), and such has a good 
performance on cpu's which support SSE, AVX.
Lemire's approach is from 2019 and relatively new, and has good support from 
java and cpu's with vectorisation support.
On SSE-2 We se 250% speedup compared to using a multiplications with the Reciprocals
instead of using a division together with a rounding trick which avoids expensive type conversions.
We see 1200% improvement over division and rounding.
# Author 

Thilo Harich a java software developer. 


# Credits 
Tilman Neumann - for the great joint work on the Hart algorithm.

# Starten des Projekts (Backend und Frontend)

Das Projekt ist eine Spring Boot Anwendung, bei der das Frontend (Thymeleaf) fest in das Backend integriert ist. Sobald Sie den Server starten, ist auch das Frontend automatisch erreichbar.

## 1. Starten über IntelliJ IDEA (Empfohlen)
1.  Öffnen Sie die Datei `src/main/java/de/harich/thilo/factoring/FactoringApplication.java`.
2.  Klicken Sie auf den **grünen Play-Button** links neben der Zeile `public class FactoringApplication` oder bei der `main`-Methode.
3.  Wählen Sie **"Run 'FactoringApplication'"** oder **"Debug 'FactoringApplication'"** (wenn Sie Breakpoints nutzen möchten).

## 2. Starten über die Konsole (Gradle)
Öffnen Sie ein Terminal im Projektordner und geben Sie ein:
```powershell
./gradlew bootRun
```

## 3. Zugriff auf das Frontend
Sobald die Anwendung gestartet ist (Sie sehen im Log `Started FactoringApplication in ... seconds`), rufen Sie im Browser folgende URL auf:

### **[http://localhost:8080/](http://localhost:8080/)**

*Hinweis: Verwenden Sie **nicht** den Chrome-Button oben rechts in der IntelliJ-HTML-Ansicht (Port 63342), da dieser nur eine statische Vorschau ohne Funktion zeigt.*

## Wichtige Voraussetzungen
*   **Java Version**: Gemäß Ihrer `build.gradle` benötigt das Projekt **Java 24**. Stellen Sie sicher, dass in IntelliJ unter *File > Project Structure > Project* das richtige SDK (Java 24) eingestellt ist.
*   **Port**: Falls Port 8080 bereits belegt ist, würde die Anwendung mit einem Fehler abbrechen. In diesem Fall müsste der Port in der `application.properties` geändert werden.

# Docker (für das Hosting)

Um die Anwendung auf Plattformen wie Render.com oder Railway.app zu hosten, die Docker unterstützen, wurde ein `Dockerfile` hinzugefügt.

**Wichtig:** Sie müssen Docker **nicht** auf Ihrem eigenen Computer installiert haben, um die Anwendung bei einem Cloud-Anbieter zu hosten. Der Anbieter (z.B. Render) nutzt das `Dockerfile` automatisch.

## Lokal mit Docker bauen und starten (Optional)
*Hinweis: Dies erfordert die Installation von [Docker Desktop](https://www.docker.com/products/docker-desktop/). Wenn Docker nicht installiert ist, erhalten Sie die Fehlermeldung: "Die Benennung 'docker' wurde nicht als Name eines Cmdlet erkannt".*

1. **Image bauen:**
   ```powershell
   docker build -t hart-factoring .
   ```
2. **Container starten:**
   ```powershell
   docker run -p 8080:8080 hart-factoring
   ```
Die Anwendung ist dann wieder unter `http://localhost:8080/` erreichbar.

## Hosting-Hinweis
Bei Cloud-Anbietern (wie Render) wählen Sie beim Erstellen des Web-Services einfach **Docker** als Runtime aus. Das System erkennt das `Dockerfile` im Root-Verzeichnis automatisch, baut das Image und startet den Server.