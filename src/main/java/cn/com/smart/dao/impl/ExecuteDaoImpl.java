package cn.com.smart.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import cn.com.smart.dao.IExecuteDao;
import cn.com.smart.exception.DaoException;
import cn.com.smart.utils.StringUtil;

/**
 * 执行Dao实现类
 * @author lmq
 * @version 1.0
 * @since JDK版本大于等于1.6
 * 
 * 2015年8月22日
 * @param <T>
 */
public abstract class ExecuteDaoImpl extends CommonDaoImpl implements IExecuteDao {

	private static final Logger log = Logger.getLogger(ExecuteDaoImpl.class);

	
	@Override
	public Integer executeHql(String hql) throws DaoException {
		int result = 0;
		if(StringUtil.isEmpty(hql)) {
	    	return result;
	    }
		log.info("执行HQL["+hql+"]");
		try {
			result = getQuery(hql, null, false).executeUpdate();
		} catch (Exception e) {
			log.info("执行HQL["+hql+"]--[异常]--["+e.getMessage()+"]");
			e.printStackTrace();
			throw new DaoException(e.getLocalizedMessage(), e.getCause());
		}
		return result;
	}

	@Override
	public Integer executeHql(String hql,Map<String, Object> param) throws DaoException {
	    int result = 0;
	    if(StringUtil.isEmpty(hql)) {
	    	return result;
	    }
	    try {
		    log.info("执行HQL["+hql+"]");
			Query query = getQuery(hql, param, false);
		    result = query.executeUpdate();
		    
		} catch (Exception e) {
			log.info("执行HQL["+hql+"]--[异常]--["+e.getMessage()+"]");
			e.printStackTrace();
			throw new DaoException(e.getLocalizedMessage(), e.getCause());
		} finally {
			param = null;
		}
		return result;
	}

	@Override
	public Integer executeHql(String hql, List<Map<String, Object>> params) throws DaoException {
		 int result = 0;
		 if(!StringUtil.isEmpty(hql)) {
			 log.info("执行HQL["+hql+"]");
			 try {
				 if (null != params && params.size() > 0) {
					for(Map<String,Object> param : params) {
						result += getQuery(hql, param, false).executeUpdate();
					}
				}
			 } catch (Exception e) {
				log.info("执行HQL["+hql+"]--[异常]--["+e.getMessage()+"]");
				e.printStackTrace();
				throw new DaoException(e.getLocalizedMessage(), e.getCause());
			} finally {
				params = null;
			}
		 }
		return result;
	}
	
}
