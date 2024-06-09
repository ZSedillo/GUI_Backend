package controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Zandro
 */

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class RemoveRecord implements ActionListener {
    private JFrame f;
    private JPanel p1, p2, p3;
    private JLabel l1, searchLabel;
    private JTextArea recordList;
    private JComboBox<String> users;
    private JTextField searchField;

    private JButton back, remove, search, refresh;
    String formatOutPutText = String.format("%30s%40s%30s%n", "Email", "Password", "UserRole");

    private LoginScreen loginScreen;

    public RemoveRecord(LoginScreen loginScreen) {
        this.loginScreen = loginScreen;
        f = new JFrame("Removing Record");
        p1 = new JPanel();
        p2 = new JPanel();
        p3 = new JPanel();

        l1 = new JLabel("Select User to delete by their email: ");

        String[] recordListSql = getEmailsFromDatabase();
        users = new JComboBox<>(recordListSql);

        recordList = new JTextArea(10, 40);

        recordList.setText(formatOutPutText);
        recordList.setEditable(false);
        recordList.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(recordList);

        back = new JButton("Back");
        remove = new JButton("Remove");
        searchLabel = new JLabel("Search User by Email: ");
        searchField = new JTextField(20);
        search = new JButton("Search");
        refresh = new JButton("Refresh");

        p1.add(scrollPane);
        p2.add(l1);
        p2.add(users);
        p2.add(searchLabel);
        p2.add(searchField);
        p2.add(search);
        p2.add(refresh);
        p3.add(back);
        p3.add(remove);
        f.setLayout(new BorderLayout());

        f.add(p1, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(0, 1));
        centerPanel.add(p2);

        f.add(centerPanel, BorderLayout.CENTER);

        f.add(p3, BorderLayout.SOUTH);

        back.addActionListener(this);
        remove.addActionListener(this);
        search.addActionListener(this);
        refresh.addActionListener(this); 
        
        f.setSize(550, 350);
        f.setLocation(620, 300);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        OutputRecords();
    }

    private String[] getEmailsFromDatabase() {
        List<String> emails = new ArrayList<>();
        try {
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String password = "app";
            Connection con = DriverManager.getConnection(url, username, password);

            Statement stmt = con.createStatement();
            String query = "SELECT Email FROM USERS ORDER BY Email";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String email = rs.getString("Email");
                emails.add(email);
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return emails.toArray(new String[0]);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(remove)) {
            String selectedEmail = (String) users.getSelectedItem();

            if (selectedEmail != null && !selectedEmail.isEmpty()) {
                int confirmation = JOptionPane.showConfirmDialog(
                        f,
                        "Are you sure you want to remove the user with email: " + selectedEmail + "?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmation == JOptionPane.YES_OPTION) {
                    removeUser(selectedEmail);
                    
                    users.setSelectedIndex(0);
                    recordList.setText(formatOutPutText);
                    OutputRecords();
                }
            } else {
                JOptionPane.showMessageDialog(f, "Please select a user to remove.", "Error Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (source.equals(back)) {
            List<String> recordHolder = new ArrayList<>();
            ListOfRecords recordWindow = new ListOfRecords(recordHolder, this.loginScreen);
            f.dispose();
        } else if (source.equals(search)) {
        String searchEmail = searchField.getText();
        if (searchEmail != null && !searchEmail.isEmpty()) {
            recordList.setText(formatOutPutText); // Clear previous content
            displayUserCredentialsInTextArea(searchEmail);
        } else {
            JOptionPane.showMessageDialog(f, "Please enter an email to search.", "Error Message", JOptionPane.ERROR_MESSAGE);
            }
        }  else if (source.equals(refresh)) {
            users.setModel(new DefaultComboBoxModel<>(getEmailsFromDatabase()));
            recordList.setText(formatOutPutText);
            searchField.setText("");
            OutputRecords();
        }
    }
    
    private void displayUserCredentialsInTextArea(String partialEmail) {
        try {
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String password = "app";
            Connection con = DriverManager.getConnection(url, username, password);

            String query = "SELECT * FROM USERS WHERE Email LIKE ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, "%" + partialEmail + "%"); // Use '%' for partial matching
                ResultSet rs = pstmt.executeQuery();

                boolean found = false;
                StringBuilder outputCredentials = new StringBuilder(formatOutPutText);

                while (rs.next()) {
                    String email = rs.getString("Email");
                    String passwordFromDB = rs.getString("Password");
                    String userRole = rs.getString("UserRole");

                    int adjust = 0;
                    if (userRole.equals("GUEST")) {
                        adjust = 2;
                    }

                    String record = String.format("%25s%" + (30 + adjust) + "s%30s%n", email, passwordFromDB, userRole);
                    outputCredentials.append(record);
                    found = true;
                }

                if (found) {
                    recordList.setText(outputCredentials.toString());
                    updateComboBox(partialEmail);
                } else {
                    recordList.setText(formatOutPutText + "No matching users found.");
                }
            }

            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateComboBox(String partialEmail) {
        String[] filteredEmails = getEmailsFromDatabaseWithFilter(partialEmail);
        users.setModel(new DefaultComboBoxModel<>(filteredEmails));
    }

    private String[] getEmailsFromDatabaseWithFilter(String partialEmail) {
        List<String> emails = new ArrayList<>();
        try {
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String password = "app";
            Connection con = DriverManager.getConnection(url, username, password);

            String query = "SELECT Email FROM USERS WHERE Email LIKE ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, "%" + partialEmail + "%"); // Use '%' for partial matching
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String email = rs.getString("Email");
                    emails.add(email);
                }
            }

            con.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return emails.toArray(new String[0]);
    }



    private void OutputRecords() {
    try {
        String driver = "org.apache.derby.jdbc.ClientDriver";
        Class.forName(driver);

        String url = "jdbc:derby://localhost:1527/LoginDB";
        String username = "app";
        String password = "app";
        Connection con = DriverManager.getConnection(url, username, password);

        Statement stmt = con.createStatement();
        String query = "SELECT * FROM USERS ORDER BY Email";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            String email = rs.getString("Email");
            String passwordFromDB = rs.getString("Password");
            String userRole = rs.getString("UserRole");
            
            int adjust = 0;
            if(userRole.equals("GUEST")){
                adjust = 2;
            }
            String outputRecord = String.format("%25s%" + (30 + adjust) + "s%30s%n", email, passwordFromDB, userRole);
            recordList.append(outputRecord);
        }

        rs.close();
        stmt.close();
        con.close();
    } catch (ClassNotFoundException | SQLException e) {
        e.printStackTrace();
    }
    }

        private void removeUser(String email) {
        try {
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String dbPassword = "app";
            Connection con = DriverManager.getConnection(url, username, dbPassword);

            if (!isAdmin(email)) {
                String query = "DELETE FROM USERS WHERE Email = ?";
                try (PreparedStatement pstmt = con.prepareStatement(query)) {
                    pstmt.setString(1, email);

                    pstmt.executeUpdate();
                }
            } else {
                JOptionPane.showMessageDialog(f, "Cannot remove an admin user.", "Error Message", JOptionPane.ERROR_MESSAGE);
            }

            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isAdmin(String email) {
        try {
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String password = "app";
            Connection con = DriverManager.getConnection(url, username, password);

            String query = "SELECT UserRole FROM USERS WHERE Email = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String userRole = rs.getString("UserRole");
                    return "ADMIN".equals(userRole);
                }
            }

            con.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}