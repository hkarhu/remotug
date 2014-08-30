package fi.uef.remotug;

import gnu.io.CommPortIdentifier;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

public class SetupDialog extends JDialog {

    private JButton buttonConnect;
    private JComboBox comboSensorPort;
    private JComboBox comboSensorSpeed;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JTextField textConnectionAddress;
    private JTextField textConnectionPort;
    private JTextField textName;
    
    private final Settings settings;
    private boolean userSelectedConnect = false;
    
    private static String[] listSerialPorts() {
    	 
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        ArrayList portList = new ArrayList();
        portList.add("emulation");
        //portList.add("/dev/ttyACM0");
        //portList.add("/dev/ttyUSB0");
        String portArray[] = null;
        while (ports.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
            if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portList.add(port.getName());
            }
        }
        portArray = (String[]) portList.toArray(new String[0]);
        return portArray;
    }
    
    public SetupDialog(final Settings settings) {
    	this.setTitle("RemoTug settings");
    	this.settings = settings;
    	this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    	this.setModal(true);
    	
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();

        textName = new JTextField();
        textConnectionAddress = new JTextField();
        textConnectionPort = new JTextField();
        comboSensorPort = new JComboBox();
        comboSensorSpeed = new JComboBox<Integer>();
        buttonConnect = new JButton();

        jLabel1.setFont(new java.awt.Font("DejaVu Serif", 1, 18));
        jLabel1.setText("Settings");

        jLabel2.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel2.setText("Player team name");

        textName.setText(settings.getPlayerName());
        textConnectionAddress.setText(settings.getServerAddress());
        textConnectionPort.setText(settings.getServerPort() + "");
        
        jLabel3.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel3.setText("Connection address");

        jLabel4.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel4.setText("Connection port");

        String[] ports = listSerialPorts();
        
        comboSensorPort.setModel(new DefaultComboBoxModel(ports));
        comboSensorPort.setEditable(true);
        comboSensorPort.setSelectedItem(settings.getSensorPort());
        comboSensorPort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //TODO
            }
        });

        jLabel5.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel5.setText("Sensor port");

        jLabel6.setFont(new java.awt.Font("DejaVu Serif", 1, 12));
        jLabel6.setText("Connection");

        jLabel7.setFont(new java.awt.Font("DejaVu Serif", 1, 12));
        jLabel7.setText("Sensor");

        comboSensorSpeed.setSelectedItem(settings.getSensorSpeed());
        comboSensorSpeed.setModel(new DefaultComboBoxModel(new Integer[] { 38400, 9600 }));
        comboSensorSpeed.setEditable(true);
        comboSensorSpeed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //TODO
            }
        });

        jLabel8.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel8.setText("Sensor baud rate");

        buttonConnect.setText("Connect");
        buttonConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	userSelectedConnect = true;
            	
            	settings.setPlayerName(textName.getText());
            	settings.setServerAddress(textConnectionAddress.getText());
            	try {
            		settings.setServerPort(Integer.parseUnsignedInt(textConnectionPort.getText()));
            	} catch (NumberFormatException e){
            		settings.setServerPort(4575);
            	}
            	
            	settings.setSensorPort(comboSensorPort.getSelectedItem().toString());
            	settings.setSensorSpeed((int)comboSensorSpeed.getSelectedItem());
            	
            	settings.print();
            	settings.saveSettings(settings);
            	SetupDialog.this.dispose();
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboSensorPort, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textName, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textConnectionAddress, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textConnectionPort, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel8, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboSensorSpeed, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonConnect, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(textName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(textConnectionAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(textConnectionPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(comboSensorPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(comboSensorSpeed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonConnect)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        this.pack();
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}

	public boolean userSelectedConnect() {
		return userSelectedConnect;
	}

	public Settings getSettings() {
		return settings;
	}
}
