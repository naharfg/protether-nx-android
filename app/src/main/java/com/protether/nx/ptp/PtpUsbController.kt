package com.protether.nx.ptp

import android.content.Context
import android.hardware.usb.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class CameraExifItem(
    val objectHandle: Int,
    val fileName: String,
    val isRawNef: Boolean,
    val shutterSpeed: String,
    val aperture: String,
    val iso: String,
    val focalLength: String,
    val fileSizeMb: Double
)

class PtpUsbController(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var endpointOut: UsbEndpoint? = null
    private var endpointIn: UsbEndpoint? = null
    private var transactionId = 1

    fun findConnectedCamera(): UsbDevice? {
        return usbManager.deviceList.values.firstOrNull { device ->
            (0 until device.interfaceCount).any { i ->
                device.getInterface(i).interfaceClass == UsbConstants.USB_CLASS_STILL_IMAGE
            }
        }
    }

    suspend fun openSession(device: UsbDevice): Boolean = withContext(Dispatchers.IO) {
        val intf = (0 until device.interfaceCount)
            .map { device.getInterface(it) }
            .firstOrNull { it.interfaceClass == UsbConstants.USB_CLASS_STILL_IMAGE }
            ?: return@withContext false

        val conn = usbManager.openDevice(device) ?: return@withContext false
        if (!conn.claimInterface(intf, true)) {
            conn.close()
            return@withContext false
        }

        for (i in 0 until intf.endpointCount) {
            val ep = intf.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_OUT) endpointOut = ep
                else if (ep.direction == UsbConstants.USB_DIR_IN) endpointIn = ep
            }
        }

        connection = conn
        usbInterface = intf
        sendPtpCommand(PtpConstants.OP_OPEN_SESSION, intArrayOf(1))
    }

    suspend fun setExposureProperty(propCode: Short, rawValue: Int): Boolean = withContext(Dispatchers.IO) {
        sendPtpCommand(PtpConstants.OP_SET_DEVICE_PROP_VALUE, intArrayOf(propCode.toInt(), rawValue))
    }

    suspend fun setTouchAfCoordinate(normX: Float, normY: Float, sensorWidth: Int = 6000, sensorHeight: Int = 4000): Boolean = withContext(Dispatchers.IO) {
        val targetX = (normX * sensorWidth).toInt().coerceIn(0, sensorWidth)
        val targetY = (normY * sensorHeight).toInt().coerceIn(0, sensorHeight)
        sendPtpCommand(PtpConstants.OP_NIKON_CHANGE_AF_AREA, intArrayOf(targetX, targetY))
    }

    suspend fun triggerRemoteShutter(): Boolean = withContext(Dispatchers.IO) {
        sendPtpCommand(PtpConstants.OP_INITIATE_CAPTURE, intArrayOf(0, 0))
    }

    private fun sendPtpCommand(opCode: Short, params: IntArray = intArrayOf()): Boolean {
        val conn = connection ?: return false
        val epOut = endpointOut ?: return false
        val length = 12 + (params.size * 4)

        val buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(length)
        buffer.putShort(1) // Container Type: Command Block
        buffer.putShort(opCode)
        buffer.putInt(transactionId++)
        params.forEach { buffer.putInt(it) }

        val bytes = buffer.array()
        val transferred = conn.bulkTransfer(epOut, bytes, bytes.size, 2500)
        return transferred >= 0
    }

    fun close() {
        usbInterface?.let { connection?.releaseInterface(it) }
        connection?.close()
    }
}
