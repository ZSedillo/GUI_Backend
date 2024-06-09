/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MP1;

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

public class LoginScreen implements ActionListener {
    private JFrame f;
    private JPanel p1,p2,p3;
    private JLabel l1, l2;

    private JTextField tf1,tf2;
    private JButton loginButton;

    private int inputCounter = 0;

    private Map<String,String> hashMap= new HashMap<>();
    
    String userNameInput="",userPasswordInput = "";

    public void showLoginScreen() {
        f.setVisible(true);
        tf1.setText("");
        tf2.setText("");
    }

    public LoginScreen()
    {
        f = new JFrame("Login Screen");
        p1 = new JPanel();
        p2 = new JPanel();
        p3 = new JPanel();

        l1 = new JLabel("Username : ");
        l2 = new JLabel("Password : ");

        tf1 = new JTextField("",15);
        tf2 = new JTextField("",15);

        loginButton = new JButton("Login");
    }

    public void startApp()
    {
        p1.add(l1);
        p1.add(tf1);

        p2.add(l2);
        p2.add(tf2);

        p3.add(loginButton);

        f.setLayout(new GridLayout(3,1));

        f.add(p1);
        f.add(p2);
        f.add(p3);

        f.setSize(350,200);
        f.setLocation(620,300);
        f.setVisible(true);

        loginButton.addActionListener(this);

        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
            try{
            String driver = "org.apache.derby.jdbc.ClientDriver";
            Class.forName(driver);
            System.out.println("Loaded Driver: " + driver);

            String url = "jdbc:derby://localhost:1527/LoginDB";
            String username = "app";
            String password = "app";
            Connection con = DriverManager.getConnection(url, username, password);
        
            Statement stmt = con.createStatement();

        userNameInput = tf1.getText();
        userPasswordInput = tf2.getText();
        
        
        String queryUserEmail = "SELECT * FROM USERS WHERE Email = ?";
        PreparedStatement preparedStatement = con.prepareStatement(queryUserEmail);
        preparedStatement.setString(1, userNameInput);
        ResultSet rs1 = preparedStatement.executeQuery();

        
        boolean userAccess = false;
        
        String checkUserEmail,checkUserPassword,checkUserRole;
        while(rs1.next()){
            checkUserEmail = rs1.getString("Email"); 
            checkUserPassword = rs1.getString("Password");
            checkUserRole = rs1.getString("UserRole");
            
            if(checkUserEmail.equals(userNameInput) && checkUserPassword.equals(userPasswordInput)){
                userAccess = true;
                if(checkUserRole.equals("ADMIN")){
                    List<String> recordHolder = new ArrayList<>();
                    ListOfRecords recordWindow = new ListOfRecords(recordHolder,this);
                    f.dispose();
                }
                else if(checkUserRole.equals("GUEST")){
                    ListOfRecordsRestricted recordWindow = new ListOfRecordsRestricted(this);
                    f.dispose();
                }
            }
        }

            if (inputCounter != 3) {
                if (!userAccess) {
                    JOptionPane.showMessageDialog(f, "Incorrect Username / Password", "Error Screen", JOptionPane.ERROR_MESSAGE);
                    inputCounter++;
                }
            }
            if (inputCounter == 3){
                JOptionPane.showMessageDialog(f, "Sorry, you have reached the limit of 3 tries, goodbye!", "Error Screen", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            stmt.close();
            con.close();
        } catch (SQLException | ClassNotFoundException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LoginScreen loginApp = new LoginScreen();
        loginApp.startApp();
    }
}
