package com.skylin.mavlink.connection.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeTransmitOptions;
import com.digi.xbee.api.packet.XBeePacket;
import com.digi.xbee.api.packet.common.ReceivePacket;
import com.digi.xbee.api.packet.common.TransmitPacket;
import com.skylin.mavlink.connection.MavLinkConnection;
import com.skylin.mavlink.exception.USBRejectException;
import com.skylin.mavlink.model.ConnectionParameter;
import com.skylin.mavlink.model.UsbConnectionParameter;
import com.skylin.mavlink.xbee.XBeeParser;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.receiver.USBPermissionUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import sjj.alog.Log;


import static com.skylin.mavlink.utils.UsbUtilsKt.getAccessory;

public class UsbConnection extends MavLinkConnection {

	private static final int FTDI_DEVICE_VENDOR_ID = 0x0403;

	private Context context;
	protected final UsbConnectionParameter usbConnectionParameter ;

	private UsbConnectionImpl mUsbConnection;

	public UsbConnection(Context context, UsbConnectionParameter usbConnectionParameter) {
		this.context = context;
		this.usbConnectionParameter = usbConnectionParameter;
	}

	@Override
	public void closeConnection() throws IOException {
		if (mUsbConnection != null) {
			mUsbConnection.closeUsbConnection();
		}
	}

    @Override
    public void openConnection() throws Exception {

		if (getAccessory(context) != null) {
			mUsbConnection = new UsbAccessoryConnection(context, usbConnectionParameter.getBaudRate());
			mUsbConnection.openUsbConnection();
			return;
		}

		Boolean aBoolean = USBPermissionUtil.request(APP.getContext()).blockingFirst();
		if (!aBoolean) {
            throw new USBRejectException();
        }
		if (mUsbConnection != null) {
            try {
                mUsbConnection.openUsbConnection();
                Log.e("Reusing previous usb connection.");
                return;
            } catch (IOException e) {
                Log.e("Previous usb connection is not usable.", e);
                mUsbConnection = null;
            }
        }

		if (isFTDIdevice(context)) {
			final UsbConnectionImpl tmp = new UsbFTDIConnection(context, usbConnectionParameter.getBaudRate());
			try {
				tmp.openUsbConnection();

				// If the call above is successful, 'mUsbConnection' will be set.
				mUsbConnection = tmp;
				Log.e("Using FTDI usb connection.");
			} catch (IOException e) {
				Log.e("Unable to open a ftdi usb connection. Falling back to the open "
						+ "usb-library.", e);
			}
		}

		// Fallback
		if (mUsbConnection == null) {
			final UsbConnectionImpl tmp = new UsbCDCConnection(context, usbConnectionParameter.getBaudRate());

			// If an error happens here, let it propagate up the call chain since this is the
			// fallback.
			tmp.openUsbConnection();
			mUsbConnection = tmp;
			Log.e("Using open-source usb connection.");
		}
	}

	private static boolean isFTDIdevice(Context context) {
		UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		if (deviceList == null || deviceList.isEmpty()) {
			return false;
		}

		for (Entry<String, UsbDevice> device : deviceList.entrySet()) {
			if (device.getValue().getVendorId() == FTDI_DEVICE_VENDOR_ID) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int readDataBlock(byte[] buffer) throws IOException {
		if (mUsbConnection == null) {
			throw new IOException("Uninitialized usb connection.");
		}

		return mUsbConnection.readDataBlock(buffer);
	}

	@Override
	public void sendBuffer(byte[] buffer) throws IOException {
		if (mUsbConnection == null) {
			throw new IOException("Uninitialized usb connection.");
		}

		mUsbConnection.sendBuffer(buffer);
	}

	@Override
	public int getConnectionType() {
		return ConnectionParameter.usb;
	}

	@Override
	public String toString() {
		if (mUsbConnection == null) {
			return super.toString();
		}

		return mUsbConnection.toString();
	}

	static abstract class UsbConnectionImpl {
		protected final int mBaudRate;
		protected final Context mContext;

		protected UsbConnectionImpl(Context context, int baudRate) {
			mContext = context;
			mBaudRate = baudRate;
		}

		protected abstract void closeUsbConnection() throws IOException;

		protected abstract void openUsbConnection() throws IOException;

		protected abstract int readDataBlock(byte[] readData) throws IOException;

		protected abstract void sendBuffer(byte[] buffer) throws IOException;
	}


	private XBeeParser parser = new XBeeParser();
	public int readData(byte[] buffer,String mac_adress) throws IOException {

		if (mUsbConnection == null) {
			throw new IOException("usb未连接");
		}

		int len = mUsbConnection.readDataBlock(buffer);
		byte[] tmp = new byte[buffer.length];
		int tmpIndex = 0;
		for (int i = 0; i < len; i++) {
			XBeePacket packet = parser.parse(buffer[i]);
			if (packet != null && packet instanceof ReceivePacket) {
				Log.e(((ReceivePacket) packet).get64bitSourceAddress().toString());
				if (((ReceivePacket) packet).get64bitSourceAddress().toString().equals(mac_adress)){
					byte[] bytes = ((ReceivePacket) packet).getRFData();
					System.arraycopy(bytes, 0, tmp, tmpIndex, bytes.length);
					tmpIndex += bytes.length;
				}

			}
		}
		System.arraycopy(tmp, 0, buffer, 0, tmpIndex);
		return tmpIndex;                               //新
	}


	private int seq = 1;

	public void sendBuf(byte[] buffer ,String target) throws IOException {

		//seq  0 255     target  目标mac

//        byte[] bytes = new TransmitPacket(seq++, new XBee64BitAddress("0013A20041680F3E"), XBee16BitAddress.UNKNOWN_ADDRESS, 0, XBeeTransmitOptions.NONE, buffer).generateByteArray();
		byte[] bytes = new TransmitPacket(seq++, new XBee64BitAddress(target), XBee16BitAddress.UNKNOWN_ADDRESS, 0, XBeeTransmitOptions.NONE, buffer).generateByteArray();
		if (seq == 255) {
			seq = 1;
		}

		if (mUsbConnection == null) {
			throw new IOException("usb未连接.");
		}
//        Log.e(Arrays.toString(bytes));
		mUsbConnection.sendBuffer(bytes);       //新
	}

}
