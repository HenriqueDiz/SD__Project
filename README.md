# [SD] - Googol

Googol is a distributed web search engine developed as an academic project for the Distributed Systems course. The system implements a client-server architecture using RPC/RMI, featuring an automatic web crawler for URL indexing, an inverted index for efficient searching, and multiple replicated Storage Barrels to ensure availability and fault tolerance. The application provides a modern web interface built with Next.js and integrates with external APIs, such as Hacker News and LLMs, allowing users to perform fast searches and obtain contextualized analyses of web pages. The project demonstrates advanced distributed computing concepts, including reliable multicast, parallel data processing, and synchronous/asynchronous real-time communication via WebSockets.

To compile and run everything on a single PC:
- `make run-all` (which runs)
  - `mvn clean compile`
  - `make run-backend`
  - `make run-frontend`

The components can also be run individually:

- `make run-q`     # URL Queue (port 8181)
- `make run-g`     # Gateway (port 8183)
- `make run-b1`    # Barrel 1 (port 8186)
- `make run-b2`    # Barrel 2 (port 8182)
- `make run-b3`    # Barrel 3 (port 8187)
- `make run-b4`    # Barrel 4 (port 8188)
- `make run-d`     # Downloader
- `make run-c`     # Client CLI

When running them separately, the IP addresses in the `application.properties` and `config.properties` files must be changed accordingly.

Grade: 100%
