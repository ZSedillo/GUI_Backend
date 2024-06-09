package MP1;

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

public class UpdateRecord implements ActionListener{
    private JFrame f;
    private JPanel p1, p2, p3, p4, p5, p6, p7;
    private JLabel l1,l2,l3,l4, searchLabel;
    private JTextField tf1,tf2;
    private JTextArea recordList;
    private JComboBox<String> users,userRoleOption;
    private JTextField searchField;

    private JButton back, update, search, refresh;
    String formatOutPutText = String.format("%30s%40s%30s%n", "Email", "Password", "UserRole");
    
    private LoginScreen loginScreen;

    public UpdateRecord(LoginScreen loginScreen) {
        this.loginScreen = loginScreen;
        f = new JFrame("Updating Record");
        p1 = new JPanel();
        p2 = new JPanel();
        p3 = new JPanel();
        p4 = new JPanel();
        p5 = new JPanel();
        p6 = new JPanel();
        p7 = new JPanel();
        
        l1 = new JLabel("Select User to update by their email: ");
        l2 = new JLabel("Password : ");
        l3 = new JLabel("Confirm Password : ");
        l4 = new JLabel("Role : ");
        
        searchLabel = new JLabel("Search User by Email: ");
        searchField = new JTextField(20);
        
        
        tf1 = new JTextField("",15);
        tf2 = new JTextField("",15);
         
       
        String[] recordListSql = getEmailsFromDatabase();
        users = new JComboBox<>(recordListSql);
        
        String[] availableRoles = {"SELECT ROLE","ADMIN","GUEST"};
        userRoleOption = new JComboBox<>(availableRoles);

        recordList = new JTextArea(10, 40);

        recordList.setText(formatOutPutText);
        recordList.setEditable(false);
        recordList.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(recordList);

        back = new JButton("Back");
        update = new JButton("Update");
        search = new JButton("Search");
        refresh = new JButton("Refresh");
        
        p1.add(scrollPane);
        p2.add(l1);
        p2.add(users);
        p3.add(searchLabel);
        p3.add(searchField);
        p3.add(search);
        p3.add(refresh);
        p4.add(l2);
        p4.add(tf1);
        p5.add(l3);
        p5.add(tf2);
        p6.add(l4);
        p6.add(userRoleOption);
        p7.add(back);
        p7.add(update);

        f.setLayout(new BorderLayout());

        f.add(p1, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(0, 1));
        centerPanel.add(p2);
        centerPanel.add(p3);
        centerPanel.add(p4);
        centerPanel.add(p5);
        centerPanel.add(p6);

        f.add(centerPanel, BorderLayout.CENTER);

        f.add(p7, BorderLayout.SOUTH);

        back.addActionListener(this);
        update.addActionListener(this);
        search.addActionListener(this);
        refresh.addActionListener(this); 

        f.setSize(590, 450);
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
        String password = tf1.getText().trim();
        String confirmPassword = tf2.getText().trim();
        String selectedEmail = (String) users.getSelectedItem();
        String selectedRole = (String) userRoleOption.getSelectedItem();
        Object source = e.getSource();
        if (source.equals(update)) {
            if (password.isEmpty() && selectedRole.equals("SELECT ROLE")) {
                JOptionPane.showMessageDialog(f, "There are no changes made!.", "Information", JOptionPane.INFORMATION_MESSAGE);
                users.setSelectedIndex(0);
            }
            else if(password.equals(confirmPassword)){
                updateRecord(selectedEmail, password, selectedRole);

                tf1.setText("");
                tf2.setText("");
                searchField.setText("");
                users.setSelectedIndex(0);
                userRoleOption.setSelectedIndex(0);

                //JOptionPane.showMessageDialog(f, "Record Updated!", "Information", JOptionPane.INFORMATION_MESSAGE);
                recordList.setText(formatOutPutText);
                OutputRecords();
            }
            else if(!password.equals(confirmPassword)){
                JOptionPane.showMessageDialog(f, "Password is not the same. Please try again!", "Error Message", JOptionPane.ERROR_MESSAGE);
            }
        } else if (source.equals(back)) {
            List<String> recordHolder = new ArrayList<>();
            ListOfRecords recordWindow = new ListOfRecords(recordHolder, this.loginScreen);
            f.dispose();
        }  else if (source.equals(search)) {
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
    
    private void updateRecord(String email, String password, String userRole) {
        try {
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String dbPassword = "app";
            Connection con = DriverManager.getConnection(url, username, dbPassword);

            if (isAdmin(email) && userRole.equals("GUEST")) {
                JOptionPane.showMessageDialog(f, "Cannot demote an admin to guest!", "Error", JOptionPane.ERROR_MESSAGE);
                con.close();
                return;
            }

            String query = "UPDATE USERS SET Password = COALESCE(?, Password), UserRole = COALESCE(?, UserRole) WHERE Email = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                if (!password.isEmpty()) {
                    pstmt.setString(1, password);
                } else {
                    pstmt.setNull(1, Types.VARCHAR);
                }

                if (!userRole.equals("SELECT ROLE")) {
                    pstmt.setString(2, userRole);
                } else {
                    pstmt.setNull(2, Types.VARCHAR);
                }

                pstmt.setString(3, email);

                pstmt.executeUpdate();
            }
            
            JOptionPane.showMessageDialog(f, "Record Updated!", "Information", JOptionPane.INFORMATION_MESSAGE);

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
                    return userRole.equals("ADMIN");
                }

                rs.close();
            }

            con.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return false;
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
}