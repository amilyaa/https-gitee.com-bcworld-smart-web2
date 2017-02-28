package cn.com.smart.web.tag;

import com.mixsmart.utils.StringUtils;

/**
 * 表格树
 * @author lmq
 *
 */
public class TableTreeTag extends AbstractTableTreeTag {

	private static final long serialVersionUID = -8947261853934909356L;
	
	@Override
	protected String getHtml(Boolean isParent,Object[] objArray,int layer, String defaultValue, int startIndex, int cols) {
		StringBuffer strBuff = new StringBuffer();
		String classOpTree = null;
		if(isExpand==1) {
			classOpTree = "tr-open-tree";
		} else {
			classOpTree = "tr-shrink-tree";
		}
		strBuff.append("<tr id='t-"+StringUtils.handNull(objArray[0])+"' class='tr-tree "+classOpTree+" t-tree-layer"+layer+" t-"+StringUtils.handNull(objArray[1])+"' parentid='t-"+StringUtils.handNull(objArray[1])+"'>");
		int count = 0;
		String a = getTdContent(objArray, defaultValue, count, startIndex);
		String tdOpData =  "";
		String uiIconOpData = "";
		if(isParent) {
			if(isExpand==1) {
				tdOpData = "open-data";
				uiIconOpData = "ui-icon-triangle-1-s";
			} else {
				tdOpData = "shrink-data";
				uiIconOpData = "ui-icon-triangle-1-e";
			}
			tdOpData = "op-tree "+tdOpData;
		} else {
			uiIconOpData = "ui-icon-radio-on";
		}
		strBuff.append("<td class='"+tdOpData+" td-tree "+getTdClass(count)+ "' "+super.getTdWidthStyle(thWidth,count)+"><span class='ui-icon "+uiIconOpData+" left'></span>"+a+"</td>");
		for (int i = startIndex; i < objArray.length; i++) {
			count++;
			if(count > cols) {
				break;
			}
			a = getTdContent(objArray, StringUtils.handNull(objArray[i]), count, i);
			strBuff.append("<td "+(StringUtils.isEmpty(getTdClass(count))?"":"class='"+getTdClass(count)+"'")+" "+super.getTdWidthStyle(thWidth,count)+">"+a+"</td>");
		}
		strBuff.append(super.handleLastCustomCell(objArray, count, tdStyles, thWidth));
		strBuff.append("</tr>");
		return strBuff.toString();
	}


	@Override
	protected String getTableDivTag() {
		return "cnoj-tree-table";
	}
}
