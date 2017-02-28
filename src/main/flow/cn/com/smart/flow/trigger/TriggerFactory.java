package cn.com.smart.flow.trigger;

import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.springframework.stereotype.Component;

import cn.com.smart.service.SmartContextService;

import com.mixsmart.utils.CollectionUtils;
import com.mixsmart.utils.StringUtils;

/**
 * 任务触发抽象类
 * @author lmq <br />
 * 2016年10月4日
 * @version 1.0
 * @since 1.0
 */
@Component
public class TriggerFactory<T> {
	
	/**
	 * 获取触发器
	 * @param triggerBean
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IFlowTrigger<T> getTrigger(String triggerType) {
		IFlowTrigger<T> trigger = null;
		if(StringUtils.isEmpty(triggerType)) {
			return trigger;
		}
		List<IFlowTrigger> triggers = SmartContextService.finds(IFlowTrigger.class);
		if(CollectionUtils.isNotEmpty(triggers)) {
			for (IFlowTrigger<T> triggerTmp : triggers) {
				if(triggerTmp.getName().equals(triggerType)) {
					trigger = triggerTmp;
					break;
				}
			}// for
		}// if
		return trigger;
	}
}
