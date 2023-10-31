$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	// 给谁发送
	var toName = $("#recipient-name").val();
	// 发送的内容是什么
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH+"/letter/send",
		{"toName":toName, "content":content},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				$("#hintBody").text("发送成功！");
			} else {
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");
			// 无论成功还是失败，都希望把页面刷新一下
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}