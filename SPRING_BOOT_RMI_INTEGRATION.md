# Integração Spring Boot + Thymeleaf com Serviço RMI

Este guia demonstra a forma correta de conectar um servidor web Spring Boot com Thymeleaf a um serviço de busca que utiliza RMI (Remote Method Invocation).

---

## Índice

1. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
2. [Configuração do Serviço RMI](#1-configuração-do-serviço-rmi-search-service)
3. [Spring Boot Application](#2-spring-boot-application---cliente-rmi)
4. [Templates Thymeleaf](#3-templates-thymeleaf)
5. [Configuração](#4-configuração)
6. [Melhores Práticas](#melhores-práticas)

---

## Visão Geral da Arquitetura

```
┌─────────────────┐
│   Browser       │
│  (HTML/CSS/JS)  │
└────────┬────────┘
         │ HTTP
         ▼
┌─────────────────┐
│  Spring Boot    │
│  Web Server     │
│                 │
│  ┌───────────┐  │
│  │Controllers│  │
│  └─────┬─────┘  │
│        │        │
│  ┌─────▼─────┐  │
│  │  Service  │  │
│  │   Layer   │  │
│  └─────┬─────┘  │
│        │        │
│  ┌─────▼─────┐  │
│  │RMI Client │  │
│  └─────┬─────┘  │
└────────┼────────┘
         │ RMI
         ▼
┌─────────────────┐
│  RMI Server     │
│  (Gateway/      │
│   Search Svc)   │
└─────────────────┘
```

---

## 1. Configuração do Serviço RMI (Search Service)

### 1.1 Interface Remota

```java
package com.example.searchservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface remota para o serviço de busca
 */
public interface SearchService extends Remote {
    
    /**
     * Realiza uma busca por palavras-chave
     * @param query Termo de pesquisa
     * @return Lista de resultados
     * @throws RemoteException
     */
    List<SearchResult> search(String query) throws RemoteException;
    
    /**
     * Obtém informações sobre uma página específica
     * @param url URL da página
     * @return Informações da página
     * @throws RemoteException
     */
    PageInfo getPageInfo(String url) throws RemoteException;
    
    /**
     * Obtém links relacionados a uma URL
     * @param url URL base
     * @return Lista de URLs relacionadas
     * @throws RemoteException
     */
    List<String> getRelatedLinks(String url) throws RemoteException;
    
    /**
     * Obtém estatísticas do sistema
     * @return Objeto com estatísticas
     * @throws RemoteException
     */
    SystemStats getStatistics() throws RemoteException;
}
```

### 1.2 Implementação do Serviço

```java
package com.example.searchservice;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementação do serviço de busca RMI
 */
public class SearchServiceImpl extends UnicastRemoteObject 
                              implements SearchService {
    
    private final SearchEngine searchEngine;
    
    public SearchServiceImpl(SearchEngine searchEngine) throws RemoteException {
        super();
        this.searchEngine = searchEngine;
    }
    
    @Override
    public List<SearchResult> search(String query) throws RemoteException {
        try {
            return searchEngine.performSearch(query);
        } catch (Exception e) {
            throw new RemoteException("Error performing search", e);
        }
    }
    
    @Override
    public PageInfo getPageInfo(String url) throws RemoteException {
        try {
            return searchEngine.getPageInfo(url);
        } catch (Exception e) {
            throw new RemoteException("Error getting page info", e);
        }
    }
    
    @Override
    public List<String> getRelatedLinks(String url) throws RemoteException {
        try {
            return searchEngine.getRelatedLinks(url);
        } catch (Exception e) {
            throw new RemoteException("Error getting related links", e);
        }
    }
    
    @Override
    public SystemStats getStatistics() throws RemoteException {
        try {
            return searchEngine.getStatistics();
        } catch (Exception e) {
            throw new RemoteException("Error getting statistics", e);
        }
    }
}
```

### 1.3 Servidor RMI

```java
package com.example.searchservice;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Servidor RMI principal
 */
public class RMIServer {
    
    public static void main(String[] args) {
        try {
            // Criar registro RMI na porta 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Criar instância do serviço
            SearchEngine engine = new SearchEngine();
            SearchService service = new SearchServiceImpl(engine);
            
            // Registrar o serviço no registro
            registry.rebind("SearchService", service);
            
            System.out.println("RMI Server ready on port 1099");
            System.out.println("Service bound: SearchService");
            
        } catch (Exception e) {
            System.err.println("RMI Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
```

---

## 2. Spring Boot Application - Cliente RMI

### 2.1 Estrutura do Projeto

```
spring-boot-app/
├── src/main/java/
│   └── com/example/webapp/
│       ├── WebApplication.java
│       ├── config/
│       │   └── RMIConfig.java
│       ├── controller/
│       │   ├── HomeController.java
│       │   ├── SearchController.java
│       │   └── StatsController.java
│       ├── service/
│       │   └── SearchServiceClient.java
│       ├── model/
│       │   ├── SearchResult.java
│       │   ├── PageInfo.java
│       │   └── SystemStats.java
│       └── exception/
│           └── GlobalExceptionHandler.java
│
├── src/main/resources/
│   ├── application.properties
│   ├── templates/
│   │   ├── index.html
│   │   ├── search.html
│   │   ├── results.html
│   │   ├── page-info.html
│   │   ├── stats.html
│   │   └── error.html
│   └── static/
│       ├── css/
│       │   └── style.css
│       └── js/
│           └── main.js
│
└── pom.xml
```

### 2.2 Configuração RMI (RMIConfig.java)

```java
package com.example.webapp.config;

import com.example.searchservice.SearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Configuração do cliente RMI
 */
@Configuration
public class RMIConfig {
    
    @Value("${rmi.host:localhost}")
    private String rmiHost;
    
    @Value("${rmi.port:1099}")
    private int rmiPort;
    
    @Value("${rmi.service.name:SearchService}")
    private String serviceName;
    
    /**
     * Cria um bean do SearchService conectando ao servidor RMI
     */
    @Bean
    public SearchService searchService() throws Exception {
        try {
            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            SearchService service = (SearchService) registry.lookup(serviceName);
            
            // Testar conexão
            service.getStatistics();
            
            System.out.println("Connected to RMI service: " + serviceName);
            return service;
            
        } catch (Exception e) {
            System.err.println("Failed to connect to RMI service: " + e.getMessage());
            throw new RuntimeException("Unable to connect to search service", e);
        }
    }
}
```

### 2.3 Cliente do Serviço (SearchServiceClient.java)

```java
package com.example.webapp.service;

import com.example.searchservice.SearchService;
import com.example.webapp.model.PageInfo;
import com.example.webapp.model.SearchResult;
import com.example.webapp.model.SystemStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper para o serviço RMI com tratamento de erros
 */
@Service
public class SearchServiceClient {
    
    private final SearchService searchService;
    
    @Autowired
    public SearchServiceClient(SearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * Realiza uma pesquisa
     */
    public List<SearchResult> performSearch(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return searchService.search(query.trim());
            
        } catch (RemoteException e) {
            throw new RuntimeException("Error connecting to search service", e);
        }
    }
    
    /**
     * Obtém informações de uma página
     */
    public PageInfo getPageInfo(String url) {
        try {
            return searchService.getPageInfo(url);
        } catch (RemoteException e) {
            throw new RuntimeException("Error getting page info", e);
        }
    }
    
    /**
     * Obtém links relacionados
     */
    public List<String> getRelatedLinks(String url) {
        try {
            return searchService.getRelatedLinks(url);
        } catch (RemoteException e) {
            throw new RuntimeException("Error getting related links", e);
        }
    }
    
    /**
     * Obtém estatísticas do sistema
     */
    public SystemStats getStatistics() {
        try {
            return searchService.getStatistics();
        } catch (RemoteException e) {
            throw new RuntimeException("Error getting statistics", e);
        }
    }
}
```

### 2.4 Controllers

#### HomeController.java
```java
package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Googol Search");
        return "index";
    }
}
```

#### SearchController.java
```java
package com.example.webapp.controller;

import com.example.webapp.model.SearchResult;
import com.example.webapp.service.SearchServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {
    
    private final SearchServiceClient searchServiceClient;
    
    @Autowired
    public SearchController(SearchServiceClient searchServiceClient) {
        this.searchServiceClient = searchServiceClient;
    }
    
    /**
     * Página de pesquisa
     */
    @GetMapping
    public String searchPage(Model model) {
        model.addAttribute("pageTitle", "Search");
        return "search";
    }
    
    /**
     * Executar pesquisa e mostrar resultados
     */
    @GetMapping("/results")
    public String searchResults(
            @RequestParam("q") String query,
            Model model) {
        
        try {
            List<SearchResult> results = searchServiceClient.performSearch(query);
            
            model.addAttribute("results", results);
            model.addAttribute("query", query);
            model.addAttribute("resultCount", results.size());
            model.addAttribute("pageTitle", "Results for: " + query);
            
            return "results";
            
        } catch (Exception e) {
            model.addAttribute("error", "Search service is currently unavailable");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    
    /**
     * Informações de uma página específica
     */
    @GetMapping("/page-info")
    public String pageInfo(
            @RequestParam("url") String url,
            Model model) {
        
        try {
            var pageInfo = searchServiceClient.getPageInfo(url);
            var relatedLinks = searchServiceClient.getRelatedLinks(url);
            
            model.addAttribute("pageInfo", pageInfo);
            model.addAttribute("relatedLinks", relatedLinks);
            model.addAttribute("pageTitle", "Page Info");
            
            return "page-info";
            
        } catch (Exception e) {
            model.addAttribute("error", "Unable to retrieve page information");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
```

#### StatsController.java
```java
package com.example.webapp.controller;

import com.example.webapp.service.SearchServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stats")
public class StatsController {
    
    private final SearchServiceClient searchServiceClient;
    
    @Autowired
    public StatsController(SearchServiceClient searchServiceClient) {
        this.searchServiceClient = searchServiceClient;
    }
    
    @GetMapping
    public String statistics(Model model) {
        try {
            var stats = searchServiceClient.getStatistics();
            
            model.addAttribute("stats", stats);
            model.addAttribute("pageTitle", "System Statistics");
            
            return "stats";
            
        } catch (Exception e) {
            model.addAttribute("error", "Unable to retrieve statistics");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
```

### 2.5 Tratamento Global de Exceções

```java
package com.example.webapp.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.rmi.RemoteException;

/**
 * Manipulador global de exceções
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RemoteException.class)
    public String handleRMIException(RemoteException e, Model model) {
        model.addAttribute("error", "Search service is currently unavailable");
        model.addAttribute("errorMessage", "Please try again later");
        model.addAttribute("technicalDetails", e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        model.addAttribute("error", "An unexpected error occurred");
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("error", "System error");
        model.addAttribute("errorMessage", "Please contact support");
        model.addAttribute("technicalDetails", e.getMessage());
        return "error";
    }
}
```

---

## 3. Templates Thymeleaf

### 3.1 index.html (Página Inicial)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle}">Googol Search</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <header>
            <h1 class="logo">Googol</h1>
        </header>
        
        <main>
            <div class="search-container">
                <form th:action="@{/search/results}" method="get">
                    <input 
                        type="text" 
                        name="q" 
                        placeholder="Search the web..." 
                        required 
                        autofocus
                        class="search-input"/>
                    <button type="submit" class="search-button">
                        Search
                    </button>
                </form>
            </div>
            
            <div class="quick-links">
                <a th:href="@{/stats}">System Statistics</a>
            </div>
        </main>
    </div>
</body>
</html>
```

### 3.2 results.html (Resultados da Pesquisa)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle}">Search Results</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <header>
            <h1 class="logo">
                <a th:href="@{/}">Googol</a>
            </h1>
            
            <!-- Search bar no topo -->
            <div class="search-bar-top">
                <form th:action="@{/search/results}" method="get">
                    <input 
                        type="text" 
                        name="q" 
                        th:value="${query}"
                        placeholder="Search the web..." 
                        required 
                        class="search-input-small"/>
                    <button type="submit" class="search-button-small">
                        Search
                    </button>
                </form>
            </div>
        </header>
        
        <main>
            <div class="results-info">
                <p>
                    Results for: <strong th:text="${query}"></strong>
                    (<span th:text="${resultCount}"></span> found)
                </p>
            </div>
            
            <!-- Sem resultados -->
            <div th:if="${results.isEmpty()}" class="no-results">
                <p>No results found for "<span th:text="${query}"></span>"</p>
                <p>Try different keywords or check your spelling</p>
            </div>
            
            <!-- Lista de resultados -->
            <div th:unless="${results.isEmpty()}" class="results-list">
                <div th:each="result, iterStat : ${results}" class="result-item">
                    <div class="result-header">
                        <span class="result-number" th:text="${iterStat.count}"></span>
                        <span class="result-references" 
                              th:text="${result.references} + ' reference(s)'"></span>
                    </div>
                    
                    <h3 class="result-title">
                        <a th:href="${result.url}" 
                           th:text="${result.title}"
                           target="_blank">Title</a>
                    </h3>
                    
                    <p class="result-url" th:text="${result.url}">URL</p>
                    
                    <p class="result-description" 
                       th:text="${result.description}">Description</p>
                    
                    <div class="result-actions">
                        <a th:href="@{/search/page-info(url=${result.url})}" 
                           class="link-button">
                            View Page Info
                        </a>
                    </div>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
```

### 3.3 page-info.html (Informações da Página)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle}">Page Information</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <header>
            <h1 class="logo">
                <a th:href="@{/}">Googol</a>
            </h1>
        </header>
        
        <main>
            <div class="page-info-container">
                <h2>Page Information</h2>
                
                <div class="info-section">
                    <h3>Title</h3>
                    <p th:text="${pageInfo.title}"></p>
                </div>
                
                <div class="info-section">
                    <h3>URL</h3>
                    <p>
                        <a th:href="${pageInfo.url}" 
                           th:text="${pageInfo.url}"
                           target="_blank"></a>
                    </p>
                </div>
                
                <div class="info-section" th:if="${pageInfo.description}">
                    <h3>Description</h3>
                    <p th:text="${pageInfo.description}"></p>
                </div>
                
                <div class="info-section" th:if="${relatedLinks != null && !relatedLinks.isEmpty()}">
                    <h3>Related Links (<span th:text="${relatedLinks.size()}"></span>)</h3>
                    <ul class="related-links-list">
                        <li th:each="link : ${relatedLinks}">
                            <a th:href="${link}" 
                               th:text="${link}"
                               target="_blank"></a>
                        </li>
                    </ul>
                </div>
                
                <div class="actions">
                    <a href="javascript:history.back()" class="button">
                        ← Back to Results
                    </a>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
```

### 3.4 stats.html (Estatísticas)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle}">System Statistics</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <header>
            <h1 class="logo">
                <a th:href="@{/}">Googol</a>
            </h1>
        </header>
        
        <main>
            <div class="stats-container">
                <h2>System Statistics</h2>
                
                <div class="stats-grid">
                    <div class="stat-card">
                        <h3>Active Barrels</h3>
                        <p class="stat-number" th:text="${stats.activeBarrels}">0</p>
                    </div>
                    
                    <div class="stat-card">
                        <h3>Total Pages Indexed</h3>
                        <p class="stat-number" th:text="${stats.totalPages}">0</p>
                    </div>
                    
                    <div class="stat-card">
                        <h3>Total Searches</h3>
                        <p class="stat-number" th:text="${stats.totalSearches}">0</p>
                    </div>
                    
                    <div class="stat-card">
                        <h3>Avg Response Time</h3>
                        <p class="stat-number" 
                           th:text="${stats.avgResponseTime} + ' ms'">0 ms</p>
                    </div>
                </div>
                
                <div class="top-searches" 
                     th:if="${stats.topSearches != null && !stats.topSearches.isEmpty()}">
                    <h3>Top 10 Searches</h3>
                    <ol>
                        <li th:each="search : ${stats.topSearches}">
                            <span th:text="${search.query}"></span>
                            <span class="search-count" 
                                  th:text="'(' + ${search.count} + ')'"></span>
                        </li>
                    </ol>
                </div>
                
                <div class="actions">
                    <a th:href="@{/}" class="button">← Back to Home</a>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
```

### 3.5 error.html (Página de Erro)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <header>
            <h1 class="logo">
                <a th:href="@{/}">Googol</a>
            </h1>
        </header>
        
        <main>
            <div class="error-container">
                <h2>Oops! Something went wrong</h2>
                
                <div class="error-message">
                    <p th:text="${error}">An error occurred</p>
                    <p class="error-detail" 
                       th:if="${errorMessage}"
                       th:text="${errorMessage}"></p>
                </div>
                
                <div class="error-technical" 
                     th:if="${technicalDetails}">
                    <details>
                        <summary>Technical Details</summary>
                        <pre th:text="${technicalDetails}"></pre>
                    </details>
                </div>
                
                <div class="actions">
                    <a th:href="@{/}" class="button">← Back to Home</a>
                    <a href="javascript:history.back()" class="button-secondary">
                        Go Back
                    </a>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
```

---

## 4. Configuração

### 4.1 application.properties

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Application Name
spring.application.name=Googol Web Search

# RMI Configuration
rmi.host=localhost
rmi.port=1099
rmi.service.name=SearchService

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.enabled=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8

# Logging
logging.level.root=INFO
logging.level.com.example.webapp=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Static Resources
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.cache.period=3600
```

### 4.2 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>googol-webapp</artifactId>
    <version>1.0.0</version>
    <name>Googol Web Application</name>
    <description>Spring Boot web interface for Googol search engine</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- DevTools (Opcional - Hot reload) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Lombok (Opcional - Reduz boilerplate) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Melhores Práticas

### 1. Connection Pool / Retry Logic

```java
@Service
public class ResilientSearchService {
    
    private final SearchServiceClient searchServiceClient;
    
    @Autowired
    public ResilientSearchService(SearchServiceClient searchServiceClient) {
        this.searchServiceClient = searchServiceClient;
    }
    
    /**
     * Pesquisa com retry automático
     */
    public List<SearchResult> searchWithRetry(String query, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                return searchServiceClient.performSearch(query);
            } catch (Exception e) {
                lastException = e;
                attempts++;
                
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed after " + maxRetries + " attempts", lastException);
    }
}
```

### 2. Caching com Spring Cache

```java
@Service
@EnableCaching
public class CachedSearchService {
    
    @Autowired
    private SearchServiceClient searchServiceClient;
    
    @Cacheable(value = "searchResults", key = "#query")
    public List<SearchResult> search(String query) {
        return searchServiceClient.performSearch(query);
    }
    
    @Cacheable(value = "pageInfo", key = "#url")
    public PageInfo getPageInfo(String url) {
        return searchServiceClient.getPageInfo(url);
    }
    
    @CacheEvict(value = {"searchResults", "pageInfo"}, allEntries = true)
    public void clearCache() {
        // Cache é limpo automaticamente
    }
}
```

**application.properties (adicionar):**
```properties
# Cache Configuration
spring.cache.type=simple
spring.cache.cache-names=searchResults,pageInfo
```

### 3. Health Check / Actuator

```java
@Component
public class RMIHealthIndicator implements HealthIndicator {
    
    @Autowired
    private SearchService searchService;
    
    @Override
    public Health health() {
        try {
            // Teste de conexão
            searchService.getStatistics();
            return Health.up()
                .withDetail("rmi", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("rmi", "Disconnected")
                .build();
        }
    }
}
```

**pom.xml (adicionar):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**application.properties (adicionar):**
```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

### 4. Async Processing

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-search-");
        executor.initialize();
        return executor;
    }
}

@Service
public class AsyncSearchService {
    
    @Autowired
    private SearchServiceClient searchServiceClient;
    
    @Async
    public CompletableFuture<List<SearchResult>> searchAsync(String query) {
        List<SearchResult> results = searchServiceClient.performSearch(query);
        return CompletableFuture.completedFuture(results);
    }
}
```

### 5. Validation e Sanitização

```java
@Controller
@Validated
public class SearchController {
    
    @GetMapping("/search/results")
    public String search(
            @RequestParam("q") 
            @NotBlank(message = "Query cannot be empty")
            @Size(min = 1, max = 500, message = "Query must be between 1 and 500 characters")
            String query,
            Model model) {
        
        // Sanitizar input
        String sanitizedQuery = sanitizeQuery(query);
        
        // Processar pesquisa
        // ...
    }
    
    private String sanitizeQuery(String query) {
        // Remover caracteres especiais perigosos
        return query.trim()
                   .replaceAll("[<>\"']", "")
                   .replaceAll("\\s+", " ");
    }
}
```

### 6. Logging Estruturado

```java
@Service
@Slf4j
public class SearchServiceClient {
    
    public List<SearchResult> performSearch(String query) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Performing search for query: {}", query);
            List<SearchResult> results = searchService.search(query);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Search completed in {}ms. Found {} results", duration, results.size());
            
            return results;
            
        } catch (Exception e) {
            log.error("Search failed for query: {}", query, e);
            throw new RuntimeException("Search failed", e);
        }
    }
}
```

---

## Execução

### 1. Iniciar o Servidor RMI
```bash
cd backend
mvn clean compile
java -cp target/classes com.example.searchservice.RMIServer
```

### 2. Iniciar a Aplicação Spring Boot
```bash
cd spring-boot-app
mvn spring-boot:run
```

### 3. Acessar no Browser
```
http://localhost:8080
```

---

## Troubleshooting

### Erro: "Connection refused"
- Verificar se o servidor RMI está em execução
- Confirmar host e porta no `application.properties`
- Verificar firewall

### Erro: "Service not found"
- Verificar se o nome do serviço está correto
- Confirmar que o serviço foi registrado no RMI registry

### Erro: "Class not found"
- Garantir que as interfaces RMI estão no classpath de ambas aplicações
- Considerar criar um módulo compartilhado com as interfaces

---

## Conclusão

Esta arquitetura fornece:

✅ **Separação de responsabilidades** - Web tier separado da lógica de negócio  
✅ **Escalabilidade** - Serviço RMI pode rodar em máquinas diferentes  
✅ **Resiliência** - Retry logic e health checks  
✅ **Performance** - Caching e processamento assíncrono  
✅ **Manutenibilidade** - Código organizado e testável  

Para projetos de produção, considere também:
- Load balancing do RMI service
- Circuit breaker pattern (Resilience4j)
- Distributed tracing (Sleuth/Zipkin)
- API REST como alternativa ao RMI direto
