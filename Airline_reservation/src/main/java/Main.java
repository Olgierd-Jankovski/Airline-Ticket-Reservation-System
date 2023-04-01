import java.sql.*;
import java.util.Scanner;

public class Main
{ // i created a jar file, to run it in console, type "java -jar jarfile.jar"

    public static Connection getConnection() {
        Connection postGresConn = null;
        try {
            postGresConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/airline_reservation", "postgres", "admin") ;

        }
        catch (SQLException sqle) {
            System.out.println("Couldn't connect to database!");
            sqle.printStackTrace();
            return null ;
        }
        System.out.println("Successfully connected to Postgres Database");

        return postGresConn ;
    }

    public static void PrintUsersData(Connection con)
    {
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next())
            {
                System.out.println(rs.getString("name") + " " + rs.getString("surname") + " " + rs.getInt("nr"));
            }
        }
        catch (SQLException sqle)
        {
            ExceptionCaught(sqle);
            return;
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
    }

    public static void checkAndClose(Statement stmt, ResultSet rs)
    {
        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (rs != null)
            {
                rs.close();
            }
        }
        catch (SQLException sqle)
        {
            ExceptionCaught(sqle);
        }
    }

    public static boolean validateName(String input)
    {
        // check if input null
        if (input == null)
        {
            return false;
        }
        //maximum 50 characters, because of database
        if (input.length() > 50)
        {
            return false;
        }
        // input should be not empty, no numbers, no special characters, only letters
        return input.matches("[a-zA-Z]+");
    }

    public static boolean validateAge(String input)
    {
        // input can be null
        if (input == null)
        {
            return true;
        }
        // maximum 3 characters, because age can't be more than 999
        if (input.length() > 3)
        {
            return false;
        }
        // input should be not empty, no numbers, no special characters, only letters
        return input.matches("[0-9]+");
    }
    public static boolean validateAK(String input)
    {
        // input cant be null
        if (input == null)
        {
            return false;
        }
        // maximum length is a size of integer, so 10 characters
        if (input.length() > 10)
        {
            return false;
        }
        // input should be not empty, no numbers, no special characters, only letters
        return input.matches("[0-9]+");
    }


    public static int CreateNewAccount(Connection con)
    {
        //ask to enter in AK, name, surname, age
        String name = "", surname = "", age = "", AK = "";
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your AK: ");
        AK = sc.nextLine();
        System.out.println("Enter your name: ");
        name = sc.nextLine();
        System.out.println("Enter your surname: ");
        surname = sc.nextLine();
        System.out.println("Enter your age: ");
        age = sc.nextLine();

        // lets validate the input
        if(!validateName(name) || !validateName(surname) || !validateAge(age) || !validateAK(AK))
        {
            System.out.println("Invalid input!");
            return 0;
        }
        //  if age is empty, then database will give it a default value of 18 (unfortunately, we cannot use a direct optional value in java, so we have to do it this way)
        // then we will execute a query with 3  parameters, if age is not empty, then we will execute a query with 4 parameters
        // also, we need to return an id of the new user, so we will use RETURNING id
        // because we need to know the id of the new user, so we can use it in the next step
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            if(age.isEmpty())
            {
                stmt = con.createStatement();
                rs = stmt.executeQuery("INSERT INTO users (AK, name, surname) VALUES (" + AK + ", '" + name + "', '" + surname + "') RETURNING nr");
            }
            else
            {
                stmt = con.createStatement();
                rs = stmt.executeQuery("INSERT INTO users (AK, name, surname, age) VALUES (" + AK + ", '" + name + "', '" + surname + "', " + age + ") RETURNING nr");

            }
            rs.next();
            return rs.getInt("nr");
        }
        catch (SQLException sqle)
        {
            ExceptionCaught(sqle);
            return 0;
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
    }


    public static int Login(Connection con)
    {
        // ask to enter in a passowrd (it is a ID of the user)
        String password = "";
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your password: ");
        password = sc.nextLine();

        // lets validate the input
        if(!validateAK(password))
        {
            System.out.println("Invalid input!");
            return 0;
        }

        String query = "SELECT * FROM users WHERE nr=?";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(password));
            rs = stmt.executeQuery();
            if(rs.next())
            {
                return rs.getInt("nr");
            }
            else
            {
                System.out.println("Invalid password!");
                return 0;
            }
        }
        catch (SQLException sqle)
        {
            ExceptionCaught(sqle);
            return 0;
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
    }

    public static void ChangeAccountDetails(Connection con, int id)
    {
        // make another menu, ask what we want to change
        // 1. name
        // 2. surname
        // 3. age

        int option = 0;
        do
        {
            System.out.println("What do you want to change?");
            System.out.println("0. Save and exit");
            System.out.println("1. Name");
            System.out.println("2. Surname");
            System.out.println("3. Age");
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();
            try
            {
                option = Integer.parseInt(input);
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("Invalid input!");
                continue;
            }

            switch (option)
            {
                case 0:
                    System.out.println("Changes saved!");
                    break;
                case 1:
                    case 2:
                    case 3:
                    System.out.println("Enter new value: ");
                    String newValue = sc.nextLine();
                    if(option == 1)
                    {
                        if(!validateName(newValue))
                        {
                            System.out.println("Invalid input!");
                            continue;
                        }
                    }
                    else if(option == 2)
                    {
                        if(!validateName(newValue))
                        {
                            System.out.println("Invalid input!");
                            continue;
                        }
                    }
                    else if(option == 3)
                    {
                        if(!validateAge(newValue))
                        {
                            System.out.println("Invalid input!");
                            continue;
                        }
                    }

                    String query = "UPDATE users SET ";
                    if(option == 1)
                    {
                        query += "name = ?";
                    }
                    else if(option == 2)
                    {
                        query += "surname = ?";
                    }
                    else if(option == 3)
                    {
                        query += "age = ?";
                    }
                    query += " WHERE nr = ?";
                    java.sql.PreparedStatement stmt = null;

                    try
                    {
                        stmt = con.prepareStatement(query);
                        if(option == 1)
                        {
                            stmt.setString(1, newValue);
                        }
                        else if(option == 2)
                        {
                            stmt.setString(1, newValue);
                        }
                        else if(option == 3)
                        {
                            stmt.setInt(1, Integer.parseInt(newValue));
                        }
                        stmt.setInt(2, id);
                        int count = stmt.executeUpdate();
                        if (count > 0)
                        {
                            System.out.println("Changes saved!");
                        }
                    }
                    catch (SQLException sqle)
                    {
                        ExceptionCaught(sqle);
                    }
                    finally
                    {
                        checkAndClose(stmt, null);
                    }
                    break;
                default:
                    System.out.println("Invalid input!");
                    break;
            }
        } while (option != 0);
    }

    public static void PreviewAvailableFlightSchedules(Connection con)
    {
        // we will preview from the view: flights_information
        // we will show all the flights that are available
        String query = "SELECT * FROM flights_information";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            System.out.println("Available flights:");
            // it will print nr, departure_time, departure_location, arrival_location, seats_left, aviacompany_id

            System.out.println("nr\tdeparture_time\tdeparture_location\tarrival_location\tseats_left\taviacompany_id");
            while(rs.next())
            {
                System.out.println(rs.getInt("nr") + "\t" + rs.getString("departure_time") + "\t" + rs.getString("departure_location") + "\t" + rs.getString("arrival_location") + "\t" + rs.getInt("seats_left") + "\t" + rs.getInt("aviacompany_id"));
            }

        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
    }

    public static void PreviewAllFlightSchedules(Connection con)
    {
        // we will preview from the view: flights_information
        // we will show all the flights that are available
        String query = "SELECT * FROM flights";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            System.out.println("All flight schedules:");
            // it will print nr, departure_time, departure_location, arrival_location, seats_left, aviacompany_id

            System.out.println("nr\tdeparture_time\tdeparture_location\tarrival_location\tseats_left\taviacompany_id");
            while(rs.next())
            {
                System.out.println(rs.getInt("nr") + "\t" + rs.getString("departure_time") + "\t" + rs.getString("departure_location") + "\t" + rs.getString("arrival_location") + "\t" + rs.getInt("seats_left") + "\t" + rs.getInt("aviacompany_id"));
            }

        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
    }

    public static void PreviewClassInformation(Connection con)
    {
        // we will use there 2 tables
        // we have 3 tables at all:
        // 1. classes. It has nr and class_name
        // 2. functions. It has name and class_nr
        // 3. classes_information. It has class_name and count_functions

        // lets use classes and functions
        // one select should connect them
        // we will show all the classes and their functions
        String query = "SELECT classes.nr, classes.class_name, functions.name FROM classes INNER JOIN functions ON classes.nr = functions.class_id ORDER BY classes.nr";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            System.out.println("Classes and their functions:");
            // it will print nr, class_name, name

            System.out.println("Class ID\tClass Name\tAvailable function");
            while(rs.next())
            {
                System.out.println(rs.getInt("nr") + "\t" + rs.getString("class_name") + "\t" + rs.getString("name"));
            }

        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
        }
        finally
        {
            checkAndClose(stmt, rs);
        }

    }
    public static int PreviewFlightAvailableSeats(Connection con, int flight_id)
    {
        PreviewCurrentFlightInformation(con, flight_id);
        int overallSeats = 0;
        String query = "SELECT seats_left FROM flights WHERE nr = ?";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
    try {
    stmt = con.prepareStatement(query);
    stmt.setInt(1, flight_id);
    rs = stmt.executeQuery();
    int seats_left = 0;
    while (rs.next()) {
        seats_left = rs.getInt("seats_left");
    }
    // now we have the amount of seats
    // lets get the amount of tickets
    query = "SELECT COUNT(*) AS count FROM tickets WHERE flight_id = ?";
    stmt = con.prepareStatement(query);
    stmt.setInt(1, flight_id);
    rs = stmt.executeQuery();
    int tickets = 0;
    while (rs.next()) {
        tickets = rs.getInt("count");
    }
    // now we have the amount of tickets
    // lets make a temporary table with all the seats
    query = "CREATE TEMPORARY TABLE seats (seat_number INT)";
    stmt = con.prepareStatement(query);
    stmt.executeUpdate();
    // now we have the table
    // lets fill it with values
        overallSeats = seats_left + tickets;
    for (int i = 1; i <= overallSeats; i++) { // we are adding tickets because seats_left is the current amount of seats, but not the total amount of seats
        query = "INSERT INTO seats VALUES (?)";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, i);
        stmt.executeUpdate();
    }
    // now we have the table with all the seats
    // lets make a temporary table with all the seats that are in tickets
    query = "CREATE TEMPORARY TABLE tickets_seats (seat_number INT)";
    stmt = con.prepareStatement(query);
    stmt.executeUpdate();
    // now we have the table
    // lets fill it with values
    query = "SELECT seat_number FROM tickets WHERE flight_id = ?";
    stmt = con.prepareStatement(query);
    stmt.setInt(1, flight_id);
    rs = stmt.executeQuery();
    while (rs.next()) {
        query = "INSERT INTO tickets_seats VALUES (?)";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, rs.getInt("seat_number"));
        stmt.executeUpdate();
    }
    // now we have the table with all the seats that are in tickets
    // lets make a temporary table with all the seats that are not in tickets
    query = "CREATE TEMPORARY TABLE available_seats (seat_number INT)";
    stmt = con.prepareStatement(query);
    stmt.executeUpdate();
    // now we have the table
    // lets fill it with values
    query = "SELECT seat_number FROM seats WHERE seat_number NOT IN (SELECT seat_number FROM tickets WHERE flight_id = ?)";
    stmt = con.prepareStatement(query);
    stmt.setInt(1, flight_id);
    rs = stmt.executeQuery();
    while (rs.next()) {
        query = "INSERT INTO available_seats VALUES (?)";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, rs.getInt("seat_number"));
        stmt.executeUpdate();
    }
    // now we have the table with all the seats that are not in tickets
    // lets show them
    query = "SELECT * FROM available_seats";
    stmt = con.prepareStatement(query);
    rs = stmt.executeQuery();
    System.out.println("Available seats:");
    System.out.println("Seat number");
    while (rs.next())
    {
        System.out.println(rs.getInt("seat_number"));
    }
    // lets delete all the temporary tables
    query = "DROP TABLE seats";
    stmt = con.prepareStatement(query);
    stmt.executeUpdate();
    query = "DROP TABLE tickets_seats";
    stmt = con.prepareStatement(query);
    stmt.executeUpdate();
    query = "DROP TABLE available_seats";
    stmt = con.prepareStatement(query);
    stmt.executeUpdate();
    }
        catch (SQLException sqle) {
            ExceptionCaught(sqle);
            return 0;
        }
    finally
    {
        checkAndClose(stmt, rs);
    }
        return overallSeats;
    }
    public static void PreviewCurrentFlightInformation(Connection con, int flight_id)
    {
        // we will preview from the view: flights_information
        // we will show all the flights that are available
        String query = "SELECT * FROM flights_information WHERE nr = ?";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, flight_id);
            rs = stmt.executeQuery();

            System.out.println("Flight information:");
            // it will print nr, departure_time, departure_location, arrival_location, seats_left, aviacompany_id

            System.out.println("nr\tdeparture_time\tdeparture_location\tarrival_location\tseats_left\taviacompany_id");
            while(rs.next())
            {
                System.out.println(rs.getInt("nr") + "\t" + rs.getString("departure_time") + "\t" + rs.getString("departure_location") + "\t" + rs.getString("arrival_location") + "\t" + rs.getInt("seats_left") + "\t" + rs.getInt("aviacompany_id"));
            }

        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
    }
    public static void BookAFlight(Connection con, int UserID)
    {
        String flight_id = "", seat_number = "", class_id = "",  cost = "";

        PreviewAllFlightSchedules(con);
        // ask to select a flight, id
        System.out.println("Please select a flight by entering its ID:");
        Scanner scanner = new Scanner(System.in);
        flight_id = scanner.nextLine();
        if(!validateAK(flight_id))
        {
            System.out.println("Invalid input!");
            return;
        }
        // now ask to select a seat
        int maxSeat = PreviewFlightAvailableSeats(con, Integer.parseInt(flight_id));
        System.out.println("Please select a seat by entering its number:");
        seat_number = scanner.nextLine();
        if(!validateAge(seat_number))
        {
            System.out.println("Invalid input!");
            return;
        }
        else if(Integer.parseInt(seat_number) > maxSeat)
        {
            System.out.println("Plane doesn't have that many seats!");
            return;
        }
        // now ask to select a class
        PreviewClassInformation(con);
        System.out.println("Please select a class by entering its ID:");
        class_id = scanner.nextLine();
        if(!validateAge(class_id)) // we are using validateAge functions, even though it is not age, because it is the same thing
        {
            System.out.println("Invalid input!");
            return;
        }
        // now ask to select a cost
        System.out.println("Please enter the price of the ticket:");
        cost = scanner.nextLine();
        if(!validateAge(cost))
        {
            System.out.println("Invalid input!");
            return;
        }
        // now we have all the information, lets insert it into the database
        String query = "BEGIN;\n"
                + "INSERT INTO tickets (user_id, seat_number, flight_id, class_id) VALUES (?, ?, ?, ?);\n"
                + "INSERT INTO payments (ticket_id, cost) VALUES (LAST_INSERT_ID(), ?);\n"
                + "COMMIT;";

        java.sql.PreparedStatement stmt = null;
        Savepoint savepoint = null;
        try
        {
            con.setAutoCommit(false);
            savepoint = con.setSavepoint();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, UserID);
            stmt.setInt(2, Integer.parseInt(seat_number));
            stmt.setInt(3, Integer.parseInt(flight_id));
            stmt.setInt(4, Integer.parseInt(class_id));
            stmt.setInt(5, Integer.parseInt(cost));
            int count = stmt.executeUpdate();
            // how to check did transaction succeed?
            System.out.println("Transaction succeeded! You have booked a flight!");
        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
            try {
                con.rollback(savepoint);
            } catch (SQLException e) {
                //  ExceptionCaught(e);
            }
        }
        finally
        {
            try {
                con.setAutoCommit(true);
                //System.out.println("Setting autocommit to true");
            } catch (SQLException e) {
                ExceptionCaught(e);
            }
            checkAndClose(stmt, null);
        }
    }

    public static boolean PreviewUserTickets(Connection con, int UserID)
    {
        String query = "SELECT * FROM tickets WHERE user_id = ?";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, UserID);
            rs = stmt.executeQuery();
            // our result set should be scrollable?????? gonna fix it later. // we already moved the cursor, so we need to move it back
            // lets check if there are any tickets
            if(!rs.next())
            {
                return false;
            }

            System.out.println("Tickets:");
            // it will print nr, departure_time, departure_location, arrival_location, seats_left, aviacompany_id

            System.out.println("ID\tseat number\tflight id\tclass id");
            do
            {
                System.out.println(rs.getInt("nr") + "\t" + rs.getInt("seat_number") + "\t" + rs.getInt("flight_id") + "\t" + rs.getInt("class_id"));
            }while(rs.next());

        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
            return false;
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
        return true;
    }

    public static boolean DeleteAccount(Connection con, int UserID)
    {
        // we need to assure that we really want to delete the account
            System.out.println("Are you sure you want to delete your account? (y/n)");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();
            if(answer.equals("y"))
            {
                String query = "DELETE FROM users WHERE nr = ?";
                java.sql.PreparedStatement stmt = null;
                try
                {
                    stmt = con.prepareStatement(query);
                    stmt.setInt(1, UserID);
                    int count = stmt.executeUpdate();
                    if(count == 1)
                    {
                        System.out.println("Account deleted!");
                        return true;
                    }
                } catch (SQLException sqle) {
                    ExceptionCaught(sqle);
                }
                finally
                {
                    checkAndClose(stmt, null);
                }
            }
            else
            {
                System.out.println("Canceling...");
                return false;
            }
        return false;
    }
    public static boolean validateTicketBelongsToUser(Connection con, int ticket_id, int UserID)
    {
        String query = "SELECT * FROM tickets WHERE user_id = ? AND nr = ?";
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, UserID);
            stmt.setInt(2, ticket_id);
            rs = stmt.executeQuery();
            if(rs.next()) // if there is a result, then the ticket belongs to the user
            {
                return true;
            }
            else
            {
                return false;
            }
        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
        }
        finally
        {
            checkAndClose(stmt, rs);
        }
        return false;
    }
    public static void CancelAFlight(Connection con, int UserID)
    {
        String ticket_id = "";
        // first we will show all the tickets that the user has
        // lets check is it empty PreviewUserTickets(con, UserID);
        if(!PreviewUserTickets(con, UserID))
        {
            // it does not have any tickets
            System.out.println("You do not have any tickets!");
            return;
        }
        // now we will ask to select a ticket
        System.out.println("Please select your flight ticket by entering its ID:");
        Scanner scanner = new Scanner(System.in);
        ticket_id = scanner.nextLine();
        if(!validateAK(ticket_id))
        {
            System.out.println("Invalid input!");
            return;
        }
        // now we have the ticket id, lets delete it
        String query = "DELETE FROM tickets WHERE nr = ?";
        java.sql.PreparedStatement stmt = null;
        try
        {
            // now lets validate if the ticket belongs to the user
            if(!validateTicketBelongsToUser(con, Integer.parseInt(ticket_id), UserID))
            {
                System.out.println("You can't delete a ticket that doesn't belong to you!");
                return;
            }
            stmt = con.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(ticket_id));
            int count = stmt.executeUpdate();
            if(count == 1)
            {
                System.out.println("Ticket deleted! You have successfully cancelled your flight!");
            }
        } catch (SQLException sqle) {
            ExceptionCaught(sqle);
        }
        finally
        {
            checkAndClose(stmt, null);
        }
    }
    public static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;
        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42Y55"))
            return true;
        return false;
    }
    public static void ExceptionCaught(SQLException sqle)
    {
        System.out.println("Couldn't execute query!");
        for(Throwable e: sqle){
            if(e instanceof SQLException){
            if(ignoreSQLException(((SQLException)e).getSQLState()) == false) {
                int errorCode = ((SQLException)e).getErrorCode();
                String sqlState = ((SQLException)e).getSQLState();
                System.out.println("SQL State: " + sqlState);
                System.out.println("Error Code: " + errorCode);
                // if error code is 0, then there is no data in the table
                if (errorCode == 0) {
                    System.out.println("Execution was successful");
                } else if (errorCode == 100) {
                    System.out.println("no data was found");
                } else if (errorCode > 0 && sqle.getErrorCode() != 100) {
                    System.out.println("Execution was successful with warning");
                } else {
                    System.out.println("Execution was not successful");
                }

                System.out.println("Message: " + sqle.getMessage());
                Throwable t = sqle.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
            }
        }

    }
    public static void MainMenu(Connection con, int AccountID)
    {
        // there should be a menu with all the available options, we will use a switch statement
        int option = 0;
        System.out.println("Welcome to the main menu!");
        do
        {
            System.out.println("Enter your option:");
            System.out.println("0. Logout");
            System.out.println("1. Change account details");
            System.out.println("2. Preview available flight schedules");
            System.out.println("3. Preview class information");
            System.out.println("4. Book a flight");
            System.out.println("5. Cancel a flight");
            System.out.println("6. Delete account");
            Scanner sc = new Scanner(System.in);
            String input = sc.nextLine();
            try
            {
                option = Integer.parseInt(input);
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("Invalid input!");
                continue;
            }

            switch (option)
            {
                case 0:
                    System.out.println("Exiting...");
                    break;
                case 1:
                    ChangeAccountDetails(con, AccountID);
                    break;
                case 2:
                    PreviewAvailableFlightSchedules(con);
                    break;
                case 3:
                    PreviewClassInformation(con);
                    break;
                case 4:
                    BookAFlight(con, AccountID);
                    break;
                case 5:
                    CancelAFlight(con, AccountID);
                    break;
                case 6:
                    if(DeleteAccount(con, AccountID))
                          return;
                default:
                    System.out.println("Invalid option!");
                    break;
            }
        } while (option != 0);
    }
    public static void main(String[] args)
    {
        Connection con = getConnection();
        if(con!= null)
        {
            // if connection is successful, lets execute queries (we will insert, update, delete, select, use transactions)
            //PrintUsersData(con);
            //CreateNewUser(con, 4454, "Kami3l", "Brzeczyszczkiewicz", 20);// CON AK NAME SURNAME AGE
            System.out.println("Welcome to the Ryanair flight booking system!");
            int option = 0;
            do
            {
                System.out.println("Enter your option:");
                System.out.println("0. Exit");
                System.out.println("1. Login with existing account");
                System.out.println("2. Create a new account");
                // now try to read a line from the console
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                try
                {
                    option = Integer.parseInt(input);
                }
                catch (NumberFormatException nfe)
                {
                    System.out.println("Invalid input!");
                    continue;
                }
                switch (option)
                {
                    case 0:
                        System.out.println("Goodbye!");
                        break;
                    case 1:
                        System.out.println("Logging in...");
                        // as database does not have a password, we will use an ID of the user to login
                        int AccountID = Login(con);
                        if(AccountID != 0)
                        {
                            System.out.println("Logged in successfully!");
                            MainMenu(con, AccountID);
                        }
                        break;
                    case 2:
                        System.out.println("Creating a new account...");
                        int generatedAccountID = CreateNewAccount(con);
                        if(generatedAccountID != 0)
                        {
                            System.out.println("New account created with id: " + generatedAccountID);
                            System.out.println("Redirecting to the main menu...");
                            MainMenu(con, generatedAccountID);
                        }
                        else
                        {
                            System.out.println("Couldn't create a new user!");
                        }
                        break;
                    default:
                        System.out.println("Invalid option!");
                        break;
                }


            } while (option != 0);
            
        }
        if(con!= null)
        {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Couldn't close connection!");
                e.printStackTrace();
            }
        }
    }
}
