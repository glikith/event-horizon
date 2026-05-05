# Event Horizon

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![HTML](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)
![No Dependencies](https://img.shields.io/badge/Dependencies-brightgreen?style=for-the-badge)

> A full-stack event booking system with a Java HTTP backend, vanilla HTML frontend, no frameworks, no build tools, no external libraries.

---

## Overview

Event Horizon is a two-tier web app for managing and booking events. The backend is a raw Java HTTP server using only the JDK's built-in `com.sun.net.httpserver`. Data is persisted to local `.dat` files. The frontend is a single HTML file that talks to the backend via REST. Users can register, browse events, and book or cancel tickets. Admins can create and delete events.

---

## Features

| Feature | Description |
|---|---|
| User Auth | Register and login with username/password; sessions stored in localStorage |
| Role System | Admin and regular user roles; admin flag set at registration |
| Event Browsing | View all events with name, date, and available ticket count |
| Ticket Booking | Book one ticket per action; capacity enforced server-side |
| Ticket Cancellation | Users can void their own tickets; seat count is restored |
| Admin Panel | Admins can create events (ID, name, date, capacity) and delete them |
| File Persistence | Users, events, and tickets saved to `users.dat`, `events.dat`, `tickets.dat` |
| Default Admin | A default `admin / admin123` account is created on first run |
| Zero Dependencies | Backend uses only standard JDK — no Maven, no Gradle, no third-party JARs |

---

## Tech Stack

```
Backend        : Java (com.sun.net.httpserver) — runs on port 8080
Frontend       : HTML5, Tailwind CSS (CDN), Vanilla JavaScript
Persistence    : Java Object Serialization (.dat files)
Fonts          : JetBrains Mono, Playfair Display (Google Fonts)
```

---

## Running the Project

Both files need to be in the same directory. The Java backend must be running before you open the HTML file.

### Step 1: Compile the backend

```bash
javac EventHorizon.java
```

### Step 2: Start the server

```bash
java EventHorizon
```

The server starts on `http://localhost:8080`. You should see it running with no output unless an error occurs. Three `.dat` files will be created in the same directory on first run.

### Step 3: Open the frontend

Open `index.html` directly in your browser:

```bash
# macOS
open index.html

# Linux
xdg-open index.html

# Windows
start index.html
```

Or just double-click `index.html` in your file explorer.

### Default Admin Credentials

```
Username : admin
Password : admin123
```

---

## API Endpoints

All endpoints are served at `http://localhost:8080/api`.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/register` | Register a new user |
| `POST` | `/api/login` | Login and receive user info |
| `GET` | `/api/events` | List all events |
| `POST` | `/api/events` | Create a new event (admin) |
| `DELETE` | `/api/events` | Delete an event by ID (admin) |
| `GET` | `/api/tickets?username=X` | Get tickets for a user |
| `POST` | `/api/tickets` | Book a ticket |
| `DELETE` | `/api/tickets` | Cancel a ticket |

---

## Author

[Gummadi Likith](https://github.com/glikith)
