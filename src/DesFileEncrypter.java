
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DesFileEncrypter extends javax.swing.JFrame {

    private static final String netbeansPath = "C:\\Users\\hoang\\Documents\\DES-Cipher";
    final JFileChooser fChooser;
    Des des;

    public DesFileEncrypter() {
        initComponents();
        fChooser = new JFileChooser(netbeansPath);
        des = new Des();
    }

    //Open file to enc, encrypts and save encrypted file
    private void encryptFile() {
        File file = openFile();
        if (file != null) {
            byte[] fileByte = FileR.readFile(file);

            String key = keyTextField1.getText();
            byte[] encFileByte = des.encryptFile(fileByte, key);
            displayGuiMessage("File " + file.getName() + " encrypted.", "Done", JOptionPane.INFORMATION_MESSAGE);
            File encFile = saveFile();
            Path path = encFile.toPath();
            try {
                Files.write(path, encFileByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Open file to dec, decrypts and save decrypted file
    private void decryptFile() {
        File file = openFile();
        if (file != null) {
            byte[] fileByte = FileR.readFile(file);

            String key = keyTextField1.getText();
            byte[] decFileByte = des.decryptFile(fileByte, key);
            displayGuiMessage("File " + file.getName() + " decrypted.", "Done", JOptionPane.INFORMATION_MESSAGE);
            File encFile = saveFile();
            Path path = encFile.toPath();
            System.out.println(decFileByte);
            try {
                Files.write(path, decFileByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        keyTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        genKeyButton3 = new javax.swing.JButton();

        jLabel1.setText("jLabel1");

        setTitle("DES Cipher");
        setResizable(false);

        jButton1.setText("Encrypt file");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Decrypt file");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        keyTextField1.setText("770DD5180AC82461");

        jLabel2.setText("Key");

        genKeyButton3.setText("Gen. key");
        genKeyButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genKeyButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keyTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(genKeyButton3)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genKeyButton3)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        encryptFile();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void genKeyButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genKeyButton3ActionPerformed
        // TODO add your handling code here:
        String genKey = des.generateKey(16);
        keyTextField1.setText(genKey);
    }//GEN-LAST:event_genKeyButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        decryptFile();
    }//GEN-LAST:event_jButton2ActionPerformed

    private File openFile() {
        int openState = fChooser.showOpenDialog(this);

        if (openState == JFileChooser.APPROVE_OPTION) {
            return fChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private File saveFile() {
        int saveState = fChooser.showSaveDialog(this);

        if (saveState == JFileChooser.APPROVE_OPTION) {
            return fChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private void displayGuiMessage(String message, String windowTitle, int JOptionPaneMessType) {
        JOptionPane.showMessageDialog(this, message, windowTitle, JOptionPaneMessType);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton genKeyButton3;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField keyTextField1;
    // End of variables declaration//GEN-END:variables
}
