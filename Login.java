import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;

public class Login {
    public static void main(String[] args) {

        Connection con = DatabaseUtility.getConnection();
        if (con != null) {
            System.out.println("Connection Successful");
        }

        JFrame frame = new JFrame("Campus Lost and Found  —  Login");
        frame.setSize(380, 280);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        // ── LOGIN TAB ──────────────────────────────────────────
        JPanel loginPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Username :");
        JTextField usernameInput = new JTextField();

        JLabel passwordLabel = new JLabel("Password :");
        JPasswordField passwordInput = new JPasswordField();

        JLabel blank = new JLabel();
        JButton loginButton = new JButton("Login");

        loginPanel.add(usernameLabel);  loginPanel.add(usernameInput);
        loginPanel.add(passwordLabel);  loginPanel.add(passwordInput);
        loginPanel.add(blank);          loginPanel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
                    PreparedStatement statement = con.prepareStatement(query);
                    String user = usernameInput.getText().trim();
                    String pass = new String(passwordInput.getPassword());
                    statement.setString(1, user);
                    statement.setString(2, pass);
                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        String role = rs.getString("role");
                        frame.dispose();

                        if (role.equals("student")) {
                            int userId = rs.getInt("id");
                            new Student(user, userId);
                        } else if (role.equals("admin")) {
                            new Admin();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Login Failed. Check your credentials.");
                    }
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(null, "Connection error: " + exception.getMessage());
                }
            }
        });

        // ── REGISTER TAB ───────────────────────────────────────
        JPanel registerPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        registerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel regUsernameLabel = new JLabel("Username :");
        JTextField regUsernameInput = new JTextField();

        JLabel regPasswordLabel = new JLabel("Password :");
        JPasswordField regPasswordInput = new JPasswordField();

        JLabel regConfirmLabel = new JLabel("Confirm Password :");
        JPasswordField regConfirmInput = new JPasswordField();

        JLabel regBlank = new JLabel();
        JButton registerButton = new JButton("Register");

        registerPanel.add(regUsernameLabel);  registerPanel.add(regUsernameInput);
        registerPanel.add(regPasswordLabel);  registerPanel.add(regPasswordInput);
        registerPanel.add(regConfirmLabel);   registerPanel.add(regConfirmInput);
        registerPanel.add(regBlank);          registerPanel.add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = regUsernameInput.getText().trim();
                String password = new String(regPasswordInput.getPassword());
                String confirm  = new String(regConfirmInput.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Username and password cannot be empty.");
                    return;
                }

                if (!password.equals(confirm)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match.");
                    return;
                }

                try {
                    // Check for duplicate username
                    PreparedStatement check = con.prepareStatement(
                        "SELECT id FROM Users WHERE username = ?"
                    );
                    check.setString(1, username);
                    ResultSet rs = check.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(null, "Username already taken. Choose another.");
                        return;
                    }

                    // Insert new student
                    PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO Users (username, password, role) VALUES (?, ?, 'student')"
                    );
                    insert.setString(1, username);
                    insert.setString(2, password);
                    insert.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Registration successful! You can now log in.");

                    // Clear fields and switch to Login tab
                    regUsernameInput.setText("");
                    regPasswordInput.setText("");
                    regConfirmInput.setText("");
                    tabs.setSelectedIndex(0);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        });

        tabs.addTab("Login", loginPanel);
        tabs.addTab("Register", registerPanel);

        frame.add(tabs);
        frame.setVisible(true);
    }
}