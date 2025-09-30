package com.internetguard.pro.data.model

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable

/**
 * Data class representing an installed app with its blocking status.
 */
data class AppInfo(
	val uid: Int,
	val packageName: String,
	val appName: String,
	val icon: Drawable?,
	val isInstalled: Boolean = true,
	val blockWifi: Boolean = false,
	val blockCellular: Boolean = false,
	val blockMode: String = "blacklist"
) : Parcelable {
	
	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		null, // Drawable cannot be parceled easily
		parcel.readByte() != 0.toByte(),
		parcel.readByte() != 0.toByte(),
		parcel.readByte() != 0.toByte(),
		parcel.readString() ?: "blacklist"
	)
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(uid)
		parcel.writeString(packageName)
		parcel.writeString(appName)
		parcel.writeByte(if (isInstalled) 1 else 0)
		parcel.writeByte(if (blockWifi) 1 else 0)
		parcel.writeByte(if (blockCellular) 1 else 0)
		parcel.writeString(blockMode)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	companion object CREATOR : Parcelable.Creator<AppInfo> {
		override fun createFromParcel(parcel: Parcel): AppInfo {
			return AppInfo(parcel)
		}
		
		override fun newArray(size: Int): Array<AppInfo?> {
			return arrayOfNulls(size)
		}
	}
}
