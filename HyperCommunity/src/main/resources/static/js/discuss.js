$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? 'Liked' : 'Like');

            } else {
                alert(data.msg);
            }
        }
    );
}

// function like(btn, entityType, entityId) {
//     $.post(
//         CONTEXT_PATH + "/like",
//         {"entityType": entityType, "entityId": entityId},
//         function (data) {
//             data = $.parseJSON(data);
//             if (data.code == 0) {
//                 console.log(data.code)
//                 $(btn).children("i").text(data.likeCount);
//                 $(btn).children("b").text(data.likeStatus == 1 ? 'Liked' : 'Like');
//             } else {
//                 alert(data.msg);
//             }
//         }
//     );
// }

// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#topBtn").text(data.type == 1 ? '已置顶' : '置顶');
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#wonderfulBtn").text(data.status == 1 ? '已加精' : '加精');
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}