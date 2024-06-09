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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

public class ListOfRecords implements ActionListener{
    private JFrame f;
    private JPanel p1, p2, p3;
    private JTextArea recordList;

    private JButton addRec,updRec,removeRec,logout;
    String formatOutPutText = String.format("%30s%40s%30s%n", "Email", "Password", "UserRole");
    
    List<String> recordHolder;
    private LoginScreen loginScreen;

    public ListOfRecords(List<String> recordHolder,LoginScreen loginScreen) {
        this.loginScreen = loginScreen;
        f = new JFrame("List of Record");
        p1 = new JPanel();
        p2 = new JPanel();
        p3 = new JPanel();

        recordList = new JTextArea(10, 40);

        recordList.setText(formatOutPutText);
        recordList.setEditable(false);
        recordList.setAlignmentX(Component.CENTER_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(recordList);

        addRec = new JButton("Add Record");
        updRec = new JButton("Update Record");
        removeRec = new JButton("Remove Record");
        logout = new JButton("Logout");

        p1.add(scrollPane);
        p2.add(addRec);
        p2.add(updRec);
        p2.add(removeRec);
        p3.add(logout);

        f.add(p1, BorderLayout.NORTH);
        f.add(p2,BorderLayout.CENTER);
        f.add(p3, BorderLayout.SOUTH);
        
        addRec.addActionListener(this);
        updRec.addActionListener(this);
        removeRec.addActionListener(this);
        logout.addActionListener(this);

        f.setSize(550, 320);
        f.setLocation(620, 300);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        OutputRecords();
    }
    
    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();
        if(source.equals(addRec)){
            AddRecord window = new AddRecord(this.loginScreen);
            f.dispose();
        }
        else if(source.equals(removeRec)){
            RemoveRecord window = new RemoveRecord(this.loginScreen);
            f.dispose();
        }
        else if(source.equals(updRec)){
            UpdateRecord window = new UpdateRecord(this.loginScreen);
            f.dispose();
        }
        else if(source.equals(logout)){
            loginScreen.showLoginScreen();
            f.dispose();
        }
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

            // Iterate through the result set and append data to the JTextArea
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