	package com.maintainet.gwsoap;

/*
 Copyright 2006 rhulha_(@t)_maintainet_(d0t)_com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

/*
Version 1.0
 initial release;

Version 1.1
 changed return type from Status to void using the fire() method.
 added getUserList() for Trusted Applications

Todo
 redirectToHost detection

*/



import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Calendar;

import javax.xml.rpc.Stub;

import com.novell.groupwise.ws.*;

public class EasySoap
{
	private GroupWisePortType gwPort;

	private String sessionId;

	private LoginResponse loginResponse;

	private BigDecimal version = new BigDecimal( 1.0);

	public boolean gwTrace = false;
	
	private String user;

	// if tappName != null a TrustedApplication is logged in.

	public EasySoap( String server, String port, String tappName, String username, String pass) throws RemoteException {
		user = username;
		gwPort = new GroupwiseService_Impl().getGroupwiseSOAPPort();

		if( server == null || port == null)
		{
			throw new IllegalArgumentException( "null == server || null == port");
		}
		((Stub) gwPort)._setProperty( Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + server + ":" + port + "/soap");

		Authentication auth = null;
		if( tappName == null)
		{
			auth = new PlainText( username, pass);
                        tappName = "GWSoapClient";
		}
		else
		{
			auth = new TrustedApplication( username, tappName, pass);
		}

		loginResponse = gwPort.loginRequest( auth, "us", version, tappName, false);
		fire( loginResponse.getStatus());
		sessionId = loginResponse.getSession();
	}

	public LoginResponse getLoginResponse()
	{
		return loginResponse;
	}

	public static void fire( Status status)
	{
		if( status.getCode() != 0)
			throw new RuntimeException( status.getDescription());
	}

	public void accept( ItemRefList items, String comment, AcceptLevel acceptLevel, long recurrenceAllInstances) throws RemoteException
	{
		fire( gwPort.acceptRequest( items, comment, acceptLevel, recurrenceAllInstances, sessionId, gwTrace));
	}

	public String acceptShare( String id, String name, String container, String description) throws RemoteException
	{
		AcceptShareResponse resp = gwPort.acceptShareRequest( id, name, container, description, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getId();
	}

	public void addItem( String container, String id) throws RemoteException
	{
		fire( gwPort.addItemRequest( container, id, sessionId, gwTrace));
	}

	public void addItems( String container, ItemRefList items) throws RemoteException
	{
		fire( gwPort.addItemsRequest( container, items, sessionId, gwTrace));
	}

	public void addMembers( String container, GroupMemberList members) throws RemoteException
	{
		fire( gwPort.addMembersRequest( container, members, sessionId, gwTrace));
	}

	public void closeFreeBusySession( int freeBusySessionId) throws RemoteException
	{
		fire( gwPort.closeFreeBusySessionRequest( freeBusySessionId, sessionId, gwTrace));
	}

	public void complete( ItemRefList items) throws RemoteException
	{
		fire( gwPort.completeRequest( items, sessionId, gwTrace));
	}

	public Integer createCursor( String container, String[] view, Filter filter) throws RemoteException
	{
		CreateCursorResponse resp = gwPort.createCursorRequest( container, view, filter, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getCursor();
	}

	public String[] createItem( Item item, SharedFolderNotification notification) throws RemoteException
	{
		CreateItemResponse resp = gwPort.createItemRequest( item, notification, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getId();
	}

	public String[] createItems( Item[] item) throws RemoteException
	{
		CreateItemsResponse resp = gwPort.createItemsRequest( item, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getId();
	}

	public String createJunkEntry( JunkEntry entry) throws RemoteException
	{
		CreateJunkEntryResponse resp = gwPort.createJunkEntryRequest( entry, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getId();
	}

	public String createProxyAccess( AccessRightEntry entry) throws RemoteException
	{
		CreateProxyAccessResponse resp = gwPort.createProxyAccessRequest( entry, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getId();
	}

	public void createSignature( Signature signature) throws RemoteException
	{
		fire( gwPort.createSignatureRequest( signature, sessionId, gwTrace));
	}

	public void decline( ItemRefList items, String comment, long recurrenceAllInstances) throws RemoteException
	{
		fire( gwPort.declineRequest( items, comment, recurrenceAllInstances, sessionId, gwTrace));
	}

	public void delegate( String id, String commentToOrganizer, String commentToDelegatee, Distribution distribution, long recurrenceAllInstances) throws RemoteException
	{
		fire( gwPort.delegateRequest( id, commentToOrganizer, commentToDelegatee, distribution, recurrenceAllInstances, sessionId, gwTrace));
	}

	public void destroyCursor( String container, int cursor) throws RemoteException
	{
		fire( gwPort.destroyCursorRequest( container, cursor, sessionId, gwTrace));
	}

	public void executeRule( String id) throws RemoteException
	{
		fire( gwPort.executeRuleRequest( id, sessionId, gwTrace));
	}

	public Item forward( String id, String[] view, boolean embed) throws RemoteException
	{
		ForwardResponse resp = gwPort.forwardRequest( id, view, embed, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItem();
	}

	public AddressBook[] getAddressBookList() throws RemoteException
	{
		GetAddressBookListResponse resp = gwPort.getAddressBookListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getBooks().getBook();
	}

	public MessagePart getAttachment( String id, int offset, int length) throws RemoteException
	{
		GetAttachmentResponse resp = gwPort.getAttachmentRequestMessage( id, offset, length, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getPart();
	}

	public Category[] getCategoryList() throws RemoteException
	{
		GetCategoryListResponse resp = gwPort.getCategoryListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getCategories().getCategory();
	}

	public Custom[] getCustomList() throws RemoteException
	{
		GetCustomListResponse resp = gwPort.getCustomListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getCustoms().getCustom();
	}

	public DeltaInfo getDeltaInfo( String container) throws RemoteException
	{
		GetDeltaInfoResponse resp = gwPort.getDeltaInfoRequest( container, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getDeltaInfo();
	}

	public Item[] getDeltas( String container, String[] view, DeltaInfo deltaInfo) throws RemoteException
	{
		GetDeltasResponse resp = gwPort.getDeltasRequest( container, view, deltaInfo, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItems().getItem();
	}

	public DocumentType[] getDocumentTypeList( String library) throws RemoteException
	{
		GetDocumentTypeListResponse resp = gwPort.getDocumentTypeListRequest( library, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItems().getItem();
	}

	public Folder[] getFolderList( String parent, String[] view, boolean recurse, boolean imap, boolean nntp) throws RemoteException
	{
		GetFolderListResponse resp = gwPort.getFolderListRequest( parent, view, recurse, imap, nntp, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getFolders().getFolder();
	}

	public Folder getFolder( String id, FolderType folderType, MessageType[] types, ItemSource[] source, String[] view) throws RemoteException
	{
		GetFolderResponse resp = gwPort.getFolderRequest( id, folderType, types, source, view, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getFolder();
	}

	public FreeBusyInfoList getFreeBusyInfo( String freeBusySessionId) throws RemoteException
	{
		GetFreeBusyResponse resp = gwPort.getFreeBusyRequest( freeBusySessionId, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getFreeBusyInfo();
	}

	public FreeBusyStats getFreeBusyStats( String freeBusySessionId) throws RemoteException
	{
		GetFreeBusyResponse resp = gwPort.getFreeBusyRequest( freeBusySessionId, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getFreeBusyStats();
	}

	public Item getItem( String id, String[] view) throws RemoteException
	{
		GetItemResponse resp = gwPort.getItemRequest( id, view, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItem();
	}

	public Item[] getItems( String container, String[] view, Filter filter, ItemRefList items, int count) throws RemoteException
	{
		GetItemsResponse resp = gwPort.getItemsRequest( container, view, filter, items, count, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItems().getItem();
	}

	public GetJunkEntriesResponse getJunkEntries( JunkHandlingListType container) throws RemoteException
	{
		GetJunkEntriesResponse resp = gwPort.getJunkEntriesRequest( container, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp;
	}

	public Custom[] getJunkMailSettings() throws RemoteException
	{
		GetJunkMailSettingsResponse resp = gwPort.getJunkMailSettingsRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getSettings().getSetting();
	}

	public Item getLibraryItem( String library, long documentNumber, String versionNumber) throws RemoteException
	{
		GetLibraryItemResponse resp = gwPort.getLibraryItemRequest( library, documentNumber, versionNumber, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItem();
	}

	public Library[] getLibraryList() throws RemoteException
	{
		GetLibraryListResponse resp = gwPort.getLibraryListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getLibraries().getLibrary();
	}

	public AccessRightEntry[] getProxyAccessList() throws RemoteException
	{
		GetProxyAccessListResponse resp = gwPort.getProxyAccessListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getAccessRights().getEntry();
	}

	public ProxyUser[] getProxyList() throws RemoteException
	{
		GetProxyListResponse resp = gwPort.getProxyListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getProxies().getProxy();
	}

	public Item[] getQuickMessages( MessageList list, Calendar startDate, String container, MessageType[] types, ItemSource[] source, String[] view, int count)
			throws RemoteException
	{
		GetQuickMessagesResponse resp = gwPort.getQuickMessagesRequest( list, startDate, container, types, source, view, count, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItems().getItem();
	}

	public Rule[] getRuleList() throws RemoteException
	{
		GetRuleListResponse resp = gwPort.getRuleListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getRules().getRule();
	}

	public Settings getSettings( String id) throws RemoteException
	{
		GetSettingsResponse resp = gwPort.getSettingsRequest( id, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getSettings();
	}

	public Signature[] getSignatures( Boolean global) throws RemoteException
	{
		GetSignaturesResponse resp = gwPort.getSignaturesRequest( global, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getSignatures().getSignature();
	}

	public GetTimestampResponse getTimestamp( Boolean backup, Boolean retention, Boolean noop) throws RemoteException
	{
		GetTimestampResponse resp = gwPort.getTimestampRequest( backup, retention, noop, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp;
	}

	public Timezone[] getTimezoneList() throws RemoteException
	{
		GetTimezoneListResponse resp = gwPort.getTimezoneListRequest( sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getTimezones().getTimezone();
	}

	// trusted App key !
	public static UserInfo[] getUserList( String server, String port, String tappName, String tappKey, boolean debug) throws RemoteException
	{
		GroupWisePortType gwPort = new GroupwiseService_Impl().getGroupwiseSOAPPort();
		if( server == null || port == null)
		{
			throw new IllegalArgumentException( "null == server || null == port");
		}
		((Stub) gwPort)._setProperty( Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + server + ":" + port + "/soap");
		GetUserListResponse resp = gwPort.getUserListRequest(tappName, tappKey, null, debug);
		fire( resp.getStatus());
		return resp.getUsers().getUser();
	}

	public UserInfo[] getUserList( String name, String key) throws RemoteException
	{
		GetUserListResponse resp = gwPort.getUserListRequest( name, key, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getUsers().getUser();
	}

	public LoginResponse login( Authentication auth, String language, BigDecimal version, String application, boolean gwTrace) throws RemoteException
	{
		LoginResponse resp = gwPort.loginRequest( auth, language, version, application, gwTrace);
		fire( resp.getStatus());
		return resp;
	}

	// se = SOAPFactory.newInstance().createElement("logoutRequest");
	public void logout() throws RemoteException
	{
		fire( gwPort.logoutRequest( sessionId, gwTrace));
	}

	public void markPrivate( ItemRefList items) throws RemoteException
	{
		fire( gwPort.markPrivateRequest( items, sessionId, gwTrace));
	}

	public void markRead( ItemRefList items) throws RemoteException
	{
		fire( gwPort.markReadRequest( items, sessionId, gwTrace));
	}

	public void markUnPrivate( ItemRefList items) throws RemoteException
	{
		fire( gwPort.markUnPrivateRequest( items, sessionId, gwTrace));
	}
	

	public void markUnRead( ItemRefList items) throws RemoteException
	{
		fire( gwPort.markUnReadRequest( items, sessionId, gwTrace));
	}

	public String[] modifyItem( String id, SharedFolderNotification notification, ItemChanges updates, long recurrenceAllInstances) throws RemoteException
	{
		ModifyItemResponse resp = gwPort.modifyItemRequest( id, notification, updates, recurrenceAllInstances, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getId();
	}

	public void modifyJunkEntry( JunkEntry entry) throws RemoteException
	{
		fire( gwPort.modifyJunkEntryRequest( entry, sessionId, gwTrace));
	}

	public void modifyJunkMailSettings( SettingsList settings) throws RemoteException
	{
		fire( gwPort.modifyJunkMailSettingsRequest( settings, sessionId, gwTrace));
	}

	public void modifyPassword( String old, String _new) throws RemoteException
	{
		fire( gwPort.modifyPasswordRequest( old, _new, sessionId, gwTrace));
	}

	public void modifyProxyAccess( String id, AccessRightChanges updates) throws RemoteException
	{
		fire( gwPort.modifyProxyAccessRequest( id, updates, sessionId, gwTrace));
	}

	public void modifySettings( SettingsList settings) throws RemoteException
	{
		fire( gwPort.modifySettingsRequest( settings, sessionId, gwTrace));
	}

	public void modifySignatures( Signatures updates) throws RemoteException
	{
		fire( gwPort.modifySignaturesRequest( updates, sessionId, gwTrace));
	}

	public void moveItem( String id, String container, String from) throws RemoteException
	{
		fire( gwPort.moveItemRequest( id, container, from, sessionId, gwTrace));
	}

	public void positionCursor( String container, int cursor, CursorSeek seek, int offset) throws RemoteException
	{
		fire( gwPort.positionCursorRequest( container, cursor, seek, offset, sessionId, gwTrace));
	}

	public void purgeDeletedItems() throws RemoteException
	{
		fire( gwPort.purgeDeletedItemsRequest( sessionId, gwTrace));
	}

	public void purge( ItemRefList items) throws RemoteException
	{
		fire( gwPort.purgeRequest( items, sessionId, gwTrace));
	}

	public ItemList readCursor( String container, int cursor, boolean forward, CursorSeek position, Integer count) throws RemoteException
	{
		ReadCursorResponse resp = gwPort.readCursorRequest( container, cursor, forward, position, count, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItems();
	}

	public void removeCustomDefinition( CustomList customs, boolean books, boolean doAsynchronous) throws RemoteException
	{
		fire( gwPort.removeCustomDefinitionRequest( customs, books, doAsynchronous, sessionId, gwTrace));
	}

	public void removeItem( String container, String id) throws RemoteException
	{
		fire( gwPort.removeItemRequest( container, id, sessionId, gwTrace));
	}

	public void removeItems( String container, ItemRefList items) throws RemoteException
	{
		fire( gwPort.removeItemsRequest( container, items, sessionId, gwTrace));
	}

	public void removeJunkEntry( String id) throws RemoteException
	{
		fire( gwPort.removeJunkEntryRequest( id, sessionId, gwTrace));
	}

	public void removeMembers( String container, GroupMemberList members) throws RemoteException
	{
		fire( gwPort.removeMembersRequest( container, members, sessionId, gwTrace));
	}

	public void removeProxyAccess( String id) throws RemoteException
	{
		fire( gwPort.removeProxyAccessRequest( id, sessionId, gwTrace));
	}

	public void removeProxyUser( String id) throws RemoteException
	{
		fire( gwPort.removeProxyUserRequest( id, sessionId, gwTrace));
	}

	public void removeSignature( String id, Boolean all, Boolean global) throws RemoteException
	{
		fire( gwPort.removeSignatureRequest( id, all, global, sessionId, gwTrace));
	}

	public Item reply( String id, String[] view) throws RemoteException
	{
		ReplyResponse resp = gwPort.replyRequest( id, view, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getItem();
	}

	public void retract( ItemRefList items, String comment, Boolean retractingAllInstances, Boolean retractCausedByResend, RetractType retractType) throws RemoteException
	{
		fire( gwPort.retractRequest( items, comment, retractingAllInstances, retractCausedByResend, retractType, sessionId, gwTrace));
	}

	public String[] sendItem( Item item) throws RemoteException
	{
		SendItemResponse resp = gwPort.sendItemRequest( item, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getId();
	}

	public void setTimestamp( Calendar backup, Calendar retention) throws RemoteException
	{
		fire( gwPort.setTimestampRequest( backup, retention, sessionId, gwTrace));
	}

	public Integer startFreeBusySession( FreeBusyUserList users, Calendar startDate, Calendar endDate) throws RemoteException
	{
		StartFreeBusySessionResponse resp = gwPort.startFreeBusySessionRequest( users, startDate, endDate, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getFreeBusySessionId();
	}

	public void unaccept( ItemRefList items) throws RemoteException
	{
		fire( gwPort.unacceptRequest( items, sessionId, gwTrace));
	}

	public void uncomplete( ItemRefList items) throws RemoteException
	{
		fire( gwPort.uncompleteRequest( items, sessionId, gwTrace));
	}

	public SignatureData updateVersionStatus( String id, VersionEventType event, SignatureData part) throws RemoteException
	{
		UpdateVersionStatusResponse resp = gwPort.updateVersionStatusRequest( id, event, part, sessionId, gwTrace);
		fire( resp.getStatus());
		return resp.getPart();
	}

	public String getUser() {
		return user;
	}

}
