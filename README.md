📚 Library Management System (Final Project)

This repository contains a microservices-based Library Management System, developed as a final project for the Web Services course (Winter 2025).
It demonstrates concepts such as REST APIs, service decomposition, Docker-based orchestration, and domain-driven design.

🚀 Features

Books Service – Manages book records (catalog, availability).

Patrons Service – Handles patron (library member) registration and details.

Staff Service – Manages library staff records.

Loans Service – Handles book borrowing/return transactions.

API Gateway – Provides a single entry point to access all services.

🏗️ Project Structure
Web-Services-Final_Project/
│── api-gateway/           # API Gateway service
│── books-service/         # Microservice for books
│── patrons-service/       # Microservice for patrons
│── staff-service/         # Microservice for staff
│── loans-service/         # Microservice for loans
│── docker-compose.yml     # Docker orchestration file
│── settings.gradle        # Gradle multi-project settings
│── gradlew / gradlew.bat  # Gradle wrapper
│── create-projects.bash   # Script to scaffold projects
│── test_all.bash          # Script to run all tests
│── library_domain_model.puml         # UML domain model
│── c4_l1_context_diagram.puml        # C4 Level 1 context diagram
│── c4_l2_container_diagram.puml      # C4 Level 2 container diagram
│── .gitignore
│── lombok.config

⚙️ Getting Started
Prerequisites

Java 17+

Gradle 7+

Docker & Docker Compose

Clone the Repository
git clone https://github.com/MattysLeduc/Web-Services-Final_Project.git
cd Web-Services-Final_Project

Build All Services
./gradlew build

Run with Docker
docker-compose up --build


This will start all services and the API gateway.

📖 API Endpoints

Each service exposes RESTful APIs (to be detailed as implementation progresses):

Books Service: /books

Patrons Service: /patrons

Staff Service: /staff

Loans Service: /loans

Gateway: Routes requests to the appropriate service.

🧪 Testing

Run all tests with:

./test_all.bash

📊 Architecture & Diagrams

The design follows Domain-Driven Design (DDD) and the C4 model:

library_domain_model.puml – Entity relationships

c4_l1_context_diagram.puml – System context

c4_l2_container_diagram.puml – Container-level architecture

👨‍💻 Contributors

Mattys Leduc – Developer

Would you like me to also add setup instructions for running each service individually (without Docker) so you can start them via Gradle while developing?
