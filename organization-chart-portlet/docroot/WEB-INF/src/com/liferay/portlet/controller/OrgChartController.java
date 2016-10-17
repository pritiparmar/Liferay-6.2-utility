package com.liferay.portlet.controller;

import java.io.IOException;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ClassNameLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liferay.util.portlet.PortletProps;

public class OrgChartController extends MVCPortlet{

    
    public static String CUSTOM_ATR = PortletProps.get("orgchart.user.custom.attribute");
    public static String ROOT = PortletProps.get("orgchart.root.jobtitle");
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		JSONObject userJSON=JSONFactoryUtil.createJSONObject();
		String cmd = ParamUtil.getString(renderRequest, "cmd","t2b");
		try {
			List<User> userList = UserLocalServiceUtil.getUsers(-1,-1);
			for(User user : userList){
				if(user.getJobTitle().equalsIgnoreCase(ROOT) && user.getStatus()==WorkflowConstants.STATUS_APPROVED){
					userJSON.put("id", user.getUserId());
					userJSON.put("name", user.getFullName());
					userJSON.put("title", user.getJobTitle());
					userJSON.put("parent","null");
					userJSON.put("className","top-level");
					userJSON.put("treeLevel", 0);
					userJSON.put("nodeLevel", 0);
					userJSON.put("tooltip", user.getJobTitle());
					userJSON.put("children",setChildUsers(user,1));
				}
			}
			renderRequest.setAttribute("data", userJSON.toString());
			renderRequest.setAttribute("direction", cmd);
		} catch (SystemException e) {
			e.printStackTrace();
		}
		super.doView(renderRequest, renderResponse);
	}

	private JSONArray setChildUsers(User user,long count) throws SystemException {
		long companyId = PortalUtil.getDefaultCompanyId();
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(User.class);
		JSONArray childUserJsonArray=JSONFactoryUtil.createJSONArray();
		List<ExpandoValue> values = ExpandoValueLocalServiceUtil.getColumnValues(companyId, classNameId, ExpandoTableConstants.DEFAULT_TABLE_NAME, CUSTOM_ATR, user.getEmailAddress(),  -1, -1);
		for (int i = 0; i < values.size(); i++) {
			try {
				long userId = values.get(i).getClassPK();
				User data = UserLocalServiceUtil.getUser(userId);
				if(data.getStatus()==WorkflowConstants.STATUS_APPROVED){
					JSONObject childObject=JSONFactoryUtil.createJSONObject();
					childObject.put("id", data.getUserId());
					childObject.put("name", data.getFullName());
					childObject.put("title", data.getJobTitle());
					childObject.put("className","level_"+count+StringPool.DASH+"type_"+i);
					childObject.put("parent",user.getUserId());
					childObject.put("treeLevel", count);
					childObject.put("nodeLevel", i);
					childObject.put("tooltip", data.getJobTitle());
					childObject.put("children",setChildNodes(data,childObject.getLong("treeLevel")+1,i));
					childUserJsonArray.put(childObject);
				}
			} catch (NoSuchUserException e) {
			} catch (PortalException e) {
			}
		  }
		return childUserJsonArray;
	}

	
	private JSONArray setChildNodes(User user, long count,long nodeLevel) throws SystemException {
		long companyId = PortalUtil.getDefaultCompanyId();
		long classNameId = ClassNameLocalServiceUtil.getClassNameId(User.class);
		JSONArray childUserJsonArray=JSONFactoryUtil.createJSONArray();
		List<ExpandoValue> values = ExpandoValueLocalServiceUtil.getColumnValues(companyId, classNameId, ExpandoTableConstants.DEFAULT_TABLE_NAME, CUSTOM_ATR, user.getEmailAddress(),  -1, -1);
		for (int i = 0; i < values.size(); i++) {
			try {
				long userId = values.get(i).getClassPK();
				User data = UserLocalServiceUtil.getUser(userId);
				if(data.getStatus()==WorkflowConstants.STATUS_APPROVED){
					JSONObject childObject=JSONFactoryUtil.createJSONObject();
					childObject.put("id", data.getUserId());
					childObject.put("name", data.getFullName());
					childObject.put("title", data.getJobTitle());
				    childObject.put("className","level_"+count+StringPool.DASH+"type_"+nodeLevel);
					childObject.put("parent",user.getUserId());
					childObject.put("treeLevel", count);
					childObject.put("nodeLevel", nodeLevel);
					childObject.put("tooltip", data.getJobTitle());
					childObject.put("children",setChildNodes(data,childObject.getLong("treeLevel")+1,nodeLevel));
					childUserJsonArray.put(childObject);
				}
			} catch (NoSuchUserException e) {
			} catch (PortalException e) {
			}
		  }
		return childUserJsonArray;
	}

	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException, PortletException {
		long userId = ParamUtil.getLong(resourceRequest,"userId");
		long parentUserId = ParamUtil.getLong(resourceRequest,"parentUserId");
		try {
			User user = UserLocalServiceUtil.getUser(userId);
			User parentUser =  UserLocalServiceUtil.getUser(parentUserId);
			user.getExpandoBridge().setAttribute(CUSTOM_ATR, parentUser.getEmailAddress(),false);
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		super.serveResource(resourceRequest, resourceResponse);
	}


}
