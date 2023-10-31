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

// 点击叉号删除一条消息
function delete_msg() {
	var btn = this;
	// 要删除的消息的id
	var id = $(btn).prev().val();
	$.post(
		CONTEXT_PATH + "/letter/erase",
		{"id":id},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				$(btn).parents(".media").remove();
			} else {
				alert(data.msg);
			}
		}
	);
}