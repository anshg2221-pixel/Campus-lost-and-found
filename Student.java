import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;

public class Student {

    public Student(String current_student, int user_id) {

        Connection con = DatabaseUtility.getConnection();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 520);
        frame.setTitle("Campus Lost and Found  —  " + current_student);

        JTabbedPane pane = new JTabbedPane();

        JPanel reportPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        reportPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel itemLabel = new JLabel("Item Name :");
        JTextField itemInput = new JTextField();

        JLabel categoryLabel = new JLabel("Category :");
        String[] categories = {"Electronic", "Academic", "Umbrella", "Skincare", "Other"};
        JComboBox<String> categoryInput = new JComboBox<>(categories);

        JLabel statusLabel = new JLabel("Status :");
        String[] statuses = {"Lost", "Found"};
        JComboBox<String> statusInput = new JComboBox<>(statuses);

        JLabel descLabel = new JLabel("Description (for admin only) :");
        JTextField descInput = new JTextField();

        JLabel blank = new JLabel();
        JButton submitButton = new JButton("Submit");

        reportPanel.add(itemLabel);     reportPanel.add(itemInput);
        reportPanel.add(categoryLabel); reportPanel.add(categoryInput);
        reportPanel.add(statusLabel);   reportPanel.add(statusInput);
        reportPanel.add(descLabel);     reportPanel.add(descInput);
        reportPanel.add(blank);         reportPanel.add(submitButton);

        JPanel feedPanel = new JPanel(new BorderLayout(8, 8));
        feedPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] columns = {"ID", "Item Name", "Category", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);

        JButton claimButton = new JButton("Claim Selected Item");
        JButton logoutButton = new JButton("Logout");

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.add(claimButton);
        bottomBar.add(logoutButton);

        feedPanel.add(scrollPane, BorderLayout.CENTER);
        feedPanel.add(bottomBar, BorderLayout.SOUTH);


        Runnable loadFeed = () -> {
            model.setRowCount(0);
            try {
                String q = "SELECT id, item_name, category, status FROM Items";
                PreparedStatement ps = con.prepareStatement(q);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("status")
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error loading feed: " + ex.getMessage());
            }
        };
        loadFeed.run();

        submitButton.addActionListener(e -> {
            try {
                String itemName = itemInput.getText().trim();
                String category = (String) categoryInput.getSelectedItem();
                String status   = (String) statusInput.getSelectedItem();
                String desc     = descInput.getText().trim();

                if (itemName.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Enter item name.");
                    return;
                }

                String q = "INSERT INTO Items(item_name, category, status, reporter_id, reported_desc) VALUES(?,?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(q);

                ps.setString(1, itemName);
                ps.setString(2, category);
                ps.setString(3, status);
                ps.setInt(4, user_id);
                ps.setString(5, desc);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(null, "Item reported!");

                itemInput.setText("");
                descInput.setText("");

                loadFeed.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });


        claimButton.addActionListener(e -> {

            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Select an item first.");
                return;
            }

            int itemId    = (int) model.getValueAt(selectedRow, 0);
            String status = (String) model.getValueAt(selectedRow, 3);

            if (!status.equalsIgnoreCase("Found")) {
                JOptionPane.showMessageDialog(null, "Only 'Found' items can be claimed.");
                return;
            }


            String claimDesc = JOptionPane.showInputDialog(null,
                    "Describe the item to prove it is yours :\n" +
                    "(e.g. colour, brand, what is inside, any markings)\n" +
                    "Admin will match this with the reporter's description.",
                    "Prove Ownership",
                    JOptionPane.QUESTION_MESSAGE);

            if (claimDesc == null || claimDesc.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "You must describe the item to submit a claim.");
                return;
            }

            try {
                String q = "UPDATE Items SET claimed_by = ?, claim_desc = ? WHERE id = ? AND claimed_by IS NULL";
                PreparedStatement ps = con.prepareStatement(q);

                ps.setInt(1, user_id);
                ps.setString(2, claimDesc.trim());
                ps.setInt(3, itemId);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(null, "Claim sent to admin for verification.");
                    loadFeed.run();
                } else {
                    JOptionPane.showMessageDialog(null, "Already claimed by someone else.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });

        logoutButton.addActionListener(e -> {
            frame.dispose();
            Login.main(null);
        });

        pane.addTab("Feed", feedPanel);
        pane.addTab("Report Item", reportPanel);

        frame.add(pane);
        frame.setVisible(true);
    }
}