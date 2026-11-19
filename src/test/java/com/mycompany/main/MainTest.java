package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuickChatTest {

    // A temporary folder, so the tests never touch your real stored_messages.json
    @TempDir
    Path tempDir;

    private Login login;

    // Test data from the Part 3 brief
    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @BeforeEach
    void setUp() {
        login = new Login("Mc", "Chopper");

        // Start every test with empty arrays and the temporary JSON file
        Message.resetAllData();
        Message.setJsonFilePath(jsonPath().toString());
    }

    // Path to the temporary JSON file
    private Path jsonPath() {
        return tempDir.resolve("stored_messages.json");
    }

    // Builds a message, creates its hash, then sends, disregards, or stores it
    private Message createAndHandle(String recipient, String text, int number, int action) {
        Message message = new Message(recipient, text, number);
        message.createMessageHash();
        message.sentMessage(action);
        return message;
    }

    // Creates the five messages from the Part 3 test data
    private void createTestData() {
        message1 = createAndHandle("+27834557896", "Did you get the cake?", 1, 1);
        message2 = createAndHandle("+27838884567", "Where are you? You are late! I have asked you to be on time.", 2, 3);
        message3 = createAndHandle("+27834484567", "Yohoooo, I am at your gate.", 3, 2);

        // The brief's test data uses 0838884567 here, which the app's validation would reject.
        // The constructor does not validate, so the test can still use it.
        message4 = createAndHandle("0838884567", "It is dinner time !", 4, 1);
        message5 = createAndHandle("+27838884567", "Ok, I am leaving without you.", 5, 3);
    }

    // ---------------------------------------------------------
    // PART 1: Username tests
    // ---------------------------------------------------------

    @Test
    void validUsernameShouldPass() {
        assertTrue(login.checkUserName("mc_1"));
    }

    @Test
    void usernameWithoutUnderscoreShouldFail() {
        assertFalse(login.checkUserName("mc123"));
    }

    @Test
    void usernameLongerThanFiveCharactersShouldFail() {
        assertFalse(login.checkUserName("mc_123"));
    }

    // ---------------------------------------------------------
    // PART 1: Password tests
    // ---------------------------------------------------------

    @Test
    void validPasswordShouldPass() {
        assertTrue(login.checkPasswordComplexity("Password1!"));
    }

    @Test
    void passwordWithoutUppercaseShouldFail() {
        assertFalse(login.checkPasswordComplexity("password1!"));
    }

    @Test
    void passwordWithoutNumberShouldFail() {
        assertFalse(login.checkPasswordComplexity("Password!"));
    }

    @Test
    void passwordWithoutSpecialCharacterShouldFail() {
        assertFalse(login.checkPasswordComplexity("Password123"));
    }

    @Test
    void passwordShorterThanEightCharactersShouldFail() {
        assertFalse(login.checkPasswordComplexity("Ab1!xyz"));
    }

    // ---------------------------------------------------------
    // PART 1: Cell phone tests
    // ---------------------------------------------------------

    @Test
    void validInternationalPhoneNumberShouldPass() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    void phoneNumberWithoutPlusShouldFail() {
        assertFalse(login.checkCellPhoneNumber("27838968976"));
    }

    @Test
    void phoneNumberWithLettersShouldFail() {
        assertFalse(login.checkCellPhoneNumber("+27838ABC976"));
    }

    // ---------------------------------------------------------
    // PART 1: Registration and login tests
    // ---------------------------------------------------------

    @Test
    void validRegistrationShouldSucceed() {
        String result = login.registerUser("mc_1", "Password1!", "+27838968976");

        assertTrue(result.contains("Username successfully captured."));
        assertEquals("mc_1", login.getRegisteredUsername());
        assertEquals("+27838968976", login.getRegisteredCellPhone());
    }

    @Test
    void loginShouldSucceedWithCorrectCredentials() {
        login.registerUser("mc_1", "Password1!", "+27838968976");
        assertTrue(login.loginUser("mc_1", "Password1!"));
    }

    @Test
    void loginShouldFailWithIncorrectPassword() {
        login.registerUser("mc_1", "Password1!", "+27838968976");
        assertFalse(login.loginUser("mc_1", "WrongPass1!"));
    }

    @Test
    void loginShouldFailBeforeRegistration() {
        assertFalse(login.loginUser("mc_1", "Password1!"));
    }

    // ---------------------------------------------------------
    // PART 2: Message ID, recipient, and length tests
    // ---------------------------------------------------------

    @Test
    void messageIDShouldContainTenDigits() {
        Message message = new Message("+27838968976", "Hello there", 1);

        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
        assertTrue(message.getMessageID().matches("[0-9]{10}"));
    }

    @Test
    void validRecipientShouldBeAccepted() {
        assertTrue(Message.isValidRecipient("+27838968976"));
    }

    @Test
    void recipientWithoutInternationalCodeShouldBeRejected() {
        assertFalse(Message.isValidRecipient("0838884567"));
    }

    @Test
    void messageOf250CharactersShouldBeAccepted() {
        assertTrue(Message.isValidLength("A".repeat(250)));
    }

    @Test
    void messageOf251CharactersShouldBeRejected() {
        assertFalse(Message.isValidLength("A".repeat(251)));
    }

    // ---------------------------------------------------------
    // PART 2: Hash and sending tests
    // ---------------------------------------------------------

    @Test
    void messageHashShouldUseCorrectFormat() {
        Message message = new Message("+27838968976", "Hello World", 1);
        String hash = message.createMessageHash();

        assertTrue(hash.matches("[0-9]{2}:1:HELLOWORLD"));
    }

    @Test
    void hashShouldUseTheLoopNumber() {
        Message message = new Message("+27838968976", "Hi there friend", 4);
        String hash = message.createMessageHash();

        assertTrue(hash.contains(":4:"));
    }

    @Test
    void sendingMessageShouldAddToSentArray() {
        Message message = new Message("+27834557896", "Did you get the cake?", 1);
        message.createMessageHash();

        assertEquals("Message successfully sent.", message.sentMessage(1));
        assertEquals(1, Message.getSentMessages().size());
        assertEquals(1, Message.returnTotalMessagesSent());
    }

    @Test
    void disregardingMessageShouldAddToDisregardedArray() {
        Message message = new Message("+27834484567", "Yohoooo, I am at your gate.", 1);
        message.createMessageHash();
        message.sentMessage(2);

        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals(0, Message.returnTotalMessagesSent());
    }

    @Test
    void invalidActionShouldBeRejected() {
        Message message = new Message("+27838968976", "Hello", 1);

        assertEquals("Invalid option. Please select 1, 2, or 3.", message.sentMessage(4));
    }

    @Test
    void hashAndIDArraysShouldBePopulated() {
        Message first  = new Message("+27834557896", "Hello one", 1);
        Message second = new Message("+27834557896", "Hello two", 2);
        String firstHash  = first.createMessageHash();
        String secondHash = second.createMessageHash();

        assertEquals(2, Message.getMessageHashes().size());
        assertEquals(2, Message.getMessageIDs().size());
        assertTrue(Message.getMessageHashes().contains(firstHash));
        assertTrue(Message.getMessageHashes().contains(secondHash));
    }

    @Test
    void printSentMessagesShouldListSentMessage() {
        Message message = new Message("+27834557896", "Did you get the cake?", 1);
        message.createMessageHash();
        message.sentMessage(1);

        String output = Message.printSentMessages();
        assertTrue(output.contains("Did you get the cake?"));
        assertTrue(output.contains("+27834557896"));
    }

    // ---------------------------------------------------------
    // PART 3: Arrays populated from the test data
    // ---------------------------------------------------------

    @Test
    void sentArrayShouldContainExpectedMessages() {
        createTestData();

        assertEquals(2, Message.getSentMessages().size());
        String output = Message.printSentMessages();
        assertTrue(output.contains("Did you get the cake?"));
        assertTrue(output.contains("It is dinner time !"));
    }

    @Test
    void disregardedArrayShouldContainExpectedMessage() {
        createTestData();

        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals("Yohoooo, I am at your gate.", Message.getDisregardedMessages().get(0)[1]);
    }

    @Test
    void storedArrayAndJsonFileShouldBePopulated() {
        createTestData();

        assertEquals(2, Message.getStoredMessages().size());
        assertTrue(Files.exists(jsonPath()));
    }

    @Test
    void messageHashAndIDArraysShouldHoldAllFiveMessages() {
        createTestData();

        assertEquals(5, Message.getMessageHashes().size());
        assertEquals(5, Message.getMessageIDs().size());
    }

    // ---------------------------------------------------------
    // PART 3: Stored messages menu features
    // ---------------------------------------------------------

    @Test
    void storedSenderRecipientShouldShowStoredRecipients() {
        createTestData();

        String output = Message.displayStoredSenderRecipient("+27821234567");
        assertTrue(output.contains("Recipient: +27838884567"));
        assertTrue(output.contains("Sender: +27821234567"));
    }

    @Test
    void longestMessageShouldBeFound() {
        createTestData();

        String output = Message.displayLongestMessage();
        assertTrue(output.contains("Where are you? You are late! I have asked you to be on time."));
    }

    @Test
    void searchByMessageIDShouldFindSentMessage() {
        createTestData();

        assertEquals(
                "Recipient: 0838884567\nMessage: It is dinner time !",
                Message.searchByMessageID(message4.getMessageID()));
    }

    @Test
    void searchByMessageIDShouldFindStoredMessage() {
        createTestData();

        String output = Message.searchByMessageID(message2.getMessageID());
        assertTrue(output.contains("Where are you? You are late!"));
    }

    @Test
    void searchByUnknownMessageIDShouldReportNotFound() {
        createTestData();

        assertEquals("Message ID not found.", Message.searchByMessageID("9999999999"));
    }

    @Test
    void searchByRecipientShouldFindStoredMessagesForThatRecipient() {
        createTestData();

        String output = Message.searchByRecipient("+27838884567");
        assertTrue(output.contains("Where are you? You are late!"));
        assertTrue(output.contains("Ok, I am leaving without you."));
    }

    @Test
    void searchByRecipientShouldReportNoMatches() {
        createTestData();

        assertEquals("No messages found for recipient: +27000000000",
                Message.searchByRecipient("+27000000000"));
    }

    @Test
    void deleteStoredMessageShouldRemoveItFromArraysAndFile() throws Exception {
        createTestData();

        String result = Message.deleteByMessageHash(message2.getMessageHash());

        assertTrue(result.contains("successfully deleted."));
        assertEquals(1, Message.getStoredMessages().size());
        assertFalse(Message.getMessageIDs().contains(message2.getMessageID()));
        assertFalse(Message.getMessageHashes().contains(message2.getMessageHash()));
        assertEquals("Message ID not found.", Message.searchByMessageID(message2.getMessageID()));

        // The JSON file should no longer contain the deleted message
        String fileContent = Files.readString(jsonPath());
        assertFalse(fileContent.contains("Where are you?"));
        assertTrue(fileContent.contains("Ok, I am leaving without you."));
    }

    @Test
    void deleteSentMessageShouldRemoveItFromSentArray() {
        createTestData();

        Message.deleteByMessageHash(message1.getMessageHash());

        assertEquals(1, Message.getSentMessages().size());
        assertFalse(Message.printSentMessages().contains("Did you get the cake?"));
    }

    @Test
    void deleteWithUnknownHashShouldReportNotFound() {
        createTestData();

        assertEquals("Message hash not found. No message deleted.",
                Message.deleteByMessageHash("ZZ:99:NOTHING"));
    }

    @Test
    void reportShouldContainSentAndStoredMessages() {
        createTestData();

        String report = Message.displayReport();
        assertTrue(report.contains(message1.getMessageHash()));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains(message2.getMessageHash()));
        assertTrue(report.contains("Ok, I am leaving without you."));
    }

    @Test
    void reportShouldSayWhenThereIsNothingToShow() {
        assertEquals("No messages to display in report.", Message.displayReport());
    }

    @Test
    void loadStoredMessagesShouldReadJsonFileIntoArray() {
        createTestData();

        // Clear the arrays, as if the program had just started
        Message.resetAllData();
        assertEquals(0, Message.getStoredMessages().size());

        // Read the file back in
        Message.loadStoredMessagesFromFile();

        assertEquals(2, Message.getStoredMessages().size());
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.getStoredMessages().get(0)[3]);
    }
}