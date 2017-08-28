package cn.com.smart.form.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mixsmart.constant.IMixConstant;
import com.mixsmart.utils.StringUtils;

import cn.com.smart.bean.SmartResponse;
import cn.com.smart.exception.DaoException;
import cn.com.smart.exception.ServiceException;
import cn.com.smart.form.bean.entity.TForm;
import cn.com.smart.form.bean.entity.TFormInstance;
import cn.com.smart.res.SQLResUtil;
import cn.com.smart.service.impl.MgrServiceImpl;
import cn.com.smart.web.bean.UserInfo;

/**
 * 表单实例 服务类
 * @author lmq 2017年8月28日
 * @version 1.0
 * @since 1.0
 */
@Service
public class FormInstanceService extends MgrServiceImpl<TFormInstance> {

    @Autowired
    private IFormDataService formDataServ;
    @Autowired
    private FormService formServ;

    /**
     * 创建表单实例
     * @param datas 表单数据
     * @param formDataId 表单数据ID
     * @param formId 表单ID
     * @param userInfo 用户信息
     * @return 返回创建结果
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public SmartResponse<String> create(Map<String, Object> datas, String formDataId, String formId,
            UserInfo userInfo) {
        SmartResponse<String> smartResp = new SmartResponse<String>();
        smartResp.setMsg("表单提交失败");
        TForm form = formServ.find(formId).getData();
        if (null == form) {
            return smartResp;
        }
        String insTitle = formServ.getInstanceTitle(datas, formId, userInfo.getId(), form.getName());
        if (StringUtils.isEmpty(insTitle)) {
            insTitle = form.getName() + "(" + userInfo.getFullName() + ")";
        }
        smartResp = formDataServ.saveOrUpdateForm(datas, formDataId, formId, userInfo.getId(), 0);
        if (OP_SUCCESS.equals(smartResp.getResult())) {
            TFormInstance formInstance = new TFormInstance();
            formInstance.setFormDataId(formDataId);
            formInstance.setFormId(formId);
            formInstance.setOrgId(userInfo.getOrgId());
            formInstance.setTitle(insTitle);
            formInstance.setUserId(userInfo.getId());
            super.save(formInstance);
            smartResp.setMsg("表单提交成功");
        }
        return smartResp;
    }

    @Override
    public SmartResponse<String> delete(String id) throws ServiceException {
        SmartResponse<String> smartResp = new SmartResponse<String>();
        smartResp.setMsg("表单实例删除失败");
        if (StringUtils.isEmpty(id)) {
            return smartResp;
        }
        String[] ids = id.split(IMixConstant.MULTI_VALUE_SPLIT);
        List<TFormInstance> list = super.finds(ids).getDatas();
        if (null != list && list.size() > 0) {
            if(deleteAssocByObj(list)) {
                smartResp.setResult(OP_SUCCESS);
                smartResp.setMsg("流程实例删除成功");
            }
        }
        return smartResp;
    }

    /**
     * 关联删除数据
     * @param list
     * @return
     */
    private boolean deleteAssocByObj(List<TFormInstance> list) {
        boolean is = false;
        Map<String, Object> param = null;
        try {
            String delSql = SQLResUtil.getOpSqlMap().getSQL("del_form_data");
            param = new HashMap<String, Object>();
            String sql = SQLResUtil.getOpSqlMap().getSQL("get_table_name");
            for (TFormInstance formIns : list) {
                param.put("formId", formIns.getFormId());
                List<Object> objs = super.getDao().queryObjSql(sql, param);
                if (null != objs && objs.size() > 0) {
                    for (Object obj : objs) {
                        if (StringUtils.isNotEmpty(delSql)) {
                            param.clear();
                            param.put("formDataId", formIns.getFormDataId());
                            super.getDao().executeSql(delSql.replace("${table}", obj.toString()), param);
                        }
                    } // for
                } // if
                super.delete(formIns.getId());
            } // for
            is = true;
        } catch (DaoException e) {
            e.printStackTrace();
        }
        return is;
    }
}
