In this project we provide fast java factoring algorithms for integers up to 63 bits.
# Project Goals & Guidelines

The main goals for this project are:

- **Clean Code**: High code quality and maintainability.
- **High Performance**: Fast and readable implementations of Hart and Trial Division factoring algorithms.
- **Modern Algorithms**: A Java implementation of "Lemire's Algorithm."
- **Efficiency**: An array-based, fast version of the Hart "one-line" factoring algorithm.
- **Vectorization**: Leveraging Java's vectorization capabilities without losing readability.
- **Benchmarking**: Prepared performance comparisons across different implementations and prototypes.
- **Usability**: An easy-to-use frontend and a REST service for number factorization.

# Algorithms

## Hart Factoring algorithm

Some work went into it, to improve the original approach 
  - enable JIT to optimize the code for vectorization
  - optimize the multipliers 
  - optimize values on different modulus (64 and 81)

The algorithm runs in O(n^1/3). In combination with a optimized trial division algorithm
it should be one of the fastest algorithms for long numbers.

## Lemire Trial Division algorithm 

Since the main loop is simple java can also make use of vectorization.
The fastest algorithm uses no divisions, which are expensive.
It uses just long values (casting to double and back to long), and such has a good 
performance on cpu's which support SSE, AVX.
Lemire's approach is from 2019 and relatively new, and has good support from 
java and cpu's with vectorization support.
On SSE-2 We se 250% speedup compared to using a multiplications with the Reciprocals
instead of using a division together with a rounding trick which avoids expensive type conversions.
We see 1200% improvement over division and rounding.
# Author 

Thilo Harich a java software developer. 


# Credits 
Tilman Neumann - for the great joint work on the Hart algorithm.

# Getting Started (Backend and Frontend)

The project is a Spring Boot application where the frontend (Thymeleaf) is integrated into the backend. Once you start the server, the frontend is automatically accessible.

## 1. Running via IntelliJ IDEA (Recommended)
1. Open the file `src/main/java/de/harich/thilo/factoring/FactoringApplication.java`.
2. Click the **green play button** next to the `public class FactoringApplication` line or the `main` method.
3. Select **"Run 'FactoringApplication'"** or **"Debug 'FactoringApplication'"** (if you want to use breakpoints).

## 2. Running via Terminal (Gradle)
Open a terminal in the project folder and enter:
```powershell
./gradlew bootRun
```

## 3. Accessing the Frontend
Once the application has started (you will see `Started FactoringApplication in ... seconds` in the log), open the following URL in your browser:

### **[http://localhost:8080/](http://localhost:8080/)**

*Note: Do **not** use the Chrome button in the top right of the IntelliJ HTML view (port 63342), as it only shows a static preview without functionality.*

## Prerequisites
* **Java Version**: According to your `build.gradle`, the project requires **Java 24**. Ensure that the correct SDK (Java 24) is set in IntelliJ under *File > Project Structure > Project*.
* **Port**: If port 8080 is already in use, the application will fail to start. In this case, the port would need to be changed in `application.properties`.

# Docker (for Hosting)

To host the application on platforms like Render.com or Railway.app that support Docker, a `Dockerfile` has been added.

**Important:** You do **not** need to have Docker installed on your own computer to host the application with a cloud provider. The provider (e.g., Render) uses the `Dockerfile` automatically.

## Building and Running Locally with Docker (Optional)
*Note: This requires [Docker Desktop](https://www.docker.com/products/docker-desktop/). If Docker is not installed, you will receive an error message: "The term 'docker' was not recognized as the name of a cmdlet".*

1. **Build the image:**
   ```powershell
   docker build -t hart-factoring .
   ```
2. **Start the container:**
   ```powershell
   docker run -p 8080:8080 hart-factoring
   ```
The application will then be accessible at `http://localhost:8080/`.

## Hosting Note
With cloud providers (like Render), simply select **Docker** as the runtime when creating the web service. The system will automatically detect the `Dockerfile` in the root directory, build the image, and start the server.