import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

// User class
class User implements Serializable {
    private String username;
    private String password;
    private boolean isAdmin;

    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public boolean isAdmin() {
        return isAdmin;
    }
}

// Event class
class Event implements Serializable {
    private int id;
    private String name;
    private String date;
    private int totalTickets;
    private int ticketsBooked;

    public Event(int id, String name, String date, int totalTickets) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.totalTickets = totalTickets;
        this.ticketsBooked = 0;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDate() {
        return date;
    }
    public int getTotalTickets() {
        return totalTickets;
    }
    public int getTicketsBooked() {
        return ticketsBooked;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public boolean bookTicket(int quantity) {
        if(ticketsBooked + quantity <= totalTickets) {
            ticketsBooked += quantity;
            return true;
        }
        return false;
    }

    public void cancelTicket(int quantity) {
        if(ticketsBooked - quantity >= 0) {
            ticketsBooked -= quantity;
        }
    }
}

// Ticket class
class Ticket implements Serializable {
    private int ticketId;
    private int eventId;
    private String username;

    public Ticket(int ticketId, int eventId, String username) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.username = username;
    }

    public int getTicketId() {
        return ticketId;
    }
    public int getEventId() {
        return eventId;
    }
    public String getUsername() {
        return username;
    }
}

// UserService handles users and file storage
class UserService {
    private Map<String, User> users = new HashMap<>();
    private final String fileName = "users.dat";

    public UserService() {
        loadUsers();
    }

    public boolean register(String username, String password, boolean isAdmin) {
        if(users.containsKey(username)) return false;
        users.put(username, new User(username, password, isAdmin));
        saveUsers();
        return true;
    }

    public User login(String username, String password) {
        User user = users.get(username);
        if(user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            users = (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // File may not exist yet, initialize empty users
            users = new HashMap<>();
            // Create a default admin user if empty
            users.put("admin", new User("admin", "admin123", true));
            saveUsers();
        }
    }

    private void saveUsers() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(users);
        } catch(IOException e) {
            System.out.println("Failed to save users.");
        }
    }
}

// EventService manages events, tickets, and persistence
class EventService {
    protected List<Event> events = new ArrayList<>();
    protected List<Ticket> tickets = new ArrayList<>();
    private int ticketCounter = 1;
    private final String eventsFile = "events.dat";
    private final String ticketsFile = "tickets.dat";

    public EventService() {
        loadEvents();
        loadTickets();
        ticketCounter = tickets.stream().mapToInt(Ticket::getTicketId).max().orElse(0) + 1;
    }

    public void addEvent(Event event) {
        events.add(event);
        saveEvents();
    }

    public boolean updateEvent(int eventId, String name, String date, int totalTickets) {
        for (Event e : events) {
            if (e.getId() == eventId) {
                e.setName(name);
                e.setDate(date);
                e.setTotalTickets(totalTickets);
                saveEvents();
                return true;
            }
        }
        return false;
    }

    public boolean deleteEvent(int eventId) {
        boolean removed = events.removeIf(e -> e.getId() == eventId);
        if (removed) {
            tickets.removeIf(t -> t.getEventId() == eventId);
            saveEvents();
            saveTickets();
        }
        return removed;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Ticket> getUserTickets(String username) {
        List<Ticket> userTickets = new ArrayList<>();
        for(Ticket t : tickets) {
            if(t.getUsername().equals(username)) userTickets.add(t);
        }
        return userTickets;
    }

    public boolean bookTickets(int eventId, String username, int quantity) {
        for(Event event : events) {
            if(event.getId() == eventId) {
                if(event.bookTicket(quantity)) {
                    for(int i = 0; i < quantity; i++) {
                        tickets.add(new Ticket(ticketCounter++, eventId, username));
                    }
                    saveEvents();
                    saveTickets();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean cancelTicket(int ticketId, String username) {
        Iterator<Ticket> iterator = tickets.iterator();
        while(iterator.hasNext()) {
            Ticket t = iterator.next();
            if(t.getTicketId() == ticketId && t.getUsername().equals(username)) {
                for(Event e : events) {
                    if(e.getId() == t.getEventId()) {
                        e.cancelTicket(1);
                        break;
                    }
                }
                iterator.remove();
                saveEvents();
                saveTickets();
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void loadEvents() {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(eventsFile))) {
            events = (List<Event>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            events = new ArrayList<>();
            if(events.isEmpty()) {
                events.add(new Event(1, "Concert", "2025-11-15", 100));
                events.add(new Event(2, "Tech Conference", "2025-12-01", 50));
                saveEvents();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTickets() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ticketsFile))) {
            tickets = (List<Ticket>) ois.readObject();
        } catch(IOException | ClassNotFoundException e) {
            tickets = new ArrayList<>();
        }
    }

    private void saveEvents() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(eventsFile))) {
            oos.writeObject(events);
        } catch(IOException e) {
            System.out.println("Failed to save events.");
        }
    }

    private void saveTickets() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ticketsFile))) {
            oos.writeObject(tickets);
        } catch(IOException e) {
            System.out.println("Failed to save tickets.");
        }
    }
}

// Main application class
class EventHorizon {

    static UserService userService = new UserService();
    static EventService eventService = new EventService();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/events", new EventsHandler());
        server.createContext("/api/tickets", new TicketsHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("HTTP Server started on port 8080");
    }

    static void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
    }

    static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
}

    static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    static String extractJsonValue(String json, String key) {
        String patternStr = "\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\s]+))";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
        java.util.regex.Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return null;
    }

    // Helper to build simple JSON strings to avoid dependencies
    static String escapeJson(String str) {
        if(str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;
            
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                String username = extractJsonValue(body, "username");
                String password = extractJsonValue(body, "password");
                String isAdminStr = extractJsonValue(body, "isAdmin");
                boolean isAdmin = "true".equalsIgnoreCase(isAdminStr);

                if (username == null || password == null) {
                    sendResponse(exchange, 400, "{\"error\":\"Missing username or password\"}");
                    return;
                }

                if (userService.login(username, "") != null) {
                    sendResponse(exchange, 400, "{\"error\":\"User already exists\"}");
                    return;
                }

                if (userService.register(username, password, isAdmin)) {
                    sendResponse(exchange, 200, "{\"message\":\"Registration successful\"}");
                } else {
                    sendResponse(exchange, 500, "{\"error\":\"Registration failed\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                String username = extractJsonValue(body, "username");
                String password = extractJsonValue(body, "password");

                if (username == null || password == null) {
                    sendResponse(exchange, 400, "{\"error\":\"Missing username or password\"}");
                    return;
                }

                User currentUser = userService.login(username, password);
                if (currentUser != null) {
                    // Send user info as JSON
                    String jsonResponse = String.format("{\"username\":\"%s\",\"isAdmin\":%b}", 
                        escapeJson(currentUser.getUsername()), currentUser.isAdmin());
                    sendResponse(exchange, 200, jsonResponse);
                } else {
                    sendResponse(exchange, 401, "{\"error\":\"Invalid username or password\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    static class EventsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder sb = new StringBuilder("[");
                List<Event> events = eventService.getEvents();
                for (int i = 0; i < events.size(); i++) {
                    Event e = events.get(i);
                    sb.append(String.format("{\"id\":%d,\"name\":\"%s\",\"date\":\"%s\",\"totalTickets\":%d,\"ticketsBooked\":%d}",
                            e.getId(), escapeJson(e.getName()), escapeJson(e.getDate()), e.getTotalTickets(), e.getTicketsBooked()));
                    if (i < events.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                try {
                    int id = Integer.parseInt(extractJsonValue(body, "id"));
                    String name = extractJsonValue(body, "name");
                    String date = extractJsonValue(body, "date");
                    int totalTickets = Integer.parseInt(extractJsonValue(body, "totalTickets"));

                    eventService.addEvent(new Event(id, name, date, totalTickets));
                    sendResponse(exchange, 200, "{\"message\":\"Event added\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid input data\"}");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                try {
                    int id = Integer.parseInt(extractJsonValue(body, "id"));
                    String name = extractJsonValue(body, "name");
                    String date = extractJsonValue(body, "date");
                    int totalTickets = Integer.parseInt(extractJsonValue(body, "totalTickets"));

                    if (eventService.updateEvent(id, name, date, totalTickets)) {
                        sendResponse(exchange, 200, "{\"message\":\"Event updated\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\":\"Event not found\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid input data\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                int id = -1;
                if (query != null && query.contains("id=")) {
                    String[] parts = query.split("id=");
                    if (parts.length > 1) {
                        id = Integer.parseInt(parts[1].split("&")[0]);
                    }
                } else {
                     String body = readRequestBody(exchange);
                     String idStr = extractJsonValue(body, "id");
                     if(idStr != null) id = Integer.parseInt(idStr);
                }
                
                if (id != -1 && eventService.deleteEvent(id)) {
                    sendResponse(exchange, 200, "{\"message\":\"Event deleted\"}");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Event not found or invalid ID\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    static class TicketsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                List<Ticket> tickets;
                if (query != null && query.contains("username=")) {
                    String username = query.split("username=")[1].split("&")[0];
                    tickets = eventService.getUserTickets(username);
                } else {
                    tickets = new ArrayList<>(eventService.tickets);
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < tickets.size(); i++) {
                    Ticket t = tickets.get(i);
                    sb.append(String.format("{\"ticketId\":%d,\"eventId\":%d,\"username\":\"%s\"}",
                            t.getTicketId(), t.getEventId(), escapeJson(t.getUsername())));
                    if (i < tickets.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                try {
                    int eventId = Integer.parseInt(extractJsonValue(body, "eventId"));
                    String username = extractJsonValue(body, "username");
                    int qty = Integer.parseInt(extractJsonValue(body, "quantity"));

                    if (eventService.bookTickets(eventId, username, qty)) {
                        sendResponse(exchange, 200, "{\"message\":\"Tickets booked\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Could not book tickets\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid input data\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                try {
                    int ticketId = Integer.parseInt(extractJsonValue(body, "ticketId"));
                    String username = extractJsonValue(body, "username");

                    if (eventService.cancelTicket(ticketId, username)) {
                        sendResponse(exchange, 200, "{\"message\":\"Ticket cancelled\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Could not cancel ticket\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid input data\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }
}
