/**
 * 初始化表单
 * @author lmq
 */
(function($){
    var InitForm = function(element, options) {
        this.setting = {};
        this.initOptions(options);
        this.$this = $(element);
    };
    //默认参数
    InitForm.defaultOptions = {
            formData:null,
            username:null,
            deptName:null,
            formFieldNames:null,
            formAttTabTag:'#form-att-tab',
            //获取表单附件信息URI
            formAttInfoUri:'form/attachment/info',
            //删除附件URI
            delAttUri:'form/attachment/deleteForm',
            isToLabel: false,
            //表单数据初始化前执行
            initDataBefore:null,
            //表单数据初始化之后执行
            initDataAfter: null,
            //init方法执行完后执行的回调函数
            callback: null,
     };
    InitForm.prototype = {
            /**
             * 初始化参数
             * @param options 参数
             */
            initOptions: function(options) {
                this.setting = $.extend(true, InitForm.defaultOptions, options);
                return this;
            },
            init: function() {
                var $parent = this.$this.parent();
                this.$this.addClass("v-hidden");
                $parent.prepend('<div class="cnoj-loading"><i class="fa fa-spinner fa-spin fa-lg"></i> 正在加载，请稍候...</div>');
                if(typeof(this.setting.initDataBefore) === 'function') {
                    this.setting.initDataBefore();
                }
                this.initFormData();
                if(typeof(this.setting.initDataAfter) === 'function') {
                    this.setting.initDataAfter();
                }
                if(typeof(handleForm) === 'function') {
                    handleForm();
                }
                //如果富文本为disable时，去掉textarea
                this.$this.find(".cnoj-richtext").each(function(){
                    var $self = $(this);
                    if($self.prop("disabled")) {
                        $self.addClass("hidden");
                        $self.prop("disabled", false);
                        $self.parent().html("<div>"+$self.val()+"</div>");
                    }
                });
                richtextListener();
                $parent.find(">.cnoj-loading").remove();
                this.$this.removeClass("v-hidden");
                if(this.setting.isToLabel) {
                    this.formValueToLabel(this.$this);
                }
                if(typeof(this.setting.callback) === 'function') {
                    this.setting.callback.apply(this, this.$this);
                }
            },
            /**
             * 初始化表单数据
             */
            initFormData: function() {
                if(utils.isEmpty(this.setting.formData)) {
                    return;
                }
                var output = $.parseJSON(this.setting.formData);
                if(output.result != '1') {
                    return;
                }
                var datas = output.datas;
                const self = this;
                for(var i=0;i<datas.length;i++) {
                    if(null != datas[i].nameMoreValues && datas[i].nameMoreValues.length>0) {
                        var tableTag = datas[i].name+"_table";
                        var $tableTag = this.$this.find("#"+tableTag);
                        $tableTag.find(".listctrl-add-row").hide();
                        var nameMoreValues = datas[i].nameMoreValues;
                        var rows = nameMoreValues[0].valueSize;
                        for(var j = 1;j<rows;j++) {
                            //调用表单中的添加行的方法，注：该方法在表单中
                            tbAddRow(datas[i].name);
                        }
                        //处理控件列表
                        $tableTag.find(".delrow").addClass("hide");
                        //控件列表是否可以显示添加按钮
                        var isListCtrlAdd = true;
                        //控件列表是否可以显示删除按钮
                        var isListCtrlDel = true;
                        for (var j = 0; j < nameMoreValues.length; j++) {
                            var index = 0;
                            $tableTag.find("input[name='"+nameMoreValues[j].name+"'],select[name='"+nameMoreValues[j].name+"']," +
                            		"textarea[name='"+nameMoreValues[j].name+"'],#"+nameMoreValues[j].name).each(function(){
                            	if(nameMoreValues[j].valueSize > 1) {
                            	    if(nameMoreValues[j].value[index] != 'null')
                            	        self.setFormValue($(this),nameMoreValues[j].value[index]);
                            	    index++;
                            	} else {
                            	    if(nameMoreValues[j].value != 'null')
                            	        self.setFormValue($(this),nameMoreValues[j].value);
                                }
                            });//end find
                            
                            var isTr = false;
                            if(utils.isNotEmpty(this.setting.formFieldNames)) {
                                var fieldNames = this.setting.formFieldNames.split(",");
                                for(var k=0;k<fieldNames.length;k++) {
                                    if(fieldNames[k] == nameMoreValues[j].name) {
                                        isTr = true;
                                        break;
                                    }
                                }// end for
                            } else {
                                isTr = true;
                            }
                            if(!isTr) {
                                isListCtrlAdd = false;
                            } 
                            if(nameMoreValues[j].name.endWith("_id")) {
                                isTr = true;
                            }
                            isListCtrlDel = isListCtrlDel && isTr;
                         }//end for
                        //判断是否可以操作listctrl
                        //当能填写或修改列表中的值时，则拥有添加行的权限
                        if(isListCtrlAdd) {
                            $tableTag.find(".listctrl-add-row").show();
                        }
                        //当所有字段都有修改权限时，则拥有删除行的权限
                        if(isListCtrlDel) {
                            $tableTag.find(".delrow:gt(0)").removeClass("hide");
                         }
                     } else {
                         var index = 0;
                         var name = datas[i].name;
                         this.$this.find("input[name='"+name+"'],select[name='"+name+"'],textarea[name='"+name+"']," +
                         		"#"+name+",span[data-name='"+name+"']").each(function(){
                              var value = datas[i].value;
                              var $findElement = $(this);
                              var tagName = $findElement.prop("tagName").toLowerCase();
                              if(tagName == 'span') {
                                  $findElement.attr("data-default-value", value);
                              } else if(utils.isNotEmpty(value) && value != 'null') {
                                  self.setFormValue($findElement,value, tagName);
                              } else if(tagName == 'div' && $findElement.hasClass("file-upload")) {
                                  self.formAttPluginHandler($findElement, value);
                              }
                              index++;
                          });
                     }
              }//end for
            },
            /**
             * 设置表单值
             * @param $this 表单元素
             * @param value 表单值
             * @param tagName html标签名称
             */
            setFormValue: function($this, value, tagName){
                var type = $this.attr("type");
                if(type == 'checkbox' || type == 'radio') {
                    if(value.indexOf(",")>-1) {
                        var values = value.split(",");
                        for(var i=0; i<values.length;i++) {
                            if($this.val() == values[i]) {
                                $this.prop("checked",true);
                            }
                        }
                    } else {
                        if($this.val() == value) {
                            $this.prop("checked",true);
                        }
                    }
                } else if(type == 'file') {
                    this.formAttHandler($this, value);
                } else {
                    if(typeof(tagName) != 'undefined' && tagName == 'div' && $this.hasClass("file-upload")) {
                        this.formAttPluginHandler($this, value);
                    } else {
                        if(utils.isNotEmpty(value)) {
                            if($this.hasClass('cnoj-datetime')) {
                                if(utils.isNotEmpty(value)) {
                                    if(value.endWith(".0"))
                                        value = value.substr(0,19);
                                }
                            } else if($this.hasClass('cnoj-date')) {
                                if(utils.isNotEmpty(value)) {
                                    if(value.length>10)
                                        value = value.substr(0,11);
                                }
                            } else if($this.hasClass('cnoj-time')) {
                                if(value.length>=19)
                                    value = value.substr(11,19);
                            }
                        }
                        $this.val(value);
                    }
                }
            },
            /**
             * 处理表单附件
             * @param $element
             * @param value
             */
            formAttHandler: function($element, value) {
                var isDisabled = $element.prop("disabled");
                var name = $element.attr("name");
                var newName = name+"_file";
                var $eleClone = null;
                if(!isDisabled) {
                    $eleClone = $element.clone();
                    $eleClone.attr("name", newName);
                    $eleClone.attr("id", newName);
                    $element.removeClass("require");
                    $element.after($eleClone);
                    $element.attr("type","text");
                }
                if(utils.isNotEmpty(value)) {
                    $element.attr("type","text");
                    $element.addClass("hidden");
                    this.attachmentListHandler(value, $element, isDisabled);
                    if(null != $eleClone) {
                        $eleClone.removeClass("require");
                    }
                }
            },
            /**
             * 表单附件插件处理者
             * @param $element
             * @param value
             */
            formAttPluginHandler: function($element, value) {
                //创建一个隐藏的输入框
                var id = $element.attr("id");
                $element.prepend("<input type='hidden' name='"+id+"' />");
                var $inputEle = $element.find("input[name='"+id+"']");
                if(utils.isNotEmpty(value)) {
                    var isDisabled = $element.find(".fileinput-button").hasClass("disabled");
                    if(isDisabled) {
                        $element.find(".fileinput-button").addClass("hidden");
                    }
                    this.attachmentListHandler(value, $inputEle, isDisabled);
                }   
            },
            /**
             * 附件列表处理者
             * @param value
             * @param $element
             */
            attachmentListHandler: function(value,$element, isDisabled) {
                if(utils.isEmpty(this.setting.formAttInfoUri)) {
                    alert("formAttInfoUri参数不能为空！");
                    return false;
                }
                var attsInfoUri = utils.isContain(this.setting.formAttInfoUri,"?")?this.setting.formAttInfoUri+"&":this.setting.formAttInfoUri+"?";
                $.get(attsInfoUri+"id="+value, function(output){
                    var attInfos = null;
                    var elementId = $element.attr("name");
                    var attIds = "";
                    if(output.result == 1) {
                        attInfos = "<ul class='file-list' id='formatt_'"+elementId+">";
                        var len = output.datas.length;
                        var datas = output.datas;
                        var fileType = null;
                        for(var i=0; i<len; i++) {
                            attInfos += "<li class='att-item'><span class='visible-print-inline'>"+datas[i][2]+"</span><a class='hidden-print' href='download/att?id="+datas[i][0]+"' target='_blank'>"+datas[i][2]+"</a>（"+datas[i][3]+"）";
                            attInfos += "<ul class='form-att-op hidden list-inline hidden-print'>操作：";
                            fileType = utils.handleNull(datas[i][4]);
                            if(utils.isNotEmpty(fileType)) {
                                fileType = fileType.toLowerCase();
                            }
                            if(fileType == 'jpg' || fileType == 'gif' || fileType == 'png' || fileType == 'txt' || fileType == 'pdf') {
                                attInfos += "<li><a href='att/view?id="+datas[i][0]+"' target='_blank'>查看</a></li>";
                            }
                            attInfos += "<li><a href='download/att?id="+datas[i][0]+"' target='_blank'>下载</a></li>";
                            if(!isDisabled) {
                                attInfos += "<li><a href='javascript:void(0)' data-input-id='"+elementId+"' onclick=deleteFormAtt(this,'"+datas[i][1]+"','"+datas[i][0]+"')><i class='fa fa-trash' aria-hidden='true'></i> 删除</a></li>";
                            }
                            attInfos +="</ul></li>";
                            attIds += datas[i][0]+",";
                        }
                        attInfos += "</ul>";
                        var $ul = $(attInfos);
                        $ul.find(".att-item").mouseover(function() {
                            var $this = $(this);
                            var h = $this.height();
                            var $attOp = $this.find(".form-att-op");
                            var pos = $this.position();
                            $attOp.css({"top":(pos.top+h)+"px","left":pos.left+"px"});
                            $attOp.removeClass("hidden");
                        }).mouseout(function() {
                            $(this).find(".form-att-op").addClass("hidden");
                        });
                        //判断是否添加过，如果添加过，则删除附件列表元素
                        var $parent = $element.parent();
                        var $fileList = $parent.find(".file-list");
                        if($fileList.length>0) {
                            $fileList.remove();
                        }
                        $parent.prepend($ul);
                    }
                    if(utils.isNotEmpty(attIds)) {
                        attIds = attIds.substring(0, attIds.length-1);
                    }
                    $element.val(attIds);
                });
            },
            /**
             * 表单值转换为label
             * @param $element
             * @returns {Boolean}
             */
            formValueToLabel: function($element) {
                if(utils.isEmpty($element)) {
                    return false;
                }
                $element.find("input[type=text],select,textarea").each(function(){
                    var $obj = $(this);
                    var tagName = $obj.prop("tagName").toLowerCase();
                    if(!$obj.hasClass("hidden") && !$obj.hasClass("hide")) {
                        var value = $obj.val();
                        if(utils.isNotEmpty(value)) {
                            value = utils.replaceAll(value,'\n','<br />');
                        }
                        if(tagName == 'select') {
                            value = $obj.find("option:selected").text();
                        } else if(tagName == 'input' && ($obj.attr("type") == 'checkbox' || $obj.attr("type") == 'radio')) {
                            if($obj.prop("checked")) {
                                $obj.addClass("hidden");
                                return;
                            } else {
                                $obj.parent().addClass("hidden");
                                return;
                            }
                        }
                        var width = $obj.width();
                        $obj.addClass("hidden");
                        var $td = $obj.parents("td:eq(0)");
                        var tbColor = $td.css("border-color");
                        if($td.length==0 || utils.isNotEmpty(tbColor) && 
                                (tbColor.toLowerCase() == '#fff' || tbColor.toLowerCase() == '#ffffff' 
                                    || tbColor.toLowerCase() == 'rgb(255, 255, 255)'))
                            $obj.after("<span style='border-bottom:1px solid #ccc;display:inline-block;width:"+width+"px'>"+value+"</span>");
                        else {
                            $obj.after("<span>"+value+"</span>");
                        }
                    }
                });
            },
            /**
             * 删除附件
             * @param elementObj 元素对象
             * @param id 流程附件ID
             * @param attId 附件ID
             */
             deleteFormAtt: function(elementObj, id, attId) {
                if(utils.isNotEmpty(id)) {
                    var $li = $(elementObj).parents("li:eq(0)");
                    var $ul = $(elementObj).parents("ul:eq(0)");
                    var inputEleId = $(elementObj).data("input-id");
                    var formDataId = $("#form-data-id").val();
                    var delAttUri = utils.isContain(this.setting.delAttUri,"?")?this.setting.delAttUri+"&":this.setting.delAttUri+"?";
                    if(utils.isEmpty(this.setting.delAttUri)) {
                        alert("delAttUri参数不能为空！");
                        return false;
                    }
                    BootstrapDialogUtil.delDialog("附件",delAttUri+'fieldId='+inputEleId+'&formDataId='+formDataId+"&attId="+attId,id,function(){
                        $li.remove();
                        //删除隐藏文本框内的对应的附件ID
                        var $parent = $(elementObj).parents(".file-list:eq(0)").parent();
                        var $inputEle = $parent.find("input[name='"+inputEleId+"']");
                        if($inputEle.length>0) {
                            var attIds = $inputEle.val();
                            if(utils.isNotEmpty(attIds)) {
                                attIds = attIds.replace(attId+",","").replace(","+attId,"").replace(attId,"");
                            }
                            $inputEle.val(attIds);
                        }
                        //判断是否还有附件
                        $li = $ul.find("li");
                        if($li.length == 0) {
                            var $inputFile = $ul.parent().find("input:eq(0)");
                            $inputFile.val("");
                        }
                        if(utils.isNotEmpty(attListUri)) {
                            loadUri(formAttTabTag,attListUri,false);
                        }
                    });
                }
            },
            /**
             * 显示表单附件列表
             * @param datas
             * @param $element
             */
            showFormAttList: function(datas, $element) {
                if(utils.isNotEmpty(datas) && datas.length>0 && utils.isNotEmpty($element)) {
                    var id = $element.attr("id");
                    var inputName = id.replace("-mfile","");
                    var $parent = $("#"+inputName);
                    var tagName = $parent.prop("tagName");
                    //console.log($parent.attr("id")+","+tagName);
                    var $inputEle = $parent.find("input[name='"+inputName+"']");
                    var attId = '';
                    for(var i=0;i<datas.length;i++) {
                        attId += datas[i].id+',';
                    }
                    attId = attId.substring(0, attId.length-1);
                    var value = $inputEle.val();
                    if(utils.isNotEmpty(value)) {
                        attId = value+","+attId;
                    }
                    this.attachmentListHandler(attId, $inputEle, false);
                }
            },
            /**
             * @param url 提交表单地址
             * @param param 参数
             * @param callback 提交后的回调函数
             */
            submitForm: function(url, param, callback) {
                if(utils.isEmpty(url)) {
                    alert("url参数不能为空");
                    return false;
                }
                if(utils.isNotEmpty(param)) {
                    url = utils.isContain(url,"?") ? (url+"&") : (url+"&");
                    url += "&"+param;
                }
                this.$this.attr("action", url);
                this.$this.attr("target","handle-form-iframe");
                this.$this.submit(); //提交表单到iframe
                $("#handle-form-iframe").load(function(){
                    parent.utils.closeWaitLoading();
                    var result = $(this).contents().text();
                    if(utils.isNotEmpty(result)) {
                        var output = $.parseJSON(result);
                        parent.utils.showMsg(output.msg);
                        if(output.result=='1') {
                            if(typeof(callback) === 'function') {
                                callback(output.result);
                            }
                            parent.closeActivedTab();
                        } else {
                            if(typeof(callback) === 'function') {
                                callback(output.result);
                            }
                        }
                    } else {
                        if(typeof(callback) === 'function') {
                            callback(output.result);
                        }
                        parent.utils.showMsg('提交失败');
                    }
                });
            }
    };
    var initFormObj = null;
	$.fn.initForm = function(options) {
	    var $this = $(this);
	    var initFormObj = $this.data("init.form.obj");
	    if(null == initFormObj) {
	        initFormObj = new InitForm(this, options);
	        $this.data("init.form.obj", initFormObj);
	    }
	    return initFormObj;
	};
})(jQuery);

/**
 * 删除附件 监听
 * @param formAttTag 表单附件所在的标识
 * @param formAttTabTag 表单附件选项卡标识
 * @param delAttUri 删除附件的URI
 */
function listenerAttDel(formAttTag, formAttTabTag, delAttUri) {
    //#form-attachment
	$(formAttTag+" .att-del").click(function(){
		var $tr = $(this).parent().parent();
		var id = $tr.find(".form-att-id").val();
		if(utils.isNotEmpty(id)) {
		    //op/del.json?busiName=flowAtt
			BootstrapDialogUtil.delDialog("附件",delAttUri,id,function(){
				$tr.remove();
				if(utils.isNotEmpty(attListUri)) {
				    //formAttTabTag
					loadUri(formAttTabTag, attListUri,false);
				}
			});
		}
	});
}

