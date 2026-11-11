package com.protether.nx.ptp

object PtpConstants {
    // Standard PTP Operation Codes (ISO 15740)
    const val OP_GET_DEVICE_INFO: Short = 0x1001
    const val OP_OPEN_SESSION: Short = 0x1002
    const val OP_CLOSE_SESSION: Short = 0x1003
    const val OP_GET_STORAGE_IDS: Short = 0x1004
    const val OP_GET_OBJECT_HANDLES: Short = 0x1007
    const val OP_GET_OBJECT_INFO: Short = 0x1008
    const val OP_GET_OBJECT: Short = 0x1009
    const val OP_GET_THUMB: Short = 0x100A
    const val OP_INITIATE_CAPTURE: Short = 0x100E
    const val OP_GET_DEVICE_PROP_VALUE: Short = 0x1015
    const val OP_SET_DEVICE_PROP_VALUE: Short = 0x1016

    // Nikon Vendor-Specific Live View & Focus Operations
    const val OP_NIKON_START_LIVE_VIEW: Short = 0x9201.toShort()
    const val OP_NIKON_END_LIVE_VIEW: Short = 0x9202.toShort()
    const val OP_NIKON_GET_LIVE_VIEW_IMAGE: Short = 0x9203.toShort()
    const val OP_NIKON_MF_DRIVE: Short = 0x9204.toShort()
    const val OP_NIKON_CHANGE_AF_AREA: Short = 0x9205.toShort()

    // PTP Device Property Codes (Exposure & WB)
    const val PROP_WHITE_BALANCE: Short = 0x5005
    const val PROP_F_NUMBER: Short = 0x5007          // Aperture
    const val PROP_EXPOSURE_TIME: Short = 0x500D     // Shutter Speed
    const val PROP_EXPOSURE_INDEX: Short = 0x500F    // ISO

    // PTP Event Codes
    const val EVENT_OBJECT_ADDED: Short = 0x4002     // Auto-Tether Trigger
}
