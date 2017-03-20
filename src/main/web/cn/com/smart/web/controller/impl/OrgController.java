package cn.com.smart.web.controller.impl;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import cn.com.smart.bean.SmartResponse;
import cn.com.smart.utils.StringUtil;
import cn.com.smart.web.bean.UserInfo;
import cn.com.smart.web.bean.entity.TNOrg;
import cn.com.smart.web.controller.base.BaseController;
import cn.com.smart.web.filter.bean.UserSearchParam;
import cn.com.smart.web.helper.PageHelper;
import cn.com.smart.web.plugins.OrgZTreeData;
import cn.com.smart.web.service.OPService;
import cn.com.smart.web.service.OrgService;
import cn.com.smart.web.tag.bean.DelBtn;
import cn.com.smart.web.tag.bean.EditBtn;
import cn.com.smart.web.tag.bean.PageParam;
import cn.com.smart.web.tag.bean.RefreshBtn;

/**
 * 组织机构
 * @author lmq
 *
 */
@Controller
@RequestMapping("org")
public class OrgController extends BaseController {

private static final String VIEW_DIR = WEB_BASE_VIEW_DIR+"/org";
	
	@Autowired
	private OrgService orgServ;
	@Autowired
	private OPService opServ;
	
	@RequestMapping("list")
	public ModelAndView list(ModelAndView modelView) throws Exception {
		SmartResponse<Object> smartResp = opServ.getTreeDatas("org_mgr_tree_list");
		
		String uri = "org/list"; 
		addBtn = new EditBtn("add","showPage/base_org_add", "org", "添加组织机构", "600");
		editBtn = new EditBtn("edit","showPage/base_org_edit", "org", "修改组织机构", "600");
		delBtn = new DelBtn("org/delete.json", "确定要删除选中的组织机构吗？（注：如果该机构下面有子机构的话，会一起删除的哦~）",uri,null, null);
		
		//delBtn = new DelBtn("op/del.json", "org", "确定要删除选中的组织机构吗？（注：如果该机构下面有子机构的话，会一起删除的哦~）",uri,null, null);
		refreshBtn = new RefreshBtn(uri, "org",null);
		
		ModelMap modelMap = modelView.getModelMap();
		modelMap.put("smartResp", smartResp);
		modelMap.put("addBtn", addBtn);
		modelMap.put("editBtn", editBtn);
		modelMap.put("delBtn", delBtn);
		modelMap.put("refreshBtn", refreshBtn);
		addBtn = null;editBtn = null;delBtn = null;
		refreshBtn = null;
		modelView.setViewName(VIEW_DIR+"/list");
		return modelView;
	}
	
	/**
	 * 
	 * @param session
	 * @param org
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/add",method=RequestMethod.POST)
	public @ResponseBody SmartResponse<String> add(HttpSession session, TNOrg org) throws Exception {
		SmartResponse<String> smartResp = new SmartResponse<String>();
		if(null != org) {
			smartResp = orgServ.save(org);
			if(OP_SUCCESS.equals(smartResp.getResult())) {
				UserInfo userInfo = super.getUserInfoFromSession(session);
				userInfo.getOrgIds().add(smartResp.getData());
			}
		}
		return smartResp;
	}
	
	@RequestMapping(value="/edit",method=RequestMethod.POST)
	public @ResponseBody SmartResponse<String> edit(TNOrg org) throws Exception {
		SmartResponse<String> smartResp = new SmartResponse<String>();
		if(null != org) {
			smartResp = orgServ.update(org);
		}
		return smartResp;
	}
	
	/**
	 * 删除
	 * @param session
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/delete",method=RequestMethod.POST)
	@ResponseBody
	public SmartResponse<String> delete(HttpSession session, String id) {
		SmartResponse<String> smartResp = orgServ.delete(id);
		if(OP_SUCCESS.equals(smartResp.getResult())) {
			UserInfo userInfo = super.getUserInfoFromSession(session);
			userInfo.getOrgIds().remove(id);
		}
		return smartResp;
	}
	
	
	
	@RequestMapping("/tree")
	public @ResponseBody SmartResponse<OrgZTreeData> tree(HttpSession session,String treeType) throws Exception {
		return orgServ.getZTree(getUserInfoFromSession(session).getOrgIds(),treeType);
	}
	
	
	
	/**
	 * 该用户拥有的角色列表
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/rolelist")
	public ModelAndView rolelist(UserSearchParam searchParam,ModelAndView modelView,Integer page) throws Exception {
		String uri = "org/rolelist";
		page = null == page?1:page;
		page = PageHelper.getPage(page);
		
		SmartResponse<Object> smartResp = opServ.getDatas("org_role_list",searchParam, getStartNum(page), getPerPageSize());
		String paramUri = uri + ((null != searchParam)?("?"+searchParam.getParamToString()):"");
		pageParam = new PageParam(paramUri, null, page);
		uri = uri+"?id="+searchParam.getId();
		addBtn = new EditBtn("add","org/addRole?id="+searchParam.getId(), null, "该组织机构中添加角色", "600");
		delBtn = new DelBtn("op/moreParamDel.json?flag=o&orgId="+searchParam.getId(), "roleOrg", "确定要从该组织机构中删除选中的角色吗？",uri,"#org-role-tab", null);
		refreshBtn = new RefreshBtn(uri, null,"#org-role-tab");
		
		ModelMap modelMap = modelView.getModelMap();
		modelMap.put("smartResp", smartResp);
		modelMap.put("addBtn", addBtn);
		modelMap.put("delBtn", delBtn);
		modelMap.put("refreshBtn", refreshBtn);
		modelMap.put("pageParam", pageParam);
		modelMap.put("searchParam", searchParam);
		
		addBtn = null;delBtn = null;
		refreshBtn = null;pageParam = null;
		
		modelView.setViewName(VIEW_DIR+"/rolelist");
		return modelView;
	}
	

	@RequestMapping("/addRole")
	public ModelAndView addRole(UserSearchParam searchParam,ModelAndView modelView,Integer page) throws Exception {
		page = null == page?1:page;
		page = PageHelper.getPage(page);
		String uri = "org/addRole";
		SmartResponse<Object> smartResp = opServ.getDatas("org_addrole_list",searchParam, getStartNum(page), getPerPageSize());
		String paramUri = uri += (null != searchParam)?("?"+searchParam.getParamToString()):"";
		pageParam = new PageParam(paramUri, ".bootstrap-dialog-message", page);
		
		ModelMap modelMap = modelView.getModelMap();
		modelMap.put("smartResp", smartResp);
		modelMap.put("pageParam", pageParam);
		modelMap.put("searchParam", searchParam);
		pageParam = null;
		modelView.setViewName(VIEW_DIR+"/addRole");
		return modelView;
		
	}
	
	
	@RequestMapping(value="/saveRole",method=RequestMethod.POST)
	public @ResponseBody SmartResponse<String> saveRole(String id,String submitDatas) throws Exception {
		SmartResponse<String> smartResp = new SmartResponse<String>();
		if(!StringUtil.isEmpty(submitDatas) && !StringUtil.isEmpty(id)) {
			String[] values = submitDatas.split(",");
			smartResp = orgServ.addRole2Org(id, values);
			values = null;
			submitDatas = null;
		}
		return smartResp;
	}
	
}
