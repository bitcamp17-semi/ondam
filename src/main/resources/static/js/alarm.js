document.addEventListener("DOMContentLoaded", function () {
    console.log("알림 스크립트 로드됨");
	// //bootstrap css가 없는 곳에서는 동적으로 넣어주도록 설정
	// if (!document.querySelector('link[href*="bootstrap.min.css"]')) {
	//     const bootstrapCss = document.createElement("link");
	//     bootstrapCss.rel = "stylesheet";
	//     bootstrapCss.href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css";
	//     document.head.appendChild(bootstrapCss);
	// }
	// //폰트 css 동적 추가
	// if (!document.querySelector('link[href*="fonts.googleapis.com"]')) {
	//   const fontLink = document.createElement("link");
	//   fontLink.rel = "stylesheet";
	//   fontLink.href = "https://fonts.googleapis.com/css2?family=Jua";
	//   document.head.appendChild(fontLink);
	// }

    // AJAX로 로그인한 사용자 ID 받아오기
    $.ajax({
      url: "/api/users/readUserBySession",
      type: "GET",
      success: function (res) {
        if (res.status === "ok" && res.result.id) {
          const userId = res.result.id;
          //console.log("로그인 userId:", userId);

                const eventSource = new EventSource("/alarm/connect?userId=" + userId);

                eventSource.addEventListener("connect", function (event) {
                    console.log("SSE 연결됨:", event.data);
                });

                eventSource.addEventListener("alarm", function (event) {
                    console.log("알림 수신:", event.data);
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

    // 알림 토스트 표시 함수
    function showToast(message) {
        let toastContainer = document.getElementById("alarmToast");

        if (!toastContainer) {
            const container = document.createElement("div");
            container.className = "position-fixed p-3";
            container.style.bottom = "3%";
            container.style.left = "5%";
            container.style.zIndex = "9999";
            container.innerHTML = `
              <div id="alarmToast" class="toast align-items-center border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                  <div class="toast-body" id="toastMessage" style="font-family:Jua;">${message}</div>
                  <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
              </div>
            `;
            document.body.appendChild(container);
            toastContainer = document.getElementById("alarmToast");
        } else {
            document.getElementById("toastMessage").textContent = message;
        }

        const toast = new bootstrap.Toast(toastContainer);
        toast.show();
    }
});