package ble;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.ble.antilost.R;
import java.util.List;

/**
 * Created by orange on 4/29/17.
 */
public class BleDeviceListAdapter extends BaseAdapter {
	private List<BleDevice> bleDevices;
	Context context;
	
	private final static int safeRssi=-70;
	private final static int unsafeRssi=-90;

	public BleDeviceListAdapter(Context context,List<BleDevice> bleDevices){
		this.bleDevices = bleDevices;
		this.context = context;
	}
	
    public void clear() {
    	bleDevices.clear();
    }
	
	@Override
	public int getCount() {
		return (bleDevices==null)?0:bleDevices.size();
	}

	@Override
	public Object getItem(int position) {
		if(bleDevices==null){
			return null;
		}
		return bleDevices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder{
		TextView device_mac;
		TextView device_name;
		TextView state_text;
		ImageView state_img;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final BleDevice bleDevice = (BleDevice)getItem(position);
		ViewHolder viewHolder = null;
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(
					R.layout.fragment_discover_list_item, null);
			
			viewHolder = new ViewHolder();
			viewHolder.state_img=(ImageView)convertView.findViewById(
					R.id.state_img);
			viewHolder.device_mac = (TextView)convertView.findViewById(
					R.id.device_mac);
			viewHolder.device_name = (TextView)convertView.findViewById(
					R.id.device_name);
			viewHolder.state_text = (TextView)convertView.findViewById(
					R.id.state_text);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}         
		    
		int rssi=bleDevice.getRssi();
		if(rssi==0){
			viewHolder.state_img.setImageResource(R.drawable.alarm);
		}else if(rssi>safeRssi){
			if(rssi>-50){//(-50，+&)
				viewHolder.state_img.setImageResource(R.drawable.signal5);
			}else if(rssi>-60){//(-60,-50]
				viewHolder.state_img.setImageResource(R.drawable.signal4);
			}else{       //(-70,-60]
				viewHolder.state_img.setImageResource(R.drawable.signal3);
			}
		}else if(rssi>unsafeRssi){
			if(rssi>-70){//(-80，-70]
				viewHolder.state_img.setImageResource(R.drawable.signal2);
			}else if(rssi>-88){//(-88,-80]
				viewHolder.state_img.setImageResource(R.drawable.signal1);
			}
		}else{
			viewHolder.state_img.setImageResource(R.drawable.signal0);
		}
		
		viewHolder.device_name.setText(bleDevice.getName());
		viewHolder.device_mac.setText(String.valueOf(bleDevice.getMacAddr()));

		if (rssi == 0) {
			viewHolder.state_text.setText("Lost!");
			Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
			// Vibrate for 2000 milliseconds
			v.vibrate(1500);
		} else {
			viewHolder.state_text.setText(String.valueOf(-(float)(rssi + 30) / 35) + "m");
		}

		
		viewHolder.device_mac.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Toast.makeText(context, 
						"[textViewItem01.setOnClickListener]"+bleDevice.getName(), 
						Toast.LENGTH_SHORT).show();
			}
		});
		
		  
		convertView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Toast.makeText(context, 
						"[convertView.setOnClickListener]"+bleDevice.getName(), 
						Toast.LENGTH_SHORT).show();
				clear();
				bleDevices.add(bleDevice);
				BlePreventLostActivity.foundBleDevicesList.clear();
				BlePreventLostActivity.foundBleDevicesList.add(bleDevice);
				BlePreventLostActivity.scannedBleDevicesList.clear();
				BlePreventLostActivity.scannedBleDevicesList.add(bleDevice);
			}
			  
		});
		
		
		convertView.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(context, 
						"[convertView.setOnLongClickListener]"+bleDevice.getName(), 
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		return convertView;
	}

}
