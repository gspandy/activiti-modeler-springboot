function dataFormet() {
	// 格式化时间窗
	$(".dateFormat").datetimepicker({
		format : 'yyyy-mm-dd',
		minView : 'month',
		language : 'zh-CN',
		autoclose : true,
		startDate : new Date()
	});
}