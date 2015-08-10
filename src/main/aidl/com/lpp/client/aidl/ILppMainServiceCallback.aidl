package com.lpp.client.aidl;

import com.lpp.client.aidl.MIXED_DATA;

interface ILppMainServiceCallback {
	void onDeleteMyself(String strServiceId);
	void onGroupList(String strServiceId, in List<String> list);
	void onReceiveMessage(String strServiceId, String strGroupId, int nMessageType, String strMessageId, String strSenderId, String strReceiverId, String strMessage);
	
	void onReceiveFile(String strServiceId, String strGroupId, int nMessageType, String strMessageId, String strSenderId, String strReceiverId, int nFileType, int nFileSize, String strFileExt, String strFilePath);
	void onReceiveMixedData(String strServiceId, String strGroupId, int nMessageType, String strMessageId, String strSenderId, String strReceiverId, in List<MIXED_DATA> mixedInfo);
	void onServerStatus(String strServiceId, int status, int result);
	void onServiceType(String strServiceId, int pushType, int dataType);
	void onMessageReceiveSetValue(String strServiceId, boolean isReceive);
	void onSendMessageResult(String strServiceId, String strMessageId, int result, int totalSendCount, int ackUserCount);
	void onLppTokenNotify(String strServiceId, int newCreateFlag, String strTokenId);
	
	void onAddGroupUser(String strServiceId, String strGroupId, String messageId);
	void onDeleteGroupUser(String strServiceId, String strGroupId, String messageId);
//	void onAddGroupUserList(String strServiceId, String strGroupId, String messageId, in List<String> strAddUser);
//	void onDeleteGroupUserList(String strServiceId, String strGroupId, String messageId, in List<String> strDeleteUser);
	void onAddGroup(String strServiceId, String strGroupId, String messageId);
	void onDeleteGroup(String strServiceId, String strGroupId, String messageId);

	void onAddGroupUserFail(String strServiceId, String strGroupId, String messageId, int nErrorCode);
	void onDeleteGroupUserFail(String strServiceId, String strGroupId, String messageId, int nErrorCode);
	void onAddGroupFail(String strServiceId, String strGroupId, String messageId, int nErrorCode);
	void onDeleteGroupFail(String strServiceId, String strGroupId, String messageId, int nErrorCode);

	void onFileReceiveNotify(String strServiceId, String strGroupId, String strSenderId, String strMessageId);
	void onMixedReceiveNotify(String strServiceId, String strGroupId, String strSenderId, String strMessageId);
	void onFileDataDownloadFail(String strServiceId, String strMessageId, int nErrorCode);
	void onMixedDataDownloadFail(String strServiceId, String strMessageId, int nErrorCode);
}