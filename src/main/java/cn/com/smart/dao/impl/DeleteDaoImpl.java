package cn.com.smart.dao.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.com.smart.bean.BaseBean;
import cn.com.smart.dao.IDeleteDao;
import cn.com.smart.exception.DaoException;

import com.mixsmart.utils.StringUtils;

/**
 * 删除Dao实现类
 * @author lmq
 * @version 1.0
 * @since JDK版本大于等于1.6
 * 
 * 2015年8月22日
 * @param <T>
 */
public abstract class DeleteDaoImpl<T extends BaseBean> extends QueryDaoImpl<T> implements IDeleteDao<T>{
	
	private static final Logger log = Logger.getLogger(DeleteDaoImpl.class);


	@Override
	public boolean delete(T o) throws DaoException {
		boolean is = false;
		if(null == o) {
			return is;
		}
		log.info("删除数据ID["+o.getId()+"]");
		try {
			getSession().delete(o);
			is = true;
			log.info("删除数据ID["+o.getId()+"][成功]");
		} catch (Exception e) {
			log.info("删除数据ID["+o.getId()+"][失败]");
			e.printStackTrace();
			is = false;
			throw new DaoException(e.getLocalizedMessage(), e.getCause());
		} finally {
			o = null;
		}
		return is;
	}
	
	@Override
	public boolean delete(Serializable id) throws DaoException {
		boolean is = false;
		is = delete(id,"id");
		return is;
	}
	
	@Override
	public boolean delete(List<T> list) throws DaoException {
		boolean is = false;
		if(null == list || list.size()<1) {
	    	return is;
	    }
		try {
			for (T o : list) {
				log.info("删除数据ID["+o.getId()+"]");
				getSession().delete(o);
				is = true;
				log.info("删除数据ID["+o.getId()+"][成功]");
			}
		} catch (Exception e) {
			log.info("批量删除数据[失败]");
			e.printStackTrace();
			is = false;
			throw new DaoException(e.getLocalizedMessage(), e.getCause());
		} finally {
			list = null;
		}
		return is;
	}
	
	
	/**
	 * 删除数据（按Bean属性）
     * 该方法是以HQL的格式删除数据，所有filedName为实体bean的属性
	 * @param id ID 多个ID之间用英文逗号“,”隔开
	 * @param fieldName bean的属性
	 * @return Boolean 删除成功返回：true；否则返回：false
	 * @throws DaoException
	 */
	protected boolean delete(Serializable id, String fieldName) throws DaoException {
		boolean is = false;
		if(StringUtils.isNotEmpty(fieldName)) {
			if(null != id && StringUtils.isNotEmpty(id.toString())) {
				String[] ids = id.toString().split(",");
				String delHql = "delete from "+clazz.getName()+" where "+fieldName+" in (:delIds)";
				Map<String, Object> param = new HashMap<String, Object>(1);
				param.put("delIds", ids);
				if(executeHql(delHql,param)>0) {
					is = true;
				}
				param = null;
				ids = null;
			}
		}
		return is;
	}
	
	@Override
	public boolean deleteByField(Map<String, Object> param) throws DaoException {
		boolean is = false;
		if(null != param && param.size()>0) {
			StringBuffer hqlBuffer = new StringBuffer();
			hqlBuffer.append("delete from "+clazz.getName()+" where ");
			int count = 0;
			for (String key : param.keySet()) {
				if(count > 0) {
					hqlBuffer.append(" and ");
				}
				if(null != param.get(key) && param.get(key).getClass().isArray()) {
					hqlBuffer.append(key+" in (:"+key+")");
				} else {
					hqlBuffer.append(key+"=:"+key);
				}
				count++;
			}
			is = executeHql(hqlBuffer.toString(), param)>0?true:false;
			hqlBuffer = null;
			param = null;
		}
		return is;
	}

	@Override
	public boolean delete(Map<String, Object> param) throws DaoException {
		return deleteByField(param);
	}
	
}
