// Allows users to add new reminders for themselves, and allows users to view reminders for any date.
// Todo: error handling
// Written by Ben Torgerson

import java.sql.*;
import java.util.Scanner;
import java.time.*;

public class Driver {

	public static void main(String[] args) throws SQLException {
		Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/reminderapp?autoReconnect=true&useSSL=false", "USER", "PASS");
		Statement myStmt = myConn.createStatement();
		Scanner scanner = new Scanner(System.in);
		
		// Login
		String userName = logIn(myStmt, scanner);
		System.out.println("Welcome, " + userName + "!");
		
		// Reminders today:
		printTodaysReminders(myStmt, userName);
		
		// View future reminders or add new ones
		while (true) {
			System.out.println("Would you like to 'add' new reminders, 'view' current reminders, or 'close' the program?");
			
			
			String userInput = scanner.nextLine();
			
			if (userInput.toLowerCase().equals("add")) {
				addReminders(myStmt, scanner, userName);
			}
			else if (userInput.toLowerCase().equals("view")) {
				viewReminders(myStmt, scanner, userName);
			}
			else if(userInput.toLowerCase().equals("close") || userInput.toLowerCase().equals("quit")) {
				break;
			}
		}
		scanner.close();
		
		System.out.println("Thank you for using reminderApp, we hope to see you soon!");
	}
	// Logs the user in, or allows them to create an account and then logs them in.
	public static String logIn(Statement myStmt, Scanner scanner) throws SQLException {
		String userName = "";
		String password = "";
		boolean validated = false;
		String userInput;
		while (validated == false) {
			System.out.println("Would you like to 'Create' a new account, or 'Login'?");
			userInput = scanner.nextLine();
			
			
			if (userInput.toLowerCase().equals("login")) {
				while (true) {
					System.out.println("Please enter your username:");
					userName = scanner.nextLine();
					System.out.println("Please enter your password: ");
					password = scanner.nextLine();
					ResultSet actualPass = myStmt.executeQuery("select Password from users where user_name = " + "'" + userName + "'");
					actualPass.next();

					if (password.equals(actualPass.getString("Password"))) {
						validated = true;
						break;
					}
					else {
						System.out.println("Your login was incorrect. Please try again!");
					}
				}
			}
			
			else if (userInput.toLowerCase().equals("create")) {
				while (validated == false) {
					System.out.println("What is your username?");
					userName = scanner.nextLine();
					System.out.println("What is your password?");
					String firstPass = scanner.nextLine();
					System.out.println("Please type your password again to confirm it.");
					String secondPass = scanner.nextLine();
					if (firstPass.equals(secondPass)) {
						
						myStmt.executeUpdate("INSERT INTO users (user_name, password) VALUES( '" + userName + "', '" + firstPass + "')");
						validated = true;
					}
					else {
						System.out.println("Your passwords did not match up. Please try again!");
					}
				}
				
			}
			else {
				System.out.println("The input was not detected. Please try again!");
			}	
				
		}
		return userName;
	}
	// Prints all of the users' reminders for that day when they log in
	public static void printTodaysReminders(Statement myStmt, String userName) throws SQLException {
		LocalDate todaysDate = java.time.LocalDate.now();
		String currentDate = todaysDate.toString();
		
		int userID = getUserID(myStmt, userName);
		String todaysYear = currentDate.substring(0,4);
		String todaysMonth = currentDate.substring(5,7);
		String todaysDay = currentDate.substring(8);
		
		ResultSet myCount = myStmt.executeQuery("select count(reminder) from reminders where user_id = " + "'" + Integer.toString(userID) + "'" + " and day = " + "'" + todaysDay + "'" + " and month = " + "'" + todaysMonth + "'" + " and year = " + "'" + todaysYear + "'");
		myCount.next();
		System.out.print("You have ");
		System.out.print(myCount.getInt("count(reminder)"));
		System.out.println(" reminders today.");
		
		
		ResultSet myResults = myStmt.executeQuery("select * from reminders where user_id = " + "'" + Integer.toString(userID) + "'" + " and day = " + "'" + todaysDay + "'" + " and month = " + "'" + todaysMonth + "'" + " and year = " + "'" + todaysYear + "'" + "order by time");
		
		while (myResults.next()) {
			System.out.println(myResults.getString("time") + ": " + myResults.getString("reminder"));
		}
	}
	// Allows users to view reminders for any specific day
	public static void viewReminders(Statement myStmt, Scanner scanner, String userName) throws SQLException {
		System.out.println("Please give the day, month, and year of the reminders you want to look at(DDMMYYYY), or 'all'.");
		String answer = scanner.nextLine();
		int userID = getUserID(myStmt, userName);
		if (answer.equals("all")) {
			ResultSet allReminders = myStmt.executeQuery("select * from reminders where user_id = " + "'" + Integer.toString(userID) + "'" + "order by time");
			
			while (allReminders.next()) {
				System.out.println(allReminders.getString("day") + "/" + allReminders.getString("month") + "/" + allReminders.getString("year") + "," + allReminders.getString("time") + ": " + allReminders.getString("reminder"));
			}
		}
		else {
			
			//build
			String viewDay = answer.substring(0, 2);
			String viewMonth = answer.substring(2, 4);
			String viewYear = answer.substring(4);
			String printDate = (viewDay + "/" + viewMonth + "/" + viewYear);
			System.out.println("For the date of " + printDate + ", you have the following reminders:");
			
			ResultSet viewReminders = myStmt.executeQuery("select * from reminders where user_id = " + "'" + Integer.toString(userID) + "'" + "and day = " + "'" + viewDay + "'" + "and month = " + "'" + viewMonth + "'" + "and year = " + "'" + viewYear + "'");
			
			while (viewReminders.next()) {
				System.out.println(viewReminders.getString("time") + ": " + viewReminders.getString("reminder"));
			}
		}
	}
	// Allows users to add in a reminder for any specific day.
	public static void addReminders(Statement myStmt, Scanner scanner, String userName) throws SQLException {
		
		System.out.println("When would you like to add this reminder? (Format: DDMMYYYY)");
		String newDate = scanner.nextLine();
		
		String newDay = newDate.substring(0, 2);
		String newMonth = newDate.substring(2,4);
		String newYear = newDate.substring(4);
		System.out.println("What time is this reminder? (Format: HH:MM XM)");
		String newTime = scanner.nextLine();
		System.out.println("What should this reminder say?");
		String newReminder = scanner.nextLine();
;
		String userID = Integer.toString(getUserID(myStmt, userName));
		
		myStmt.executeUpdate("INSERT INTO reminders (user_id, day, month, year, time, reminder) VALUES('" + userID + "', '" + newDay + "', '" + newMonth + "', '" + newYear + "', '" + newTime + "', '" + newReminder + "')");
		
		System.out.println("Reminder added.");
	}
	// takes in the userName, and gets the userID associated with it.
	public static int getUserID(Statement myStmt, String userName) throws SQLException {
		ResultSet user = myStmt.executeQuery("select user_id from users where user_name = " + "'" + userName + "'");
		user.next();
		return user.getInt("user_ID");
	}
}
