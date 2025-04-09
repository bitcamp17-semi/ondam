document.addEventListener("DOMContentLoaded", function () {
    console.log("📢 알림 스크립트 로드됨");

    // ✅ AJAX로 로그인한 사용자 ID 받아오기
    $.ajax({
      url: "/api/users/readUserById",
      type: "GET",
      success: function (res) {
        if (res.status === "ok" && res.result.id) {
          const userId = res.result.id;
          //console.log("로그인 userId:", userId);

          const eventSource = new EventSource("/alarm/connect?userId=" + userId);

          eventSource.addEventListener("connect", function (event) {
            console.log("SSE 연결됨:", event.data);
          });

          eventSource.addEventListener("schedule", function (event) {
            console.log("일정 알림 수신:", event.data);
            showToast(event.data);
          });

          eventSource.onerror = function (error) {
            console.error("SSE 오류 발생:", error);
          };
        } else {
          console.error("❌ 사용자 정보를 가져오지 못했습니다.");
        }
      },
      error: function () {
        console.error("❌ AJAX 요청 실패 - 사용자 정보");
      }
    });

    // 토스트 메시지 출력 함수
    function showToast(message) {
      const toastBody = document.getElementById("toastMessage");
      toastBody.textContent = message;

      const toastElement = document.getElementById("alarmToast");
      const toast = new bootstrap.Toast(toastElement);
      toast.show();
    }
  });