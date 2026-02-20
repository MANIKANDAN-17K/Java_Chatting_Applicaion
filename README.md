# 💬 ChatApp — Desktop Chat Application

A real-world desktop chat application built with **Java Swing + TCP Sockets + SQLite**.  
Supports user registration, login, global chat, and private messaging — all running locally on your machine.

---

## 📁 Project Structure

```
ChatApplication/
├── src/
│   └── com/chatapp/
│       ├── client/
│       │   ├── ChatClient.java          # Socket connection to server
│       │   └── gui/
│       │       ├── LoginFrame.java      # Login window
│       │       ├── RegisterFrame.java   # Register window
│       │       ├── ChatFrame.java       # Main chat window
│       │       ├── ContactPanel.java    # Online users list
│       │       └── MessagePanel.java    # Chat message area
│       ├── server/
│       │   ├── ChatServer.java          # Server entry point
│       │   ├── ClientHandler.java       # Per-client thread
│       │   └── SessionManager.java      # Track online users
│       ├── model/
│       │   ├── User.java                # User entity
│       │   └── Message.java             # Message entity
│       ├── dao/
│       │   ├── UserDAO.java             # User DB operations
│       │   └── MessageDAO.java          # Message DB operations
│       ├── service/
│       │   ├── AuthService.java         # Login/Register logic
│       │   └── MessageService.java      # Message business logic
│       └── util/
│           ├── DBConnection.java        # SQLite connection + schema setup
│           ├── PasswordUtil.java        # BCrypt hashing
│           └── Constants.java           # App-wide constants
├── sql/
│   ├── schema.sql                       # Table definitions (reference)
│   └── seed.sql                         # Sample data
├── pom.xml                              # Maven build file
└── README.md
```

---

## 🛠️ Tech Stack

| Layer            | Technology               |
|------------------|--------------------------|
| GUI              | Java Swing               |
| Real-time chat   | Java Sockets (TCP)       |
| Database         | SQLite via JDBC          |
| Password hashing | BCrypt (jbcrypt 0.4)     |
| Build tool       | Maven                    |
| Java version     | Java 17+                 |

---

## ⚙️ Prerequisites

Make sure you have the following installed:

- **Java 17+** → `java -version`
- **Maven 3.8+** → `mvn -version`

That's it — SQLite is embedded, no external DB server needed.

---

## 🚀 How to Build & Run

### 1. Clone / Open the project

```bash
cd ChatApplication
```

### 2. Build — creates two fat JARs

```bash
mvn clean package
```

This generates inside `target/`:
- `ChatServer.jar` — the server
- `ChatClient.jar` — the client (run multiple instances)

---

### 3. Start the Server

Open a terminal and run:

```bash
java -jar target/ChatServer.jar
```

Expected output:
```
[10:00:00] [SERVER] Server started on port 9090
[10:00:00] [DB] SQLite driver loaded.
[10:00:00] [DB] Table ready: users
[10:00:00] [DB] Table ready: messages
[10:00:00] [SERVER] Waiting for clients...
```

> The database file `chatapp.db` is auto-created in the same directory on first run.

---

### 4. Launch Clients

Open one or more new terminals and run:

```bash
java -jar target/ChatClient.jar
```

The **Login window** will appear. You can:
- Click **Register** to create a new account
- Then **Login** to enter the chat

> Launch multiple clients to chat between users!

---

## 💡 Features

| Feature                  | Details                                          |
|--------------------------|--------------------------------------------------|
| 🔐 Register / Login      | BCrypt-hashed passwords stored in SQLite         |
| 💬 Global Chat           | Broadcast messages to all online users           |
| 🔒 Private Messages      | Click a user in the contact list to PM them      |
| 🟢 Online User List      | Contact panel updates live as users join/leave   |
| 📜 Chat History          | Last 50 messages loaded when you connect         |
| 🌙 Dark Theme UI         | Full dark-mode Swing GUI (Catppuccin palette)    |
| 🔌 Graceful Disconnect   | Server notifies all users when someone leaves    |

---

## 🔧 Chat Commands

These can be typed in the message box:

| Command              | Description                        |
|----------------------|------------------------------------|
| `/pm username text`  | Send a private message             |
| `/list`              | List all online users in chat      |
| `/quit`              | Disconnect from the server         |

---

## 🏗️ Architecture Overview

```
┌─────────────────────┐        TCP Socket (port 9090)       ┌──────────────────────┐
│     ChatClient      │ ──────────────────────────────────► │     ChatServer       │
│                     │                                      │                      │
│  LoginFrame         │  1. Send username on connect         │  ClientHandler       │
│  RegisterFrame      │  2. Send messages as plain text      │  (one per client)    │
│  ChatFrame          │  3. Receive broadcast messages       │                      │
│  ContactPanel       │  4. Receive /userlist updates        │  SessionManager      │
│  MessagePanel       │ ◄────────────────────────────────── │  (singleton)         │
└─────────────────────┘                                      └──────────┬───────────┘
         │                                                              │
         │  AuthService                                                 │
         │  → UserDAO ──────────────────────────────────────────► SQLite DB
         │  → MessageDAO                                          (chatapp.db)
         │  PasswordUtil (BCrypt)
```

### Message Flow

```
User types message → ChatClient.sendMessage()
  → Socket → ClientHandler.run()
    → SessionManager.broadcastAll()
      → each ClientHandler.sendMessage()
        → Socket → ChatClient listener thread
          → SwingUtilities.invokeLater()
            → ChatFrame.receiveMessage()
              → MessagePanel.addMessage()  ← bubble appears in UI
```

### Private Message Flow (`/pm`)

```
User types → /pm alice hello
  → ClientHandler detects "/pm" prefix
    → SessionManager.getClient("alice")
      → target.sendMessage("[PM from bob] hello")
        → alice's MessagePanel shows the PM bubble
```

---

## 🗄️ Database Schema

```sql
-- Users table
CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT    NOT NULL,
    created_at    DATETIME DEFAULT (datetime('now')),
    last_seen     DATETIME
);

-- Messages table
CREATE TABLE messages (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    sender     TEXT    NOT NULL,
    receiver   TEXT,                          -- NULL = global message
    content    TEXT    NOT NULL,
    is_private INTEGER NOT NULL DEFAULT 0,    -- 0 = global, 1 = private
    sent_at    DATETIME DEFAULT (datetime('now')),
    FOREIGN KEY (sender)   REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (receiver) REFERENCES users(username) ON DELETE SET NULL
);
```

---

## 🔒 Security Notes

- Passwords are **never stored in plain text** — BCrypt with cost factor 12
- Login returns a **generic error** ("Invalid username or password") to prevent username enumeration
- Input is **validated and sanitized** before hitting the database
- All DB queries use **PreparedStatements** — no SQL injection possible

---

## 🧪 Running Tests

```bash
mvn test
```

---

## 📦 Dependencies (auto-downloaded by Maven)

| Library         | Version   | Purpose                  |
|-----------------|-----------|--------------------------|
| sqlite-jdbc     | 3.45.1.0  | SQLite database driver   |
| jbcrypt         | 0.4       | BCrypt password hashing  |
| junit-jupiter   | 5.10.2    | Unit testing             |

---

## 🐛 Troubleshooting

**"Connection refused" on client launch**
→ Make sure `ChatServer.jar` is running first.

**"SQLite driver not found"**
→ Run `mvn clean package` again to pull dependencies.

**Black/blank Swing window on Linux**
→ Add `-Dawt.useSystemAAFontSettings=on` to the java command.

**Port 9090 already in use**
→ Change `SERVER_PORT` in `Constants.java` and rebuild.

---

## 👨‍💻 Author

Built with Java — Swing · Sockets · SQLite · BCrypt
