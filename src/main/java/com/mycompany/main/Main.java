package com.mycompany.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

// JSON library for reading and writing the stored messages file
// ATTRIBUTION: org.json library - https://github.com/stleary/JSON-java
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// =============================================================
//  LOGIN CLASS - Part 1
// =============================================================

/**
 * Login class for the Chat Application - Part 1: Registration and Login.
 *
 * Handles username, password and cell phone validation, registration,
 * login checking and the welcome message.
 *
 * @author Mc Chopper
 * @version 2.0
 */
class Login {

    // The user's first name (used in the welcome message)
    private String firstName;

    // The user's last name (used in the welcome message)
    private String lastName;

    // The registered username
    private String registeredUsername;

    // The registered password
    private String registeredPassword;

    // The registered cell phone number
    private String registeredCellPhone;

    /**
     * Creates a Login object for the given name.
     *
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     */
    public Login(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName  = lastName;
    }

    /**
     * Checks the username: it must contain an underscore and be no more than five characters.
     *
     * @param username The username to check.
     * @return true if valid, false otherwise.
     */
    public boolean checkUserName(String username) {
        // A missing username is never valid
        if (username == null) return false;

        // Check for an underscore and a maximum length of five
        boolean hasUnderscore = username.contains("_");
        boolean correctLength = username.length() <= 5;
        return hasUnderscore && correctLength;
    }

    /**
     * Checks the password: at least 8 characters, a capital letter, a number and a special character.
     *
     * @param password The password to check.
     * @return true if all rules are met, false otherwise.
     */
    public boolean checkPasswordComplexity(String password) {
        // Too short or missing means invalid
        if (password == null || password.length() < 8) return false;

        // Flags for each rule
        boolean hasCapital = false;
        boolean hasNumber  = false;
        boolean hasSpecial = false;

        // Look at each character and set the flags
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))           hasCapital = true;
            else if (Character.isDigit(c))          hasNumber  = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        // All three rules must be true
        return hasCapital && hasNumber && hasSpecial;
    }

    /**
     * Checks the cell phone number: a '+' followed by 9 to 11 digits.
     *
     * ATTRIBUTION: Regex researched from https://www.w3schools.com/java/java_regex.asp
     *
     * @param cellPhoneNumber The number to check.
     * @return true if correctly formatted, false otherwise.
     */
    public boolean checkCellPhoneNumber(String cellPhoneNumber) {
        if (cellPhoneNumber == null) return false;
        return cellPhoneNumber.matches("^\\+[0-9]{9,11}$");
    }

    /**
     * Registers the user if all three checks pass, and stores the details.
     *
     * @param username        The chosen username.
     * @param password        The chosen password.
     * @param cellPhoneNumber The cell phone number.
     * @return A message describing the result.
     */
    public String registerUser(String username, String password, String cellPhoneNumber) {
        // Check the username first
        if (!checkUserName(username))
            return "Username is not correctly formatted; please ensure that your username "
                 + "contains an underscore and is no more than five characters in length.";

        // Then the password
        if (!checkPasswordComplexity(password))
            return "Password is not correctly formatted; please ensure that the password "
                 + "contains at least eight characters, a capital letter, a number, and a special character.";

        // Then the cell phone number
        if (!checkCellPhoneNumber(cellPhoneNumber))
            return "Cell phone number incorrectly formatted or does not contain international code; "
                 + "please correct the number and try again.";

        // Everything passed, so save the details
        this.registeredUsername  = username;
        this.registeredPassword  = password;
        this.registeredCellPhone = cellPhoneNumber;
        return "Username successfully captured.\nPassword successfully captured.\nCell phone number successfully added.";
    }

    /**
     * Checks a username and password against the registered details.
     *
     * @param username The entered username.
     * @param password The entered password.
     * @return true if both match, false otherwise.
     */
    public boolean loginUser(String username, String password) {
        // Nobody can log in before registering
        if (registeredUsername == null || registeredPassword == null) return false;
        return registeredUsername.equals(username) && registeredPassword.equals(password);
    }

    /**
     * Returns the login result message.
     *
     * @param username The entered username.
     * @param password The entered password.
     * @return A welcome message on success, or a try-again message on failure.
     */
    public String returnLoginStatus(String username, String password) {
        if (loginUser(username, password))
            return "Welcome " + firstName + " " + lastName + " it is great to see you.";
        return "Username or password incorrect, please try again.";
    }

    // ---- Getters ----
    public String getFirstName()           { return firstName; }
    public String getLastName()            { return lastName; }
    public String getRegisteredUsername()  { return registeredUsername; }
    public String getRegisteredCellPhone() { return registeredCellPhone; }
}


// =============================================================
//  MESSAGE CLASS - Part 2 and Part 3
// =============================================================

/**
 * Message class for the Chat Application - Parts 2 and 3.
 *
 * One object holds one message. Five static arrays hold every message
 * from the session (and the stored ones loaded from the JSON file).
 *
 * @author Mc Chopper
 * @version 2.0
 */
class Message {

    // ---- Fields for one message ----

    // The 10-digit random message ID
    private String messageID;

    // The message's number in this sending session (taken from the loop)
    private int numMessagesSent;

    // The recipient's cell phone number
    private String recipient;

    // The message text (max 250 characters)
    private String messageText;

    // The hash made from the ID, the number, and the first and last word
    private String messageHash;

    // ---- Part 3: the five arrays (filled while the program runs, never hard-coded) ----

    // Array 1: sent messages, each as [messageID, messageHash, recipient, messageText]
    private static ArrayList<String[]> sentMessages = new ArrayList<>();

    // Array 2: disregarded messages, each as [recipient, messageText]
    private static ArrayList<String[]> disregardedMessages = new ArrayList<>();

    // Array 3: stored messages, each as [messageID, messageHash, recipient, messageText]
    private static ArrayList<String[]> storedMessages = new ArrayList<>();

    // Array 4: every message hash created this session
    private static ArrayList<String> messageHashes = new ArrayList<>();

    // Array 5: every message ID created this session
    private static ArrayList<String> messageIDs = new ArrayList<>();

    // The name of the JSON file (unit tests change this to a temporary file)
    private static String jsonFilePath = "stored_messages.json";

    // One Random object shared by all messages
    private static final Random random = new Random();

    /**
     * Creates a message. The ID is made automatically.
     *
     * @param recipient     The recipient's cell phone number.
     * @param messageText   The message text.
     * @param messageNumber The message's number in this session (from the loop).
     */
    public Message(String recipient, String messageText, int messageNumber) {
        this.recipient       = recipient;
        this.messageText     = messageText;
        this.numMessagesSent = messageNumber;
        this.messageID       = generateMessageID();
    }

    /**
     * Makes a random 10-digit message ID.
     *
     * @return The 10-digit ID as text (it can start with zeros).
     */
    private static String generateMessageID() {
        // A random number from 0 up to 9,999,999,999
        long number = (long) (random.nextDouble() * 10000000000L);

        // %010d adds leading zeros so the ID is always exactly 10 digits
        return String.format("%010d", number);
    }

    /**
     * Checks the message ID is no more than ten characters.
     *
     * @return true if the ID is valid.
     */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    /**
     * Checks a recipient number: a '+' followed by 9 to 11 digits.
     *
     * ATTRIBUTION: Same regex as Part 1, from https://www.w3schools.com/java/java_regex.asp
     *
     * @param number The number to check.
     * @return true if the number is valid.
     */
    public static boolean isValidRecipient(String number) {
        return number != null && number.matches("^\\+[0-9]{9,11}$");
    }

    /**
     * Checks this message's recipient and returns a message for the user.
     *
     * @return A success or error message.
     */
    public String checkRecipientCell() {
        if (isValidRecipient(recipient))
            return "Cell phone number successfully captured.";
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
             + "Please correct the number and try again.";
    }

    /**
     * Checks that a message is no longer than 250 characters.
     *
     * @param text The message text.
     * @return true if the text is 250 characters or fewer.
     */
    public static boolean isValidLength(String text) {
        return text != null && text.length() <= 250;
    }

    /**
     * Makes the message hash and adds it to the hash and ID arrays.
     *
     * Format: first two digits of the ID : message number : first word + last word (uppercase)
     * Example: ID 0012345678, number 1, "Hi Thanks" gives "00:1:HITHANKS".
     *
     * @return The message hash.
     */
    public String createMessageHash() {
        // The first two digits of the ID
        String firstTwoID = messageID.substring(0, 2);

        // Split the text into words and take the first and last word
        String[] words   = messageText.trim().split("\\s+");
        String firstWord = words[0];
        String lastWord  = words[words.length - 1];

        // Build the hash and make it uppercase
        String hash = firstTwoID + ":" + numMessagesSent + ":" + firstWord + lastWord;
        this.messageHash = hash.toUpperCase();

        // Add the hash and ID to their Part 3 arrays
        messageHashes.add(messageHash);
        messageIDs.add(messageID);

        return messageHash;
    }

    /**
     * Sends, disregards, or stores this message.
     *
     * 1 = Send: adds to the sent array.
     * 2 = Disregard: adds to the disregarded array.
     * 3 = Store: adds to the stored array and saves the JSON file.
     *
     * @param choice The option chosen (1, 2 or 3).
     * @return A message for the user.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(new String[]{ messageID, messageHash, recipient, messageText });
                return "Message successfully sent.";

            case 2:
                disregardedMessages.add(new String[]{ recipient, messageText });
                return "Message disregarded.";

            case 3:
                storedMessages.add(new String[]{ messageID, messageHash, recipient, messageText });
                saveStoredMessagesToFile();
                return "Message successfully stored.";

            default:
                return "Invalid option. Please select 1, 2, or 3.";
        }
    }

    /**
     * Lists every sent message in the order: ID, hash, recipient, message.
     *
     * @return The formatted list, or a message saying nothing was sent.
     */
    public static String printSentMessages() {
        if (sentMessages.isEmpty()) return "No messages sent yet.";

        StringBuilder output = new StringBuilder();
        output.append("=== All Sent Messages ===\n");
        for (String[] msg : sentMessages) {
            output.append("Message ID: ").append(msg[0]).append("\n");
            output.append("Message Hash: ").append(msg[1]).append("\n");
            output.append("Recipient: ").append(msg[2]).append("\n");
            output.append("Message: ").append(msg[3]).append("\n");
            output.append("-------------------------\n");
        }
        return output.toString();
    }

    /**
     * Returns how many messages have been sent.
     *
     * @return The number of sent messages.
     */
    public static int returnTotalMessagesSent() {
        return sentMessages.size();
    }

    // =========================================================
    //  PART 3 - JSON FILE
    // =========================================================

    /**
     * Saves the whole stored array to the JSON file.
     * The file is rewritten each time, so it always matches the array.
     *
     * ATTRIBUTION: Writing JSON with org.json, https://github.com/stleary/JSON-java
     * File writing: https://www.w3schools.com/java/java_files_create.asp
     */
    public static void saveStoredMessagesToFile() {
        // Build a JSON array with one object for each stored message
        JSONArray jsonArray = new JSONArray();
        for (String[] msg : storedMessages) {
            JSONObject obj = new JSONObject();
            obj.put("messageID",   msg[0]);
            obj.put("messageHash", msg[1]);
            obj.put("recipient",   msg[2]);
            obj.put("message",     msg[3]);
            jsonArray.put(obj);
        }

        // Write the array to the file, indented to make it easy to read
        try {
            Files.writeString(Paths.get(jsonFilePath), jsonArray.toString(4));
        } catch (IOException e) {
            System.out.println("Error writing to JSON file: " + e.getMessage());
        }
    }

    /**
     * Reads the JSON file and fills the stored array.
     * Called once when the program starts.
     *
     * ATTRIBUTION: Reading JSON with org.json, https://github.com/stleary/JSON-java
     * File reading: https://www.w3schools.com/java/java_files_read.asp
     */
    public static void loadStoredMessagesFromFile() {
        storedMessages.clear();
        Path path = Paths.get(jsonFilePath);

        // No file yet means nothing is stored yet
        if (!Files.exists(path)) {
            System.out.println("No stored messages file found yet. Starting fresh.");
            return;
        }

        try {
            String content = Files.readString(path);

            // An empty file has nothing to load
            if (content.trim().isEmpty()) return;

            // Turn the text into a JSON array and copy each message into the arrays
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String id    = obj.getString("messageID");
                String hash  = obj.getString("messageHash");
                String recip = obj.getString("recipient");
                String text  = obj.getString("message");

                storedMessages.add(new String[]{ id, hash, recip, text });
                messageIDs.add(id);
                messageHashes.add(hash);
            }
            System.out.println("Loaded " + storedMessages.size() + " stored message(s).");

        } catch (IOException e) {
            System.out.println("Could not read the stored messages file: " + e.getMessage());
        } catch (JSONException e) {
            System.out.println("The stored messages file is not in the correct JSON format. Starting fresh.");
        }
    }

    // =========================================================
    //  PART 3 - STORED MESSAGES MENU FEATURES
    // =========================================================

    /**
     * Joins the sent and stored arrays into one list, so the search features can use both.
     *
     * @return A new list holding every sent and stored message.
     */
    private static ArrayList<String[]> allMessages() {
        ArrayList<String[]> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        return all;
    }

    /**
     * Feature a: shows the sender and recipient of every stored message.
     *
     * @param senderCell The logged-in user's cell phone number.
     * @return The formatted list, or a message saying nothing is stored.
     */
    public static String displayStoredSenderRecipient(String senderCell) {
        if (storedMessages.isEmpty()) return "No stored messages found.";

        StringBuilder output = new StringBuilder();
        output.append("=== Stored Messages - Sender and Recipient ===\n");
        for (String[] msg : storedMessages) {
            output.append("Sender: ").append(senderCell).append("\n");
            output.append("Recipient: ").append(msg[2]).append("\n");
            output.append("-------------------------\n");
        }
        return output.toString();
    }

    /**
     * Feature b: finds the longest message in the sent and stored arrays.
     *
     * @return The recipient and text of the longest message, or a not-found message.
     */
    public static String displayLongestMessage() {
        String[] longest = null;

        // Keep the message whose text is longer than the best so far
        for (String[] msg : allMessages()) {
            if (longest == null || msg[3].length() > longest[3].length())
                longest = msg;
        }

        if (longest == null) return "No messages found.";
        return "Longest message:\nRecipient: " + longest[2] + "\nMessage: " + longest[3];
    }

    /**
     * Feature c: searches for a message ID and returns the recipient and text.
     *
     * @param searchID The message ID to find.
     * @return The recipient and text, or a not-found message.
     */
    public static String searchByMessageID(String searchID) {
        for (String[] msg : allMessages()) {
            if (msg[0].equals(searchID))
                return "Recipient: " + msg[2] + "\nMessage: " + msg[3];
        }
        return "Message ID not found.";
    }

    /**
     * Feature d: finds every sent or stored message for one recipient.
     *
     * @param recipientNumber The recipient's cell phone number.
     * @return All matching messages, or a not-found message.
     */
    public static String searchByRecipient(String recipientNumber) {
        StringBuilder output = new StringBuilder();
        boolean found = false;

        for (String[] msg : allMessages()) {
            if (msg[2].equals(recipientNumber)) {
                output.append("Message: ").append(msg[3]).append("\n");
                found = true;
            }
        }

        if (!found) return "No messages found for recipient: " + recipientNumber;
        return output.toString().trim();
    }

    /**
     * Feature e: deletes a message by its hash from the sent or stored array.
     * If a stored message is deleted, the JSON file is rewritten too.
     *
     * @param hash The message hash to delete.
     * @return A confirmation or not-found message.
     */
    public static String deleteByMessageHash(String hash) {
        String upperHash = hash.trim().toUpperCase();

        // Look in the sent array first
        for (int i = 0; i < sentMessages.size(); i++) {
            String[] msg = sentMessages.get(i);
            if (msg[1].equals(upperHash)) {
                sentMessages.remove(i);
                removeHashAndID(msg[0], msg[1]);
                return "Message: \"" + msg[3] + "\" successfully deleted.";
            }
        }

        // Then look in the stored array
        for (int i = 0; i < storedMessages.size(); i++) {
            String[] msg = storedMessages.get(i);
            if (msg[1].equals(upperHash)) {
                storedMessages.remove(i);
                removeHashAndID(msg[0], msg[1]);
                saveStoredMessagesToFile();
                return "Message: \"" + msg[3] + "\" successfully deleted.";
            }
        }

        return "Message hash not found. No message deleted.";
    }

    /**
     * Removes one ID and one hash from the Part 3 arrays after a delete.
     *
     * @param id   The message ID to remove.
     * @param hash The message hash to remove.
     */
    private static void removeHashAndID(String id, String hash) {
        messageIDs.remove(id);
        messageHashes.remove(hash);
    }

    /**
     * Feature f: shows the full details of every sent and stored message.
     *
     * @return The formatted report, or a message saying there is nothing to show.
     */
    public static String displayReport() {
        ArrayList<String[]> all = allMessages();
        if (all.isEmpty()) return "No messages to display in report.";

        StringBuilder report = new StringBuilder();
        report.append("========== MESSAGE REPORT ==========\n");
        for (String[] msg : all) {
            report.append("Message ID: ").append(msg[0]).append("\n");
            report.append("Message Hash: ").append(msg[1]).append("\n");
            report.append("Recipient: ").append(msg[2]).append("\n");
            report.append("Message: ").append(msg[3]).append("\n");
            report.append("----------------------------\n");
        }
        report.append("====================================");
        return report.toString();
    }

    // ---- Getters ----
    public String getMessageID()    { return messageID; }
    public String getMessageHash()  { return messageHash; }
    public String getRecipient()    { return recipient; }
    public String getMessageText()  { return messageText; }
    public int getNumMessagesSent() { return numMessagesSent; }

    // Array getters, used by the unit tests
    public static ArrayList<String[]> getSentMessages()        { return sentMessages; }
    public static ArrayList<String[]> getDisregardedMessages() { return disregardedMessages; }
    public static ArrayList<String[]> getStoredMessages()      { return storedMessages; }
    public static ArrayList<String>   getMessageHashes()       { return messageHashes; }
    public static ArrayList<String>   getMessageIDs()          { return messageIDs; }

    /**
     * Clears all five arrays. Only the unit tests call this, so each test starts clean.
     * The program itself never clears the arrays during a session.
     */
    public static void resetAllData() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
    }

    /**
     * Changes the JSON file location. Only the unit tests use this,
     * so they write to a temporary file instead of the real one.
     *
     * @param path The new file path.
     */
    public static void setJsonFilePath(String path) {
        jsonFilePath = path;
    }
}


// =============================================================
//  MAIN CLASS - Parts 1, 2 and 3 combined
// =============================================================

/**
 * Main class for the QuickChat application.
 *
 * Console only. Flow:
 *   Part 1: register, then log in.
 *   Part 2: send messages with the main menu (send, show recent, stored, quit).
 *   Part 3: the Stored Messages sub-menu with six features.
 *
 * @author Mc Chopper
 * @version 2.0
 */
public class Main {

    // One scanner for all console input
    static Scanner scanner = new Scanner(System.in);

    /**
     * Starts the program.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {

        // =========================================================
        // PART 1 - REGISTRATION
        // =========================================================

        System.out.println("======================================");
        System.out.println("   Welcome to the Chat Application    ");
        System.out.println("======================================\n");

        // Ask for the user's name
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine().trim();

        Login login = new Login(firstName, lastName);

        System.out.println("\n--- Registration ---");

        // Keep asking until the username is valid
        String username;
        while (true) {
            System.out.print("Enter a username (must contain '_' and be max 5 characters): ");
            username = scanner.nextLine().trim();
            if (login.checkUserName(username)) {
                System.out.println("Username successfully captured.");
                break;
            }
            System.out.println("Username is not correctly formatted; please ensure that "
                    + "your username contains an underscore and is no more than five characters in length.");
        }

        // Keep asking until the password is valid
        String password;
        while (true) {
            System.out.print("Enter a password (min 8 chars, uppercase, number, special char): ");
            password = scanner.nextLine().trim();
            if (login.checkPasswordComplexity(password)) {
                System.out.println("Password successfully captured.");
                break;
            }
            System.out.println("Password is not correctly formatted; please ensure that the password "
                    + "contains at least eight characters, a capital letter, a number, and a special character.");
        }

        // Keep asking until the cell phone number is valid
        String cellPhone;
        while (true) {
            System.out.print("Enter your cell phone number with international code (e.g. +27838968976): ");
            cellPhone = scanner.nextLine().trim();
            if (login.checkCellPhoneNumber(cellPhone)) {
                System.out.println("Cell phone number successfully added.");
                break;
            }
            System.out.println("Cell phone number incorrectly formatted or does not contain "
                    + "international code; please correct the number and try again.");
        }

        // Save the validated details
        login.registerUser(username, password, cellPhone);

        // =========================================================
        // PART 1 - LOGIN
        // =========================================================

        System.out.println("\n--- Login ---");

        // Keep asking until the login succeeds
        while (true) {
            System.out.print("Enter your username: ");
            String loginUsername = scanner.nextLine().trim();

            System.out.print("Enter your password: ");
            String loginPassword = scanner.nextLine().trim();

            System.out.println(login.returnLoginStatus(loginUsername, loginPassword));

            if (login.loginUser(loginUsername, loginPassword)) break;

            System.out.println("Please try again.\n");
        }

        // =========================================================
        // PART 2 + PART 3 - QUICKCHAT
        // =========================================================

        // Load any stored messages from the JSON file into the arrays
        Message.loadStoredMessagesFromFile();

        System.out.println("\nWelcome to QuickChat.");

        // Ask how many messages to send in this session
        int numMessages = askForNumberOfMessages();

        // ---- Main menu (repeats until the user quits) ----
        boolean running = true;
        while (running) {
            System.out.println("\n--- QuickChat Menu ---");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Stored Messages");
            System.out.println("4) Quit");
            System.out.print("Select an option: ");

            String menuChoice = scanner.nextLine().trim();

            switch (menuChoice) {
                case "1":
                    sendMessages(numMessages);
                    break;

                case "2":
                    System.out.println("Coming Soon.");
                    break;

                case "3":
                    storedMessagesMenu(login.getRegisteredCellPhone());
                    break;

                case "4":
                    running = false;
                    System.out.println("\nGoodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please select 1, 2, 3, or 4.");
            }
        }

        scanner.close();
    }

    /**
     * Asks how many messages to send. Repeats until a number above zero is entered.
     *
     * @return The number of messages.
     */
    private static int askForNumberOfMessages() {
        while (true) {
            System.out.print("\nHow many messages would you like to send? ");
            String input = scanner.nextLine().trim();
            try {
                int count = Integer.parseInt(input);
                if (count > 0) return count;
                System.out.println("Please enter a number greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    /**
     * Asks for a recipient number. Repeats until the number is valid.
     *
     * @return A valid recipient number.
     */
    private static String askForRecipient() {
        while (true) {
            System.out.print("Enter recipient cell number (with international code e.g. +27718693002): ");
            String recipient = scanner.nextLine().trim();

            if (Message.isValidRecipient(recipient)) {
                System.out.println("Cell phone number successfully captured.");
                return recipient;
            }
            System.out.println("Cell phone number is incorrectly formatted or does not contain "
                    + "an international code. Please correct the number and try again.");
        }
    }

    /**
     * Asks for the message text. Repeats until the text is not empty and is 250 characters or fewer.
     *
     * @return The message text.
     */
    private static String askForMessageText() {
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            String text = scanner.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("Message cannot be empty. Please try again.");
                continue;
            }
            if (Message.isValidLength(text)) {
                System.out.println("Message ready to send.");
                return text;
            }

            // Tell the user how many characters to remove
            int excess = text.length() - 250;
            System.out.println("Message exceeds 250 characters by " + excess + "; please reduce the size.");
        }
    }

    /**
     * Asks what to do with a message: send (1), disregard (2) or store (3).
     * Repeats until 1, 2 or 3 is entered.
     *
     * @return The chosen option.
     */
    private static int askForAction() {
        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Select an option: ");

            String input = scanner.nextLine().trim();
            if (input.equals("1") || input.equals("2") || input.equals("3"))
                return Integer.parseInt(input);

            System.out.println("Invalid input. Please enter 1, 2, or 3.");
        }
    }

    /**
     * Sends the chosen number of messages using a for-loop.
     *
     * For each message: get the recipient, get the text, create the message,
     * show its ID and hash, then ask to send, disregard or store.
     * A disregarded message makes the loop go back to the same number.
     * At the end, all sent messages and the total are shown.
     *
     * @param numMessages How many messages to send.
     */
    private static void sendMessages(int numMessages) {
        for (int i = 1; i <= numMessages; i++) {
            System.out.println("\n--- Message " + i + " of " + numMessages + " ---");

            String recipient   = askForRecipient();
            String messageText = askForMessageText();

            // Only create the message once the input is valid
            Message msg = new Message(recipient, messageText, i);
            String hash = msg.createMessageHash();
            System.out.println("Message ID generated: " + msg.getMessageID());
            System.out.println("Message Hash: " + hash);

            int action = askForAction();
            System.out.println(msg.sentMessage(action));

            if (action == 2) {
                // Disregarded: step the loop back so this same number is entered again
                System.out.println("Please re-enter message " + i + ".");
                i--;
            }
        }

        System.out.println("\n======================================");
        System.out.println(Message.printSentMessages());
        System.out.println("Total messages sent: " + Message.returnTotalMessagesSent());
        System.out.println("======================================");
    }

    /**
     * Part 3: the Stored Messages sub-menu with features a to f.
     *
     * @param senderCell The logged-in user's cell phone number.
     */
    private static void storedMessagesMenu(String senderCell) {
        boolean inSubMenu = true;

        while (inSubMenu) {
            System.out.println("\n--- Stored Messages Menu ---");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest message");
            System.out.println("c) Search for a message by Message ID");
            System.out.println("d) Search all messages for a particular recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display full message report");
            System.out.println("x) Back to main menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "a":
                    System.out.println(Message.displayStoredSenderRecipient(senderCell));
                    break;

                case "b":
                    System.out.println(Message.displayLongestMessage());
                    break;

                case "c":
                    System.out.print("Enter Message ID to search: ");
                    String searchID = scanner.nextLine().trim();
                    System.out.println(Message.searchByMessageID(searchID));
                    break;

                case "d":
                    System.out.print("Enter recipient number to search: ");
                    String searchRecipient = scanner.nextLine().trim();
                    System.out.println(Message.searchByRecipient(searchRecipient));
                    break;

                case "e":
                    System.out.print("Enter message hash to delete: ");
                    String deleteHash = scanner.nextLine().trim();
                    System.out.println(Message.deleteByMessageHash(deleteHash));
                    break;

                case "f":
                    System.out.println(Message.displayReport());
                    break;

                case "x":
                    inSubMenu = false;
                    break;

                default:
                    System.out.println("Invalid option. Please select a, b, c, d, e, f, or x.");
            }
        }
    }
}