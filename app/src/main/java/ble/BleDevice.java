package ble;

/**
 * Created by orange on 4/28/17.
 */

public class BleDevice {

	private String name;     // name of ble
	private String macAddr;  // mac address
	private int    rssi;     // strength of signal

	public BleDevice(String name, String macAddr, int rssi) {
		super();
		this.name = name;
		this.macAddr = macAddr;
		this.rssi = rssi;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}

	// overwrite the hashCode and equals functions, so that we can compare two bleDevices
	public int hashCode(){

		return 17+ 9 * getMacAddr().hashCode();
    }
	
	public boolean equals(Object other){
		if(!(other instanceof BleDevice)) {
		    return false; 
		}
		
		final BleDevice bleDevice = (BleDevice)other;
		if(!getMacAddr().equals(bleDevice.getMacAddr())){
			return false;
		}

		return true;	
	}

	@Override
	public String toString() {
		return "Ble name=" + name + ", macAddr="
				+ macAddr + ", rssi=" + rssi ;
	}
}
