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

public class AddRecord implements ActionListener{
    private JFrame f;
    private JPanel p1, p2, p3, p4, p5, p6;
    private JTextArea recordList;
    private JLabel l1, l2, l3, l4;
    private JTextField tf1,tf2,tf3;
    private JComboBox<String> role;

    private JButton back, add;
    String formatOutPutText = String.format("%30s%40s%30s%n", "Email", "Password", "UserRole");
    
    private LoginScreen loginScreen;

    public AddRecord(LoginScreen loginScreen) {
        this.loginScreen = loginScreen;
        f = new JFrame("Adding Record");
        p1 = new JPanel();
        p2 = new JPanel();
        p3 = new JPanel();
        p4 = new JPanel();
        p5 = new JPanel();
        p6 = new JPanel();
        
        l1 = new JLabel("Email : ");
        l2 = new JLabel("Password : ");
        l3 = new JLabel("Confirm Password : ");
        l4 = new JLabel("Choose Role: ");
        
        String[] roleList = {"Select UserRole","ADMIN","GUEST"};
        role = new JComboBox<>(roleList);
        
        tf1 = new JTextField("",15);
        tf2 = new JTextField("",15);
        tf3 = new JTextField("",15);

        recordList = new JTextArea(10, 40);

        recordList.setText(formatOutPutText);
        recordList.setEditable(false);
        recordList.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(recordList);

        back = new JButton("Back");
        add = new JButton("Add");

        p1.add(scrollPane); 
        p2.add(l1);
        p2.add(tf1);
        p3.add(l2);
        p3.add(tf2);
        p4.add(l3);
        p4.add(tf3);
        p5.add(l4);
        p5.add(role);
        p6.add(back);
        p6.add(add);

        f.setLayout(new BorderLayout());

        f.add(p1, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(0, 1));
        centerPanel.add(p2);
        centerPanel.add(p3);
        centerPanel.add(p4);
        centerPanel.add(p5);

        f.add(centerPanel, BorderLayout.CENTER);

        f.add(p6, BorderLayout.SOUTH);

        back.addActionListener(this);
        add.addActionListener(this);

        f.setSize(550, 400);
        f.setLocation(620, 300);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        OutputRecords();
    }
    
    public void actionPerformed(ActionEvent e){
        String email = tf1.getText();
        String password = tf2.getText();
        String confirmPassword = tf3.getText();
        String selectedRole = (String) role.getSelectedItem();
        Object source = e.getSource();
        if(source.equals(add)){
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please input an email.", "Error Message", JOptionPane.ERROR_MESSAGE);
            } 
            else if (!email.matches(".+@(gmail\\.com|email\\.com|yahoo\\.com)$")) {
             JOptionPane.showMessageDialog(f, "Please input a valid email.", "Error Message", JOptionPane.ERROR_MESSAGE);
            }
            else if (password.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please input a password.", "Error Message", JOptionPane.ERROR_MESSAGE);
            } 
            else if(!password.equals(confirmPassword)){
                JOptionPane.showMessageDialog(f, "Password is not the same. Please try again!", "Error Message", JOptionPane.ERROR_MESSAGE);
            }
            else if (selectedRole.equals("Select UserRole")) {
                JOptionPane.showMessageDialog(f, "Please select a user role.", "Error Message", JOptionPane.ERROR_MESSAGE);
            } 
            else {
                insertRecord(email, password, selectedRole);

            tf1.setText("");
            tf2.setText("");
            tf3.setText("");
            role.setSelectedIndex(0);

            OutputRecords();
            }
        }
        else if(source.equals(back)){
            List<String> recordHolder = new ArrayList<>();
            ListOfRecords recordWindow = new ListOfRecords(recordHolder,this.loginScreen);
            f.dispose();
        }
    }
    
    //@TODO Add Confirm Password
    private void insertRecord(String email, String password, String userRole) {
        try {
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String dbPassword = "app";
            Connection con = DriverManager.getConnection(url, username, dbPassword);
            
            String userNameInput = tf1.getText();
            String queryUserEmail = "SELECT Email FROM USERS WHERE Email = ?";
            PreparedStatement preparedStatement = con.prepareStatement(queryUserEmail);
            preparedStatement.setString(1, userNameInput);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                // Email exists in the database
                String userEmail = rs.getString("Email");
                JOptionPane.showMessageDialog(f, "Email already exists in the database:", "Error Message", JOptionPane.ERROR_MESSAGE);
            } else {
                String query = "INSERT INTO USERS (Email, Password, UserRole) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = con.prepareStatement(query)) {
                    pstmt.setString(1, email);
                    pstmt.setString(2, password);
                    pstmt.setString(3, userRole);

                    pstmt.executeUpdate(); //executeQuery();
                    JOptionPane.showMessageDialog(f, "Record Added!.");
                }
            }
           

            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void OutputRecords() {
    try {
        recordList.setText(formatOutPutText);
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