package com.lpp.client.aidl;

import com.lpp.client.aidl.SEND_MESSAGE_RESULT;
import com.lpp.client.aidl.ILppMainServiceCallback;
import com.lpp.client.aidl.SEARCH_GROUP_RESULT;
import com.lpp.client.aidl.LPP_TOKEN_RESULT;

interface ILppMainService {
	String getJniVersion();
	String getLpcVersion();
	LPP_TOKEN_RESULT getLppToken(String strDeviceId, String strServerIp, int nServerPort);
	int start(String strLoginTokenId, String strDeviceId, String strServiceId, String strServicePwd, String strServerIp, int nServerPort, String strFilePath, in ILppMainServiceCallback serviceCallback);
	int stop(String strServerIp, String strServiceId, ILppMainServiceCallback serviceCallback);
	int networkReconnected();
	int getClientStatus(String strServerIp, String strServiceId);
	SEND_MESSAGE_RESULT sendMessage(String strServiceId, String receiverGroupId, String receiverId, String message);
	SEND_MESSAGE_RESULT sendFile(String strServiceId, String receiverGroupId, String receiverId, int fileType, String filePath);
	int makeMixedData();
	int addMixedText(String message);
	int addMixedFile(String strServiceId, int fileType, String filePath);
	SEND_MESSAGE_RESULT sendMixedData(String strServiceId, String receiverGroupId, String receiverId);
	SEND_MESSAGE_RESULT addGroup(String strServiceId, in List<String> listUser);
	SEND_MESSAGE_RESULT deleteGroup(String strServiceId, String groupId);
	SEND_MESSAGE_RESULT addGroupUser(String strServiceId, String groupId, in List<String> listUser);
	SEND_MESSAGE_RESULT deleteGroupUser(String strServiceId, String groupId, in List<String> listUser);
	SEND_MESSAGE_RESULT cancelSendData(String strServiceId, int dataType, String groupId, String strReceiverId, String strMessageId);
	SEND_MESSAGE_RESULT requestData(String strServiceId, int dataType, String groupId, String strSenderId, String strMessageId);
	SEARCH_GROUP_RESULT getGroupUserList(String strLoginTokenId, String strServiceId, String strServicePwd, String strSearchGroupId);
	int getMaxFileSize(String strServiceId);
	int setReceiveMessage(String strLoginTokenId, String strServiceId, String strServicePwd, boolean bReceive);
	int getReconnCycle();
	int getReconnCount();
}
