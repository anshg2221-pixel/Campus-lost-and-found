import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class Admin {

    public Admin() {

        Connection con = DatabaseUtility.getConnection();

        JFrame frame = new JFrame("Campus Lost and Found — Admin Panel");
        frame.setSize(950, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] cols = {
            "ID","Item","Category","Status",
            "Reported Desc","Claim Desc",
            "Reporter","Claimed By","Approved"
        };

        DefaultTableModel model = new DefaultTableModel(cols, 0){
            public boolean isCellEditable(int r,int c){ return false; }
        };

        JTable table = new JTable(model);

        // hide ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scroll = new JScrollPane(table);

        JButton approve = new JButton("Approve");
        JButton reject  = new JButton("Reject");
        JButton refresh = new JButton("Refresh");
        JButton logout  = new JButton("Logout");

        JPanel btnPanel = new JPanel();
        btnPanel.add(approve);
        btnPanel.add(reject);
        btnPanel.add(refresh);
        btnPanel.add(logout);

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        // LOAD DATA
        Runnable load = () -> {
            model.setRowCount(0);
            try {
                String q =
                    "SELECT i.id, i.item_name, i.category, i.status, " +
                    "i.reported_desc, i.claim_desc, " +
                    "u1.username, u2.username, i.approved " +
                    "FROM Items i " +
                    "LEFT JOIN Users u1 ON i.reporter_id=u1.id " +
                    "LEFT JOIN Users u2 ON i.claimed_by=u2.id";

                PreparedStatement ps = con.prepareStatement(q);
                ResultSet rs = ps.executeQuery();

                while(rs.next()){
                    model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getString(8)==null?"—":rs.getString(8),
                        rs.getInt(9)==1?"Yes":"No"
                    });
                }

            } catch(Exception ex){
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        };

        load.run();

        // APPROVE
        approve.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row==-1) return;

            int id = (int)model.getValueAt(row,0);

            try{
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE Items SET approved=1, status='Claimed' WHERE id=?"
                );
                ps.setInt(1,id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(null,"Approved");
                load.run();

            }catch(Exception ex){
                JOptionPane.showMessageDialog(null,ex.getMessage());
            }
        });

        // REJECT
        reject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row==-1) return;

            int id = (int)model.getValueAt(row,0);

            try{
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE Items SET claimed_by=NULL, claim_desc=NULL, approved=0 WHERE id=?"
                );
                ps.setInt(1,id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(null,"Rejected");
                load.run();

            }catch(Exception ex){
                JOptionPane.showMessageDialog(null,ex.getMessage());
            }
        });
        
        logout.addActionListener(e -> {
            frame.dispose();
            Login.main(null);
        });

        refresh.addActionListener(e -> load.run());

        frame.setVisible(true);
    }
}