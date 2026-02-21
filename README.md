# 💬 ChattingApplication

A simple multi-client desktop chat application built with **Java Swing** (GUI) and **Java Sockets** (networking). No database, no authentication — just connect and chat!

---

## 📁 Project Structure

```
ChattingApplication/
└── src/
    └── com/chatapp/
        ├── model/
        │   └── Message.java           ← Shared message class
        ├── server/
        │   ├── ChatServer.java        ← Server entry point
        │   └── ClientHandler.java     ← Handles each client connection
        └── client/
            ├── ChatClient.java        ← Client entry point (main method here)
            └── gui/
                ├── LoginFrame.java    ← Login screen
                ├── DashboardFrame.java← Online users list
                └── ChatFrame.java     ← Chat window
```

---

## ⚙️ Requirements

- Java JDK 8 or above
- Eclipse IDE (any edition)
- No external libraries needed!

---

## 🚀 How to Run

### Step 1 — Import Project in Eclipse
- Open Eclipse
- File → Import → Existing Projects into Workspace
- Select your `ChattingApplication` folder

### Step 2 — Run the Server
- Open `ChatServer.java`
- Right click → **Run As → Java Application**
- You should see: `Chat Server started on port 12345`

### Step 3 — Run the Client
- Open `ChatClient.java`
- Right click → **Run As → Java Application**
- Login screen will appear — enter your name and click **Join**

### Step 4 — Run Multiple Clients
- Repeat Step 3 to open more clients
- Each client enters a different name
- They will see each other in the online users list

---

## 💡 How to Chat

1. Enter your name on the login screen and click **Join**
2. You will see the **Dashboard** showing all online users
3. **Double click** on any user to send a chat request
4. The other user will see a popup — they can **Accept** or **Decline**
5. If accepted, a **Chat Window** opens for both users
6. Type your message and press **Enter** or click **Send**

---

## 📨 Message Types

| Type           | Description                        |
|----------------|------------------------------------|
| `JOIN`         | Client connects with a username    |
| `LEAVE`        | Client disconnects                 |
| `USER_LIST`    | Server sends online users list     |
| `CHAT_REQUEST` | User A requests to chat with User B|
| `CHAT_ACCEPT`  | User B accepts the request         |
| `CHAT_DECLINE` | User B declines the request        |
| `MESSAGE`      | Actual chat message between users  |

---

## 🎨 Features

- ✅ Dark theme UI throughout
- ✅ Multiple clients can connect simultaneously
- ✅ See who is online in real time
- ✅ Chat request → Accept / Decline flow
- ✅ Message bubbles (blue = sent, grey = received)
- ✅ Auto scroll to latest message
- ✅ Press Enter to send message

---

## 🔧 Configuration

Server runs on `localhost` port `12345` by default.

To change the port, update these two lines:

**In `ChatServer.java`:**
```java
private static final int PORT = 12345;
```

**In `ChatClient.java`:**
```java
private static final int SERVER_PORT = 12345;
```

---

## 🛣️ Future Plans

- [ ] Chat history (save previous messages)
- [ ] Group chat support
- [ ] File sharing
- [ ] User authentication (login/register)
- [ ] Database integration

---

## 👨‍💻 Author

Built as a learning project to understand Java Networking and Swing GUI.
```
